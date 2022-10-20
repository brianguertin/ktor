/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.curl.internal

import io.ktor.io.*
import io.ktor.utils.io.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import libcurl.*
import platform.posix.*
import kotlin.coroutines.*

internal fun onHeadersReceived(
    buffer: CPointer<ByteVar>,
    size: size_t,
    count: size_t,
    userdata: COpaquePointer
): Long {
    val packet = userdata.fromCPointer<CurlResponseBuilder>().headersBytes
    val chunkSize = (size * count)
    packet.writeCPointer(buffer, chunkSize.toInt())
    return chunkSize.toLong()
}

internal fun onBodyChunkReceived(
    buffer: CPointer<ByteVar>,
    size: size_t,
    count: size_t,
    userdata: COpaquePointer
): Int {
    val wrapper = userdata.fromCPointer<CurlResponseBodyData>()
    if (!wrapper.bodyStartedReceiving.isCompleted) {
        wrapper.bodyStartedReceiving.complete(Unit)
    }

    val body = wrapper.body
    if (body.isClosedForWrite) {
        return if (body.closedCause != null) -1 else 0
    }

    val chunkSize = (size * count).toInt()
    body.writablePacket.writeCPointer(buffer, chunkSize)
    wrapper.bytesWritten += chunkSize
    if (wrapper.bytesWritten.value == chunkSize) {
        wrapper.bytesWritten.value = 0
        return chunkSize
    }

    TODO("backpressure")

    CoroutineScope(wrapper.callContext).launch {
        try {
            body.flush()
        } catch (_: Throwable) {
            // no op, error will be handled on next write on cURL thread
        } finally {
            wrapper.onUnpause()
        }
    }
    return CURL_WRITEFUNC_PAUSE
}

internal fun onBodyChunkRequested(
    buffer: CPointer<ByteVar>,
    size: size_t,
    count: size_t,
    dataRef: COpaquePointer
): Int {
    val wrapper: CurlRequestBodyData = dataRef.fromCPointer()
    val body = wrapper.body
    val requested = (size * count).toInt()

    if (body.isClosedForRead) {
        return if (body.closedCause != null) -1 else 0
    }

    val source = body.readablePacket.peek()

    if (source is WithRawMemory) {
        val count = source.raw { address, length ->
            val toRead = minOf(requested, length)
            memcpy(address, buffer, toRead.convert())
            return@raw toRead
        }

        body.readablePacket.discard(count)
        return count
    }

    val data: ByteArray = body.readArray()
    if (data.isNotEmpty()) {
        memcpy(data.refTo(0), buffer, data.size.convert())
        return data.size
    }

    CoroutineScope(wrapper.callContext).launch {
        try {
            body.awaitBytes()
        } catch (_: Throwable) {
            // no op, error will be handled on next read on cURL thread
        } finally {
            wrapper.onUnpause()
        }
    }
    return CURL_READFUNC_PAUSE
}

internal class CurlRequestBodyData(
    val body: ByteReadChannel,
    val callContext: CoroutineContext,
    val onUnpause: () -> Unit
)

internal class CurlResponseBodyData(
    val bodyStartedReceiving: CompletableDeferred<Unit>,
    val body: ByteWriteChannel,
    val callContext: CoroutineContext,
    val onUnpause: () -> Unit
) {
    internal val bytesWritten = atomic(0)
}

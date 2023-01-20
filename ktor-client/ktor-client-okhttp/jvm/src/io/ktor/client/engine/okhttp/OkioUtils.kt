/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.okhttp

import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.io.*
import kotlinx.coroutines.*
import okio.*
import okio.Buffer
import kotlin.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
internal fun BufferedSource.toChannel(
    context: CoroutineContext,
    requestData: HttpRequestData
): ByteReadChannel = GlobalScope.writer(context) {
    try {
        this@toChannel.readAll(toSink())
    } catch (cause: Throwable) {
        throw mapExceptions(cause, requestData)
    }
}

internal fun ByteWriteChannel.toSink() = object : Sink {
    override fun write(source: Buffer, byteCount: Long) {
        closedCause?.let { throw it }
        writeByteArray(source.readByteArray(byteCount))
    }

    override fun flush() {
        closedCause?.let { throw it }
        runBlocking {
            this@toSink.flush()
        }
    }

    override fun timeout(): Timeout = Timeout.NONE

    override fun close() {
        closedCause?.let { throw it }
        this@toSink.close()
    }
}

private fun mapExceptions(cause: Throwable, request: HttpRequestData): Throwable = when (cause) {
    is java.net.SocketTimeoutException -> SocketTimeoutException(request, cause)
    else -> cause
}

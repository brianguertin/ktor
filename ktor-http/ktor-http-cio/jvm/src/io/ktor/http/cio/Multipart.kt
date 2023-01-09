/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.http.cio

import io.ktor.http.cio.internals.*
import io.ktor.io.*
import io.ktor.io.IOException
import io.ktor.network.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.io.*
import java.io.EOFException
import java.nio.*
import java.nio.channels.*

/**
 * Represents a multipart content starting event. Every part need to be completely consumed or released via [release]
 */
public sealed class MultipartEvent {
    /**
     * Release underlying data/packet.
     */
    public abstract fun release()

    /**
     * Represents a multipart content preamble. A multipart stream could have at most one preamble.
     * @property body contains preamble's content
     */
    public class Preamble(public val body: Packet) : MultipartEvent() {
        override fun release() {
            body.close()
        }
    }

    /**
     * Represents a multipart part. There could be any number of parts in a multipart stream. Please note that
     * it is important to consume [body] otherwise multipart parser could get stuck (suspend)
     * so you will not receive more events.
     *
     * @property headers deferred that will be completed once will be parsed
     * @property body a channel of part content
     */
    public class MultipartPart(
        public val headers: Deferred<HttpHeadersMap>,
        public val body: ByteReadChannel
    ) : MultipartEvent() {
        @OptIn(ExperimentalCoroutinesApi::class)
        override fun release() {
            headers.invokeOnCompletion { t ->
                if (t != null) {
                    headers.getCompleted().release()
                }
            }
            runBlocking {
                body.discard()
            }
        }
    }

    /**
     * Represents a multipart content epilogue. A multipart stream could have at most one epilogue.
     * @property body contains epilogue's content
     */
    public class Epilogue(public val body: Packet) : MultipartEvent() {
        override fun release() {
            body.close()
        }
    }
}

/**
 * Parse multipart part headers
 */
@Deprecated("This is going to be removed. Use parseMultipart instead.", level = DeprecationLevel.ERROR)
public suspend fun parsePartHeaders(input: ByteReadChannel): HttpHeadersMap {
    return parsePartHeadersImpl(input)
}

/**
 * Parse multipart part headers
 */
private suspend fun parsePartHeadersImpl(input: ByteReadChannel): HttpHeadersMap {
    val builder = CharArrayBuilder()

    try {
        return parseHeaders(input, builder)
            ?: throw EOFException("Failed to parse multipart headers: unexpected end of stream")
    } catch (t: Throwable) {
        builder.release()
        throw t
    }
}

@Deprecated("This is going to be removed.", level = DeprecationLevel.ERROR)
public fun expectMultipart(headers: HttpHeadersMap): Boolean {
    return headers["Content-Type"]?.startsWith("multipart/") ?: false
}

/**
 * Starts a multipart parser coroutine producing multipart events
 */
public fun CoroutineScope.parseMultipart(
    input: ByteReadChannel,
    headers: HttpHeadersMap
): ReceiveChannel<MultipartEvent> {
    val contentType = headers["Content-Type"] ?: throw IOException("Failed to parse multipart: no Content-Type header")
    val contentLength = headers["Content-Length"]?.parseDecLong()

    return parseMultipart(input, contentType, contentLength)
}

/**
 * Starts a multipart parser coroutine producing multipart events
 */
public fun CoroutineScope.parseMultipart(
    input: ByteReadChannel,
    contentType: CharSequence,
    contentLength: Long?
): ReceiveChannel<MultipartEvent> {
    if (!contentType.startsWith("multipart/")) {
        throw IOException("Failed to parse multipart: Content-Type should be multipart/* but it is $contentType")
    }

    val boundary = "--" + parseBoundaryInternal(contentType)
    return produce {
        TODO("$boundary")
    }
}

private val CrLf = ByteBuffer.wrap("\r\n".toByteArray())!!
private val BoundaryTrailingBuffer = ByteBuffer.allocate(8192)!!

/**
 * Starts a multipart parser coroutine producing multipart events
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Deprecated("This is going to be removed. Use parseMultipart(contentType) instead.", level = DeprecationLevel.ERROR)
public fun CoroutineScope.parseMultipart(
    boundaryPrefixed: ByteBuffer,
    input: ByteReadChannel,
    totalLength: Long?
): ReceiveChannel<MultipartEvent> = produce {
    TODO()
//    val readBeforeParse = input.totalBytesRead
//    val firstBoundary = boundaryPrefixed.duplicate()!!.apply {
//        position(2)
//    }
//
//    val preamble = Packet()
//    parsePreambleImpl(firstBoundary, input, preamble, 8192)
//
//    if (preamble.size > 0) {
//        send(MultipartEvent.Preamble(preamble.build()))
//    }
//
//    if (skipBoundary(firstBoundary, input)) {
//        return@produce
//    }
//
//    val trailingBuffer = BoundaryTrailingBuffer.duplicate()
//
//    do {
//        input.readUntilDelimiter(CrLf, trailingBuffer)
//        if (input.readUntilDelimiter(CrLf, trailingBuffer) != 0) {
//            throw IOException("Failed to parse multipart: boundary line is too long")
//        }
//        input.skipDelimiter(CrLf)
//
//        val body = ByteChannel()
//        val headers = CompletableDeferred<HttpHeadersMap>()
//        val part = MultipartEvent.MultipartPart(headers, body)
//        send(part)
//
//        var hh: HttpHeadersMap? = null
//        try {
//            hh = parsePartHeadersImpl(input)
//            if (!headers.complete(hh)) {
//                hh.release()
//                throw kotlin.coroutines.cancellation.CancellationException("Multipart processing has been cancelled")
//            }
//            parsePartBodyImpl(boundaryPrefixed, input, body, hh)
//        } catch (t: Throwable) {
//            if (headers.completeExceptionally(t)) {
//                hh?.release()
//            }
//            body.close(t)
//            throw t
//        }
//
//        body.close()
//    } while (!skipBoundary(boundaryPrefixed, input))
//
//    if (input.availableForRead != 0) {
//        input.skipDelimiter(CrLf)
//    }
//
//    if (totalLength != null) {
//        @Suppress("DEPRECATION")
//        val consumedExceptEpilogue = input.totalBytesRead - readBeforeParse
//        val size = totalLength - consumedExceptEpilogue
//        if (size > Int.MAX_VALUE) throw IOException("Failed to parse multipart: prologue is too long")
//        if (size > 0) {
//            send(MultipartEvent.Epilogue(input.readPacket(size.toInt())))
//        }
//    } else {
//        val epilogueContent = input.readRemaining()
//        if (epilogueContent.isNotEmpty) {
//            send(MultipartEvent.Epilogue(epilogueContent))
//        }
//    }
}

private const val PrefixChar = '-'.code.toByte()

private fun findBoundary(contentType: CharSequence): Int {
    var state = 0 // 0 header value, 1 param name, 2 param value unquoted, 3 param value quoted, 4 escaped
    var paramNameCount = 0

    for (i in contentType.indices) {
        val ch = contentType[i]

        when (state) {
            0 -> {
                if (ch == ';') {
                    state = 1
                    paramNameCount = 0
                }
            }

            1 -> {
                if (ch == '=') {
                    state = 2
                } else if (ch == ';') {
                    // do nothing
                    paramNameCount = 0
                } else if (ch == ',') {
                    state = 0
                } else if (ch == ' ') {
                    // do nothing
                } else if (paramNameCount == 0 && contentType.startsWith("boundary=", i, ignoreCase = true)) {
                    return i
                } else {
                    paramNameCount++
                }
            }

            2 -> {
                when (ch) {
                    '"' -> state = 3
                    ',' -> state = 0
                    ';' -> {
                        state = 1
                        paramNameCount = 0
                    }
                }
            }

            3 -> {
                if (ch == '"') {
                    state = 1
                    paramNameCount = 0
                } else if (ch == '\\') {
                    state = 4
                }
            }

            4 -> {
                state = 3
            }
        }
    }

    return -1
}

/**
 * Parse multipart boundary encoded in [contentType] header value
 */
internal fun parseBoundaryInternal(contentType: CharSequence): String {
    val boundaryStart = findBoundary(contentType)

    if (boundaryStart == -1) {
        throw IOException("Failed to parse multipart: Content-Type's boundary parameter is missing")
    }

    val boundaryEnd = contentType.indexOf(';', boundaryStart)
    val boundary = if (boundaryEnd == -1) {
        contentType.substring(boundaryStart + 9)
    } else {
        contentType.substring(boundaryStart + 9, boundaryEnd)
    }

    return boundary
}

@Suppress("LoopToCallChain")
private fun ByteBuffer.startsWith(prefix: ByteBuffer, prefixSkip: Int = 0): Boolean {
    val size = minOf(remaining(), prefix.remaining() - prefixSkip)
    if (size <= 0) return false

    val position = position()
    val prefixPosition = prefix.position() + prefixSkip

    for (i in 0 until size) {
        if (get(position + i) != prefix.get(prefixPosition + i)) return false
    }

    return true
}

@Suppress("LoopToCallChain")
private fun ByteBuffer.indexOfPartial(sub: ByteBuffer): Int {
    val subPosition = sub.position()
    val subSize = sub.remaining()
    val first = sub[subPosition]
    val limit = limit()

    outer@ for (idx in position() until limit) {
        if (get(idx) == first) {
            for (j in 1 until subSize) {
                if (idx + j == limit) break
                if (get(idx + j) != sub.get(subPosition + j)) continue@outer
            }
            return idx - position()
        }
    }

    return -1
}

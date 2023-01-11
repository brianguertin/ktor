/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.http.cio.internals

import io.ktor.http.cio.*
import io.ktor.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.text.toByteArray

private val CrLfCrLf = ByteArrayBuffer("\r\n\r\n".toByteArray())

/**
 * Starts a multipart parser coroutine producing multipart events
 */
internal fun CoroutineScope.parseMultipart(
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
internal fun CoroutineScope.parseMultipart(
    input: ByteReadChannel,
    contentType: CharSequence,
    contentLength: Long?
): ReceiveChannel<MultipartEvent> {
    if (!contentType.startsWith("multipart/")) {
        throw IOException("Failed to parse multipart: Content-Type should be multipart/* but it is $contentType")
    }

    val boundary = "--" + parseBoundaryInternal(contentType)
    val boundaryBuffer = ByteArrayBuffer(boundary.encodeToByteArray())

    val body = if (contentLength != null) {
        input.limited(contentLength)
    } else {
        input
    }

    return parseMultipart(boundaryBuffer, body)
}

/**
 * Starts a multipart parser coroutine producing multipart events
 */
internal fun CoroutineScope.parseMultipart(
    boundary: ReadableBuffer,
    input: ByteReadChannel
): ReceiveChannel<MultipartEvent> = produce {
    while (true) {
        val headersPacket = input.readUntil(CrLfCrLf) ?: error("Expected part headers")
        val headers: HttpHeadersMap = parseHeaders(ByteReadChannel(headersPacket))
        val body = input.readUntil(boundary) ?: Packet.Empty

        send(MultipartEvent.MultipartPart(headers, ByteReadChannel(body)))
    }
}

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
    val boundaryParameter = findBoundary(contentType)
    if (boundaryParameter == -1) {
        throw IOException("Failed to parse multipart: Content-Type's boundary parameter is missing")
    }

    val boundaryStart = boundaryParameter + 9
    var state = 0 // 0 - skipping spaces, 1 - unquoted characters, 2 - quoted no escape, 3 - quoted after escape

    val result = buildString(70) {
        loop@ for (i in boundaryStart until contentType.length) {
            val ch = contentType[i]
            val v = ch.code and 0xffff
            if (v and 0xffff > 0x7f) {
                throw IOException(
                    "Failed to parse multipart: wrong boundary byte 0x${v.toString(16)} - should be 7bit character"
                )
            }

            when (state) {
                0 -> {
                    when (ch) {
                        ' ' -> {
                            // skip space
                        }

                        '"' -> {
                            state = 2 // start quoted string parsing
                        }

                        ';', ',' -> {
                            break@loop
                        }

                        else -> {
                            state = 1
                            append(ch)
                        }
                    }
                }

                1 -> { // non-quoted string
                    if (ch == ' ' || ch == ',' || ch == ';') { // space, comma or semicolon (;)
                        break@loop
                    } else if (length < 70) {
                        append(ch)
                    } else {
                        //  RFC 2046, sec 5.1.1
                        throw IOException("Failed to parse multipart: boundary shouldn't be longer than 70 characters")
                    }
                }

                2 -> {
                    if (ch == '\\') {
                        state = 3
                    } else if (ch == '"') {
                        break@loop
                    } else if (length < 70) {
                        append(ch)
                    } else {
                        //  RFC 2046, sec 5.1.1
                        throw IOException("Failed to parse multipart: boundary shouldn't be longer than 70 characters")
                    }
                }

                3 -> {
                    if (length < 70) {
                        append(ch)
                        state = 2
                    } else {
                        //  RFC 2046, sec 5.1.1
                        throw IOException("Failed to parse multipart: boundary shouldn't be longer than 70 characters")
                    }
                }
            }
        }
    }

    if (result.isEmpty()) {
        throw IOException("Failed to parse multipart: boundary shouldn't be empty")
    }

    return result
}

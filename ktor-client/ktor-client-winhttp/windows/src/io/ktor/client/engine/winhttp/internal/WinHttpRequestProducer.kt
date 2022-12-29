/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.winhttp.internal

import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.io.*
import io.ktor.io.pool.*
import io.ktor.util.*
import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlin.collections.set
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
internal class WinHttpRequestProducer(
    private val request: WinHttpRequest,
    private val data: HttpRequestData
) {
    private val closed = atomic(false)
    private val chunked: Boolean = request.chunkedMode == WinHttpChunkedMode.Enabled && !data.isUpgradeRequest()

    fun getHeaders(): Map<String, String> {
        val headers = data.headersToMap()

        if (chunked) {
            headers[HttpHeaders.TransferEncoding] = "chunked"
        }

        return headers
    }

    suspend fun writeBody() {
        if (closed.value) return

        val requestBody = data.body.toByteChannel()
        if (requestBody != null) {
            try {
                if (chunked) {
                    writeChunkedBody(requestBody)
                } else {
                    writeRegularBody(requestBody)
                }
            } finally {
            }
        }
    }

    private suspend fun writeChunkedBody(requestBody: ByteReadChannel) {
        while (requestBody.awaitBytes()) {
            val bytes = requestBody.readBuffer().toByteArray()
            writeBodyChunk(bytes)
        }
        chunkTerminator.usePinned { src ->
            request.writeData(src, chunkTerminator.size)
        }
    }

    private suspend fun writeBodyChunk(readBuffer: ByteArray) {
        // Write chunk length
        val chunkStart = "${readBuffer.size.toString(16)}\r\n".toByteArray()
        chunkStart.usePinned { src ->
            request.writeData(src, chunkStart.size)
        }
        // Write chunk data
        readBuffer.usePinned { src ->
            request.writeData(src, readBuffer.size)
        }
        // Write chunk ending
        chunkEnd.usePinned { src ->
            request.writeData(src, chunkEnd.size)
        }
    }

    private suspend fun writeRegularBody(requestBody: ByteReadChannel) {
        while (requestBody.awaitBytes()) {
            val buffer = requestBody.readBuffer().toByteArray()
            buffer.usePinned { src ->
                request.writeData(src, buffer.size)
            }
        }
    }

    private fun HttpRequestData.headersToMap(): MutableMap<String, String> {
        val result = mutableMapOf<String, String>()

        mergeHeaders(headers, body) { key, value ->
            result[key] = value
        }

        return result
    }

    private suspend fun OutgoingContent.toByteChannel(): ByteReadChannel? = when (this) {
        is OutgoingContent.ByteArrayContent -> ByteReadChannel(bytes())
        is OutgoingContent.WriteChannelContent -> GlobalScope.writer(coroutineContext) {
            writeTo(this)
        }
        is OutgoingContent.ReadChannelContent -> readFrom()
        is OutgoingContent.NoContent -> null
        else -> throw UnsupportedContentTypeException(this)
    }

    companion object {
        private val chunkEnd = "\r\n".toByteArray()
        private val chunkTerminator = "0\r\n\r\n".toByteArray()
    }
}

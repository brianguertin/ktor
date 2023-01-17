/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache

import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.io.*
import io.ktor.util.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.apache.http.*
import org.apache.http.HttpHeaders
import org.apache.http.HttpRequest
import org.apache.http.client.config.*
import org.apache.http.client.methods.*
import org.apache.http.client.utils.*
import org.apache.http.entity.*
import org.apache.http.nio.*
import org.apache.http.nio.protocol.*
import org.apache.http.protocol.*
import java.nio.*
import kotlin.coroutines.*

@OptIn(InternalAPI::class)
internal class ApacheRequestProducer(
    private val requestData: HttpRequestData,
    private val config: ApacheEngineConfig,
    callContext: CoroutineContext
) : HttpAsyncRequestProducer, CoroutineScope {

    private val request: HttpUriRequest = setupRequest()

    private val host = URIUtils.extractHost(request.uri)
        ?: throw IllegalArgumentException("Cannot extract host from URL ${request.uri}")

    private val interestController = InterestControllerHolder()

    private val producerJob = Job()
    override val coroutineContext: CoroutineContext = callContext + producerJob

    @OptIn(DelicateCoroutinesApi::class)
    private val channel: ByteReadChannel = when (val body = requestData.body) {
        is OutgoingContent.ByteArrayContent -> ByteReadChannel(body.bytes())
        is OutgoingContent.ProtocolUpgrade -> throw UnsupportedContentTypeException(body)
        is OutgoingContent.NoContent -> ByteReadChannel.Empty
        is OutgoingContent.ReadChannelContent -> body.readFrom()
        is OutgoingContent.WriteChannelContent -> GlobalScope.writer(callContext) {
            body.writeTo(this)
        }
    }

    init {
        producerJob.invokeOnCompletion { cause ->
            channel.cancel(cause)
        }
    }

    override fun isRepeatable(): Boolean = true

    override fun getTarget(): HttpHost = host

    override fun generateRequest(): HttpRequest = request

    override fun requestCompleted(context: HttpContext) {
    }

    override fun resetRequest() {}

    override fun failed(cause: Exception) {
        val mappedCause = mapCause(cause, requestData)
        channel.cancel(mappedCause)
        producerJob.completeExceptionally(mappedCause)
    }

    @Volatile
    var closed = false

    override fun produceContent(encoder: ContentEncoder, ioctrl: IOControl) {
        if (closed) {
            encoder.complete()
            return
        }

        val cause = channel.closedCause
        if (cause != null) {
            throw cause
        }

        if (interestController.outputSuspended) return
        if (channel.readablePacket.isEmpty) {
            interestController.suspendOutput(ioctrl)
            awaitBytesInChannel()
            return
        }

        while (channel.readablePacket.isNotEmpty) {
            val buffer = channel.readablePacket.readBuffer().readByteBuffer()
            var result: Int
            do {
                result = encoder.write(buffer)
            } while (buffer.hasRemaining())

            if (result == -1) {
                encoder.complete()
                closed = true
                return
            }
        }
    }

    private val waiting = atomic(false)
    private fun awaitBytesInChannel() {
        if (!waiting.compareAndSet(expect = false, update = true)) return
        launch {
            try {
                closed = !channel.awaitBytes()
            } finally {
                interestController.resumeOutputIfPossible()
            }
        }.invokeOnCompletion {
            waiting.value = false
        }
    }

    override fun close() {
        channel.cancel()
        producerJob.complete()
    }

    private fun setupRequest(): HttpUriRequest = with(requestData) {
        val builder = RequestBuilder.create(method.value)!!
        builder.uri = url.toURI()

        val content = requestData.body
        var length: String? = null
        var type: String? = null

        mergeHeaders(headers, content) { key, value ->
            when (key) {
                HttpHeaders.CONTENT_LENGTH -> length = value
                HttpHeaders.CONTENT_TYPE -> type = value
                else -> builder.addHeader(key, value)
            }
        }

        if ((method != HttpMethod.Get && method != HttpMethod.Head) || body !is OutgoingContent.NoContent) {
            builder.entity = BasicHttpEntity().apply {
                val lengthResult = length
                if (lengthResult.isNullOrBlank()) {
                    isChunked = true
                } else {
                    contentLength = lengthResult.toLong()
                }

                setContentType(type)
            }
        }

        with(config) {
            builder.config = RequestConfig.custom()
                .setRedirectsEnabled(followRedirects)
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .customRequest()
                .setupTimeoutAttributes(requestData)
                .build()
        }

        return builder.build()
    }
}

@OptIn(InternalAPI::class)
private fun RequestConfig.Builder.setupTimeoutAttributes(requestData: HttpRequestData): RequestConfig.Builder = also {
    requestData.getCapabilityOrNull(HttpTimeout)?.let { timeoutAttributes ->
        timeoutAttributes.connectTimeoutMillis?.let { setConnectTimeout(convertLongTimeoutToIntWithInfiniteAsZero(it)) }
        timeoutAttributes.socketTimeoutMillis?.let { setSocketTimeout(convertLongTimeoutToIntWithInfiniteAsZero(it)) }
    }
}

/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache

import io.ktor.client.request.*
import io.ktor.io.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import org.apache.http.*
import org.apache.http.nio.*
import org.apache.http.nio.protocol.*
import org.apache.http.protocol.*
import java.nio.*
import kotlin.coroutines.*

@OptIn(InternalCoroutinesApi::class)
internal class ApacheResponseConsumer(
    parentContext: CoroutineContext,
    private val requestData: HttpRequestData
) : HttpAsyncResponseConsumer<Unit>, CoroutineScope {
    private val interestController = InterestControllerHolder()

    private val consumerJob = Job(parentContext[Job])
    override val coroutineContext: CoroutineContext = parentContext + consumerJob

    private val responseDeferred = CompletableDeferred<HttpResponse>()
    private val channel = ConflatedByteChannel()

    val responseChannel: ByteReadChannel get() = channel

    init {
        coroutineContext[Job]?.invokeOnCompletion(onCancelling = true) { cause ->
            if (cause != null) {
                responseDeferred.completeExceptionally(cause)
                responseChannel.cancel(cause)
            }
        }
    }

    override fun consumeContent(decoder: ContentDecoder, ioctrl: IOControl) {
        var result: Int
        do {
            val buffer = ByteBuffer.allocate(8192)
            result = decoder.read(buffer)
            if (result > 0) {
                buffer.flip()
                channel.writeByteBuffer(buffer)
            }
        } while (result > 0)
        runBlocking { channel.flush() }

        if (result < 0 || decoder.isCompleted) {
            close()
            return
        }
    }

    override fun failed(cause: Exception) {
        val mappedCause = mapCause(cause, requestData)
        consumerJob.completeExceptionally(mappedCause)
        responseDeferred.completeExceptionally(mappedCause)
        responseChannel.cancel(mappedCause)
    }

    override fun cancel(): Boolean {
        return true
    }

    override fun close() {
        channel.close()
        consumerJob.complete()
    }

    override fun getException(): Exception? = channel.closedCause as? Exception

    override fun getResult() {
    }

    override fun isDone(): Boolean = channel.isClosedForWrite

    override fun responseCompleted(context: HttpContext) {
    }

    override fun responseReceived(response: HttpResponse) {
        responseDeferred.complete(response)
    }

    suspend fun waitForResponse(): HttpResponse = responseDeferred.await()
}

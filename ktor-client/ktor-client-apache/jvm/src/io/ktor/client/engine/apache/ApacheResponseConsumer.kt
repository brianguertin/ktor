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

private val WINDOW_SIZE = 8 * 1024

@OptIn(InternalCoroutinesApi::class)
internal class ApacheResponseConsumer(
    parentContext: CoroutineContext,
    private val requestData: HttpRequestData
) : HttpAsyncResponseConsumer<Unit>, CoroutineScope, ByteReadChannel {
    private val interestController = InterestControllerHolder()
    private val closed = atomic(false)

    override var closedCause: Throwable? = null
        private set

    private val ioThreadPacket = Packet()
    override val readablePacket: Packet = Packet()

    private val consumerJob = Job(parentContext[Job])
    override val coroutineContext: CoroutineContext = parentContext + consumerJob

    private val responseDeferred = CompletableDeferred<HttpResponse>()

    init {
        coroutineContext[Job]?.invokeOnCompletion(onCancelling = true) { cause ->
            if (cause != null) {
                responseDeferred.completeExceptionally(cause)
                cancel(cause)
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
                synchronized(ioThreadPacket) {
                    ioThreadPacket.writeByteBuffer(buffer)
                }
            }
        } while (result > 0)

        if (result < 0 || decoder.isCompleted) {
            close()
            return
        }

        synchronized(ioThreadPacket) {
            if (ioThreadPacket.availableForRead > WINDOW_SIZE) {
                interestController.suspendInput(ioctrl)
            }
        }
    }

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        flushToChannel()
        while (!isDone && !predicate()) {
            flushToChannel()
        }

        return readablePacket.isNotEmpty
    }

    private fun flushToChannel() {
        synchronized(ioThreadPacket) {
            if (ioThreadPacket.availableForRead > 0) {
                readablePacket.writePacket(ioThreadPacket)
            }
        }

        interestController.resumeInputIfPossible()
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (closed.compareAndSet(expect = false, update = true)) {
            responseDeferred.completeExceptionally(cause ?: CancellationException())
            closedCause = cause
            return true
        }

        return false
    }

    override fun failed(cause: Exception) {
        val mappedCause = mapCause(cause, requestData)
        consumerJob.completeExceptionally(mappedCause)
        responseDeferred.completeExceptionally(mappedCause)
        cancel(mappedCause)
    }

    override fun cancel(): Boolean {
        return true
    }

    override fun close() {
        closed.value = true
        consumerJob.complete()
    }

    override fun getException(): Exception? = closedCause as? Exception

    override fun getResult() {
    }

    override fun isDone(): Boolean = closed.value

    override fun responseCompleted(context: HttpContext) {
    }

    override fun responseReceived(response: HttpResponse) {
        responseDeferred.complete(response)
    }

    suspend fun waitForResponse(): HttpResponse = responseDeferred.await()
}

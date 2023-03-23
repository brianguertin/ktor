/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.apache5

import io.ktor.io.*
import kotlinx.coroutines.*
import org.apache.hc.core5.concurrent.*
import org.apache.hc.core5.http.*
import org.apache.hc.core5.http.nio.*
import org.apache.hc.core5.http.protocol.*
import java.nio.*

internal class BasicResponseConsumer(
    private val dataConsumer: ApacheResponseConsumer
) : AsyncResponseConsumer<Unit>, ByteReadChannel {

    internal val responseDeferred = CompletableDeferred<HttpResponse>()

    override fun consumeResponse(
        response: HttpResponse,
        entityDetails: EntityDetails?,
        httpContext: HttpContext,
        resultCallback: FutureCallback<Unit>
    ) {
        responseDeferred.complete(response)
        if (entityDetails != null) {
            dataConsumer.streamStart(
                entityDetails,
                object : CallbackContribution<Unit>(resultCallback) {
                    override fun completed(body: Unit) {
                        resultCallback.completed(Unit)
                    }
                }
            )
        } else {
            dataConsumer.close()
            resultCallback.completed(Unit)
        }
    }

    override fun informationResponse(response: HttpResponse, httpContext: HttpContext) {
    }

    override fun updateCapacity(capacityChannel: CapacityChannel) {
        dataConsumer.updateCapacity(capacityChannel)
    }

    override fun consume(src: ByteBuffer) {
        dataConsumer.consume(src)
    }

    override fun streamEnd(trailers: List<Header>?) {
        dataConsumer.streamEnd(trailers)
    }

    override fun failed(cause: Exception) {
        responseDeferred.completeExceptionally(cause)
        dataConsumer.failed(cause)
    }

    override fun releaseResources() {
        dataConsumer.releaseResources()
    }
}

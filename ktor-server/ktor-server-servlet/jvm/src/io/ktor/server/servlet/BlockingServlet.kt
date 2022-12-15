/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.server.servlet

import io.ktor.http.content.*
import io.ktor.io.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.util.cio.*
import kotlinx.coroutines.*
import javax.servlet.*
import javax.servlet.http.*
import kotlin.coroutines.*

internal class BlockingServletApplicationCall(
    application: Application,
    servletRequest: HttpServletRequest,
    servletResponse: HttpServletResponse,
    override val coroutineContext: CoroutineContext,
    managedByEngineHeaders: Set<String> = emptySet()
) : BaseApplicationCall(application), CoroutineScope {
    override val request: BaseApplicationRequest = BlockingServletApplicationRequest(this, servletRequest)
    override val response: BlockingServletApplicationResponse =
        BlockingServletApplicationResponse(this, servletResponse, coroutineContext, managedByEngineHeaders)

    init {
        putResponseAttribute()
        putServletAttributes(servletRequest)
    }
}

private class BlockingServletApplicationRequest(
    call: ApplicationCall,
    servletRequest: HttpServletRequest
) : ServletApplicationRequest(call, servletRequest) {

    private val inputStreamChannel by lazy {
        servletRequest.inputStream.toByteReadChannel(context = UnsafeBlockingTrampoline, pool = KtorDefaultPool)
    }

    override fun receiveChannel() = inputStreamChannel
}

internal class BlockingServletApplicationResponse(
    call: ApplicationCall,
    servletResponse: HttpServletResponse,
    override val coroutineContext: CoroutineContext,
    managedByEngineHeaders: Set<String> = emptySet()
) : ServletApplicationResponse(call, servletResponse, managedByEngineHeaders), CoroutineScope {
    override fun createResponseJob(): ByteWriteChannel = reader(UnsafeBlockingTrampoline) {
        writeLoop(this, servletResponse.outputStream)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun writeLoop(from: ByteReadChannel, output: ServletOutputStream) {
        while (!from.isClosedForRead) {
            if (from.availableForRead == 0) from.awaitBytes()

            val buffer = from.readablePacket.readBuffer().toByteArray()
            if (buffer.isEmpty()) break
            try {
                output.write(buffer)
                output.flush()
            } catch (cause: Throwable) {
                throw ChannelWriteException("Failed to write to ServletOutputStream", cause)
            }
        }
    }

    override suspend fun respondUpgrade(upgrade: OutgoingContent.ProtocolUpgrade) {
        @Suppress("BlockingMethodInNonBlockingContext")
        servletResponse.sendError(501, "Upgrade is not supported in synchronous servlets")
    }
}

/**
 * Never do like this! Very special corner-case.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private object UnsafeBlockingTrampoline : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = true

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}

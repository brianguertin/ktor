// ktlint-disable filename
/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.plugins.doublereceive

import io.ktor.io.*
import io.ktor.io.pool.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal class MemoryCache(
    val body: ByteReadChannel,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : DoubleReceiveCache {
    private var fullBody: ByteArray? = null
    private var cause: Throwable? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val reader: ByteReadChannel = GlobalScope.writer(coroutineContext) {
        val packet = Packet()
        while (!body.isClosedForRead) {
            val buffer = body.readBuffer()
            if (buffer.isEmpty) break
            packet.writeBuffer(buffer.clone())

            writeBuffer(buffer)
        }

        if (body.closedCause != null) {
            cause = body.closedCause
            close(body.closedCause)
        }

        fullBody = packet.toByteArray()
    }

    override fun read(): ByteReadChannel {
        val currentCause = cause
        if (currentCause != null) {
            return ByteReadChannel {
                close(currentCause)
            }
        }

        return fullBody?.let { ByteReadChannel(it) } ?: reader
    }

    override fun dispose() {
    }
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.jvm.*

public class ConflatedByteChannel : ByteReadChannel, ByteWriteChannel {
    @Volatile
    private var closedToken: ClosedCause? = null
    private val closing = atomic(false)

    private val channel = Channel<Packet>()

    override val isClosedForRead: Boolean
        get() = closedToken != null && readablePacket.isEmpty

    override val isClosedForWrite: Boolean
        get() = closing.value

    override val closedCause: Throwable?
        get() = closedToken?.cause

    override val readablePacket: Packet = Packet()

    override val writablePacket: Packet = Packet()

    override suspend fun flush() {
        if (writablePacket.isEmpty) return
        channel.send(writablePacket.steal())
    }

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        while (!predicate()) {
            val value = channel.receiveCatching()
            when {
                value.isClosed -> {
                    closedToken = ClosedCause(value.exceptionOrNull())
                    return false
                }

                value.isFailure -> {
                    closedToken = ClosedCause(value.exceptionOrNull())
                    throw value.exceptionOrNull()!!
                }

                value.isSuccess -> {
                    readablePacket.writePacket(value.getOrThrow())
                    return true
                }
            }
        }

        return true
    }

    override fun cancel(cause: Throwable?): Boolean = close(cause)

    @OptIn(DelicateCoroutinesApi::class)
    override fun close(cause: Throwable?): Boolean {
        if (!closing.compareAndSet(false, true)) return false

        // TODO: use IO dispatcher
        GlobalScope.launch(Dispatchers.Default) {
            if (cause != null) {
                channel.close(cause)
            } else {
                flush()
                channel.close()
            }
            closedToken = ClosedCause(cause)
        }

        return true
    }
}

internal class ClosedCause(val cause: Throwable? = null)

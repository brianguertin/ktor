/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.channels.*

public class ConflatedByteChannel : ByteReadChannel, ByteWriteChannel {
    private val closed = atomic<ClosedCause?>(null)
    private val channel = Channel<Packet>()

    override val isClosedForRead: Boolean
        get() = closed.value != null && readablePacket.isEmpty

    override val isClosedForWrite: Boolean
        get() = closed.value != null

    override val closedCause: Throwable?
        get() = closed.value?.cause

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
                    closed.value = ClosedCause(value.exceptionOrNull())
                    return false
                }

                value.isFailure -> {
                    closed.value = ClosedCause(value.exceptionOrNull())
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

    override fun cancel(cause: Throwable?): Boolean {
        if (closed.compareAndSet(null, ClosedCause(cause))) {
            channel.cancel(kotlinx.coroutines.CancellationException("ConflatedByteChannel is cancelled", cause))
            writablePacket.close()
            return true
        }

        return false
    }

    override fun close(cause: Throwable?): Boolean {
        if (closed.compareAndSet(null, ClosedCause(cause))) {
            channel.close()
            return true
        }
        return false
    }
}

internal class ClosedCause(val cause: Throwable? = null)

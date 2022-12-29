/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlin.jvm.*

public class ConflatedByteChannel : ByteReadChannel, ByteWriteChannel {
    @Volatile
    private var closedToken: ClosedCause? = null
    private val closing = atomic(false)

    private val channel = Channel<Packet>()

    override val isClosedForWrite: Boolean
        get() = closing.value

    override val closedCause: Throwable?
        get() = closedToken?.cause

    override val readablePacket: Packet = Packet()

    override val writablePacket: Packet = Packet()

    private val closeStackTrace = atomic<String?>(null)

    override suspend fun flush() {
        if (writablePacket.isEmpty) return
        try {
            channel.send(writablePacket.steal())
        } catch (_: Throwable) {
            closedCause?.let { throw it }
        }
    }

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        while (!predicate()) {
            val value = channel.receiveCatching()
            when {
                value.isClosed -> {
                    val cause = value.exceptionOrNull()
                    if (cause != null) {
                        readablePacket.close()
                    }
                    closedToken = ClosedCause(cause)
                    return false
                }

                value.isFailure -> {
                    readablePacket.close()
                    val cause = value.exceptionOrNull()
                    closedToken = ClosedCause(cause)
                    throw cause ?: IllegalStateException("Internal error: cause is null")
                }

                value.isSuccess -> {
                    readablePacket.writePacket(value.getOrThrow())
                    return true
                }
            }
        }

        return closedToken == null || readablePacket.isNotEmpty
    }

    override fun cancel(cause: Throwable?): Boolean = close(cause)

    @OptIn(DelicateCoroutinesApi::class)
    override fun close(cause: Throwable?): Boolean {
        if (!closing.compareAndSet(false, true)) return false
        closeStackTrace.value = Exception().stackTraceToString()
        closedToken = ClosedCause(cause)

        // TODO: use IO dispatcher
        GlobalScope.launch(Dispatchers.Default) {
            if (cause != null) {
                writablePacket.close()
                channel.close(cause)
            } else {
                flush()
                channel.close()
            }
        }

        return true
    }
}

internal class ClosedCause(val cause: Throwable? = null)

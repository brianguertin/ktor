/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.io.*
import io.ktor.network.selector.*
import io.ktor.network.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.pool.*
import kotlinx.coroutines.*
import java.nio.*
import java.nio.channels.*

internal fun CoroutineScope.attachForWritingDirectImpl(
    nioChannel: WritableByteChannel,
    selectable: Selectable,
    selector: SelectorManager,
    socketOptions: SocketOptions.TCPClientSocketOptions? = null
): ByteWriteChannel = object : ByteWriteChannel {
    init {
        selectable.interestOp(SelectInterest.WRITE, false)
    }

    override var isClosedForWrite: Boolean = false
        private set

    override var closedCause: Throwable? = null
        private set

    override val writablePacket: Packet = Packet()

    override fun close(cause: Throwable?): Boolean {
        if (isClosedForWrite || closedCause != null) return false
        isClosedForWrite = true
        closedCause = cause

        runBlocking {
            flush()
        }

        selectable.interestOp(SelectInterest.WRITE, false)
        if (nioChannel !is SocketChannel) return true

        try {
            if (java7NetworkApisAvailable) {
                nioChannel.shutdownOutput()
            } else {
                nioChannel.socket().shutdownOutput()
            }
        } catch (ignore: ClosedChannelException) {
        }

        return true
    }

    // TODO: timeouts
    override suspend fun flush() {
        check(nioChannel is GatheringByteChannel)
        if (writablePacket.availableForRead == 0) return

        val data = writablePacket.readByteBuffers()
        var total = data.sumOf { it.remaining() }
        do {
            val rc = nioChannel.write(data)
            if (rc == 0L) {
                selectable.interestOp(SelectInterest.WRITE, true)
                selector.select(selectable, SelectInterest.WRITE)
            }
            total -= rc.toInt()
        } while (total > 0)
    }
}

private fun Packet.readByteBuffers(): Array<ByteBuffer> {
    return readBuffers().map { it.readByteBuffer() }.toTypedArray()
}

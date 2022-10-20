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

internal fun CoroutineScope.attachForWritingImpl(
    nioChannel: WritableByteChannel,
    selectable: Selectable,
    selector: SelectorManager,
    pool: ObjectPool<ByteBuffer>,
    socketOptions: SocketOptions.TCPClientSocketOptions? = null
): ByteWriteChannel {
    return reader(Dispatchers.Unconfined + CoroutineName("cio-to-nio-writer")) {
        try {
            val timeout = if (socketOptions?.socketTimeout != null) {
                createTimeout("writing", socketOptions.socketTimeout) {
                    channel.cancel(SocketTimeoutException())
                }
            } else {
                null
            }

            while (!channel.isClosedForRead || channel.awaitBytes()) {
                val buffer = channel.readAvailable()
                buffer.flip()

                while (buffer.hasRemaining()) {
                    var rc: Int

                    timeout.withTimeout {
                        do {
                            rc = nioChannel.write(buffer)
                            if (rc == 0) {
                                selectable.interestOp(SelectInterest.WRITE, true)
                                selector.select(selectable, SelectInterest.WRITE)
                            }
                        } while (buffer.hasRemaining() && rc == 0)
                    }

                    selectable.interestOp(SelectInterest.WRITE, false)
                }
            }
            timeout?.finish()
        } finally {
            if (nioChannel is SocketChannel) {
                try {
                    if (java7NetworkApisAvailable) {
                        nioChannel.shutdownOutput()
                    } else {
                        nioChannel.socket().shutdownOutput()
                    }
                } catch (ignore: ClosedChannelException) {
                }
            }
        }
    }
}

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
        while (writablePacket.isNotEmpty) {
            val buffer = writablePacket.readBuffer()

            val data: ByteBuffer = buffer.readByteBuffer()
            if (!data.hasRemaining()) continue
            var rc = 0

            do {
                rc = nioChannel.write(data)
                if (rc == 0) {
                    selectable.interestOp(SelectInterest.WRITE, true)
                    selector.select(selectable, SelectInterest.WRITE)
                }
            } while (data.hasRemaining() && rc == 0)
        }
    }
}

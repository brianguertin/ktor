/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.io.*
import io.ktor.network.selector.*
import io.ktor.network.util.*
import kotlinx.coroutines.*
import java.nio.channels.*

internal fun CoroutineScope.attachForReadingDirectImpl(
    nioChannel: ReadableByteChannel,
    selectable: Selectable,
    selector: SelectorManager,
    socketOptions: SocketOptions.TCPClientSocketOptions? = null
): ByteReadChannel = writer(Dispatchers.IO + CoroutineName("cio-from-nio-reader")) {
    try {
        selectable.interestOp(SelectInterest.READ, false)

        val timeout = if (socketOptions?.socketTimeout != null) {
            createTimeout("reading-direct", socketOptions.socketTimeout) {
                close(SocketTimeoutException())
            }
        } else {
            null
        }

        while (!isClosedForWrite) {
            timeout.withTimeout {
                val rc = readFrom(nioChannel)

                if (rc == -1) {
                    flush()
                    close()
                    return@withTimeout
                }

                if (rc > 0) return@withTimeout

                while (true) {
                    flush()
                    selectForRead(selectable, selector)
                    if (readFrom(nioChannel) != 0) break
                }
            }
        }

        timeout?.finish()
        closedCause?.let { throw it }
        flush()
        close()
    } finally {
        if (nioChannel is SocketChannel) {
            try {
                if (java7NetworkApisAvailable) {
                    nioChannel.shutdownInput()
                } else {
                    nioChannel.socket().shutdownInput()
                }
            } catch (ignore: ClosedChannelException) {
            }
        }
    }
}

private fun ByteWriteChannel.readFrom(nioChannel: ReadableByteChannel): Int {
    var count = 0
    write { buffer ->
        count = nioChannel.read(buffer)
    }

    return count
}

private suspend fun selectForRead(selectable: Selectable, selector: SelectorManager) {
    selectable.interestOp(SelectInterest.READ, true)
    selector.select(selectable, SelectInterest.READ)
}

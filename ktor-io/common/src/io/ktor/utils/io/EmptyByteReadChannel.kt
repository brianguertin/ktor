package io.ktor.utils.io

import io.ktor.io.*

internal object EmptyByteReadChannel : ByteReadChannel {
    override val isClosedForRead: Boolean = true
    override val closedCause: Throwable? = null
    override val readablePacket: Packet = Packet()

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        return false
    }

    override fun cancel(cause: Throwable?): Boolean {
        return false
    }
}

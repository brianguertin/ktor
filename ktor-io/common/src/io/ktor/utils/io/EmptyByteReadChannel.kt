package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*

internal object EmptyByteReadChannel : ByteReadChannel {
    override val availableForRead: Int = 0
    override val isClosedForRead: Boolean = true
    override val isClosedForWrite: Boolean = true
    override val closedCause: Throwable? = null
    override val totalBytesRead: Long = 0

    override suspend fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int {
        return -1
    }

    override suspend fun readAvailable(dst: ChunkBuffer): Int {
        return -1
    }

    override suspend fun readFully(dst: ByteArray, offset: Int, length: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun readFully(dst: ChunkBuffer, n: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun readPacket(size: Int): ByteReadPacket {
        TODO("Not yet implemented")
    }

    override suspend fun readRemaining(limit: Long): ByteReadPacket {
        return ByteReadPacket.Empty
    }

    override suspend fun readLong(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun readInt(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun readShort(): Short {
        TODO("Not yet implemented")
    }

    override suspend fun readByte(): Byte {
        TODO("Not yet implemented")
    }

    override suspend fun readBoolean(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun readDouble(): Double {
        TODO("Not yet implemented")
    }

    override suspend fun readFloat(): Float {
        TODO("Not yet implemented")
    }

    override fun readSession(consumer: ReadSession.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun readSuspendableSession(consumer: suspend SuspendableReadSession.() -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun <A : Appendable> readUTF8LineTo(out: A, limit: Int): Boolean {
        return false
    }

    override suspend fun readUTF8Line(limit: Int): String? {
        return null
    }

    override fun cancel(cause: Throwable?): Boolean {
        return false
    }

    override suspend fun discard(max: Long): Long {
        return 0
    }

    override suspend fun awaitContent() {
    }

    override suspend fun peekTo(
        destination: Memory,
        destinationOffset: Long,
        offset: Long,
        min: Long,
        max: Long
    ): Long {
        return 0
    }
}

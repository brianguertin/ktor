package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.internal.*
import java.nio.*

public actual suspend fun ByteReadChannel.joinTo(dst: ByteWriteChannel, closeOnEnd: Boolean) {
    require(dst !== this)
    return joinToImplSuspend(dst, closeOnEnd)
}

private suspend fun ByteReadChannel.joinToImplSuspend(dst: ByteWriteChannel, close: Boolean) {
    copyTo(dst, Long.MAX_VALUE)
    if (close) {
        dst.close()
    } else {
        dst.flush()
    }
}

/**
 * Reads up to [limit] bytes from receiver channel and writes them to [dst] channel.
 * Closes [dst] channel if fails to read or write with cause exception.
 * @return a number of copied bytes
 */
public actual suspend fun ByteReadChannel.copyTo(dst: ByteWriteChannel, limit: Long): Long {
    require(this !== dst)

    if (limit == 0L) {
        return 0L
    }

    if (this is ByteBufferChannel && dst is ByteBufferChannel) {
        return dst.copyDirect(this, limit, null)
    } else if (this is ByteChannelSequentialBase && dst is ByteChannelSequentialBase) {
        return copyToSequentialImpl(dst, Long.MAX_VALUE) // more specialized extension function
    }

    return copyToImpl(dst, limit)
}

private suspend fun ByteReadChannel.copyToImpl(dst: ByteWriteChannel, limit: Long): Long {
    val buffer = ChunkBuffer.Pool.borrow()
    val dstNeedsFlush = !dst.autoFlush

    try {
        var copied = 0L

        while (true) {
            val remaining = limit - copied
            if (remaining == 0L) break
            buffer.resetForWrite(minOf(buffer.capacity.toLong(), remaining).toInt())

            val size = readAvailable(buffer)
            if (size == -1) break

            dst.writeFully(buffer)
            copied += size

            if (dstNeedsFlush && availableForRead == 0) {
                dst.flush()
            }
        }
        return copied
    } catch (t: Throwable) {
        dst.close(t)
        throw t
    } finally {
        buffer.release(ChunkBuffer.Pool)
    }
}

/**
 * TODO
 * Reads all the bytes from receiver channel and builds a packet that is returned unless the specified [limit] exceeded.
 * It will simply stop reading and return packet of size [limit] in this case
 */
/*suspend fun ByteReadChannel.readRemaining(limit: Int = Int.MAX_VALUE): ByteReadPacket {
    val buffer = JavaNioAccess.BufferPool.borrow()
    val packet = WritePacket()

    try {
        var copied = 0L

        while (copied < limit) {
            buffer.clear()
            if (limit - copied < buffer.limit()) {
                buffer.limit((limit - copied).toInt())
            }
            val size = readAvailable(buffer)
            if (size == -1) break

            buffer.flip()
            packet.writeFully(buffer)
            copied += size
        }

        return packet.build()
    } catch (t: Throwable) {
        packet.release()
        throw t
    } finally {
        JavaNioAccess.BufferPool.recycle(buffer)
    }
}*/

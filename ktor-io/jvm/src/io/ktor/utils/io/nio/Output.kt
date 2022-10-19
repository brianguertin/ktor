package io.ktor.utils.io.nio

import io.ktor.utils.io.bits.DROP_Memory
import io.ktor.utils.io.bits.sliceSafe
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import java.nio.channels.*

private class ChannelAsOutput(
    pool: ObjectPool<DROP_ChunkBuffer>,
    val channel: WritableByteChannel
) : DROP_Output(pool) {
    override fun flush(source: DROP_Memory, offset: Int, length: Int) {
        val slice = source.buffer.sliceSafe(offset, length)
        while (slice.hasRemaining()) {
            channel.write(slice)
        }
    }

    override fun closeDestination() {
        channel.close()
    }
}

public fun WritableByteChannel.asOutput(
    pool: ObjectPool<DROP_ChunkBuffer> = DROP_ChunkBuffer.Pool
): DROP_Output = ChannelAsOutput(pool, this)

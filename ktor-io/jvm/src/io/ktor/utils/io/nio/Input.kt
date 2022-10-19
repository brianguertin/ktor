package io.ktor.utils.io.nio

import io.ktor.utils.io.bits.DROP_Memory
import io.ktor.utils.io.bits.sliceSafe
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import java.nio.channels.*
import kotlin.require

private class ChannelAsInput(
    private val channel: ReadableByteChannel,
    pool: ObjectPool<DROP_ChunkBuffer>
) : DROP_Input(pool = pool) {
    init {
        require(channel !is SelectableChannel || !channel.isBlocking) { "Non-blocking channels are not supported" }
    }

    override fun fill(destination: DROP_Memory, offset: Int, length: Int): Int {
        return channel.read(destination.buffer.sliceSafe(offset, length)).coerceAtLeast(0)
    }

    override fun closeSource() {
        channel.close()
    }
}

public fun ReadableByteChannel.asInput(
    pool: ObjectPool<DROP_ChunkBuffer> = DROP_ChunkBuffer.Pool
): DROP_Input = ChannelAsInput(this, pool)

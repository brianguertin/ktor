@file:Suppress("RedundantModalityModifier", "FunctionName")

package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*

/**
 * Read-only immutable byte packet. Could be consumed only once however it does support [copy] that doesn't copy every byte
 * but creates a new view instead. Once packet created it should be either completely read (consumed) or released
 * via [release].
 */
public class DROP_ByteReadPacket internal constructor(
    head: DROP_ChunkBuffer,
    remaining: Long,
    pool: ObjectPool<DROP_ChunkBuffer>
) : DROP_Input(head, remaining, pool) {
    public constructor(head: DROP_ChunkBuffer, pool: ObjectPool<DROP_ChunkBuffer>) : this(
        head,
        head.remainingAll(),
        pool
    )

    init {
        markNoMoreChunksAvailable()
    }

    /**
     * Returns a copy of the packet. The original packet and the copy could be used concurrently. Both need to be
     * either completely consumed or released via [release]
     */
    public final fun copy(): DROP_ByteReadPacket = DROP_ByteReadPacket(head.copyAll(), remaining, pool)

    final override fun fill(): DROP_ChunkBuffer? = null

    final override fun fill(destination: DROP_Memory, offset: Int, length: Int): Int {
        return 0
    }

    final override fun closeSource() {
    }

    override fun toString(): String {
        return "ByteReadPacket($remaining bytes remaining)"
    }

    public companion object {
        public val Empty: DROP_ByteReadPacket =
            DROP_ByteReadPacket(DROP_ChunkBuffer.Empty, 0L, DROP_ChunkBuffer.EmptyPool)
    }
}

public expect fun ByteReadPacket(
    array: ByteArray,
    offset: Int = 0,
    length: Int = array.size,
    block: (ByteArray) -> Unit
): DROP_ByteReadPacket

@Suppress("NOTHING_TO_INLINE")
public inline fun ByteReadPacket(array: ByteArray, offset: Int = 0, length: Int = array.size): DROP_ByteReadPacket {
    return ByteReadPacket(array, offset, length) {}
}

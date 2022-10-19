package io.ktor.utils.io.streams

import io.ktor.utils.io.bits.DROP_Memory
import io.ktor.utils.io.bits.storeByteArray
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import java.io.*

internal class InputStreamAsInput(
    private val stream: InputStream,
    pool: ObjectPool<DROP_ChunkBuffer>
) : DROP_Input(pool = pool) {

    override fun fill(destination: DROP_Memory, offset: Int, length: Int): Int {
        if (destination.buffer.hasArray() && !destination.buffer.isReadOnly) {
            return stream
                .read(destination.buffer.array(), destination.buffer.arrayOffset() + offset, length)
                .coerceAtLeast(0)
        }

        val buffer = ByteArrayPool.borrow()
        try {
            val rc = stream.read(buffer, 0, minOf(buffer.size, length))
            if (rc == -1) return 0
            destination.storeByteArray(offset, buffer, 0, rc)
            return rc
        } finally {
            ByteArrayPool.recycle(buffer)
        }
    }

    override fun closeSource() {
        stream.close()
    }
}

public fun InputStream.asInput(pool: ObjectPool<DROP_ChunkBuffer> = DROP_ChunkBuffer.Pool): DROP_Input = InputStreamAsInput(this, pool)

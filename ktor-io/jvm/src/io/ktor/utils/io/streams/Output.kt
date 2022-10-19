package io.ktor.utils.io.streams

import io.ktor.utils.io.bits.DROP_Memory
import io.ktor.utils.io.bits.sliceSafe
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import java.io.*

private class OutputStreamAdapter(
    pool: ObjectPool<DROP_ChunkBuffer>,
    private val stream: OutputStream
) : DROP_Output(pool) {
    override fun flush(source: DROP_Memory, offset: Int, length: Int) {
        val nioBuffer = source.buffer
        if (nioBuffer.hasArray() && !nioBuffer.isReadOnly) {
            stream.write(nioBuffer.array(), nioBuffer.arrayOffset() + offset, length)
            return
        }

        val array = ByteArrayPool.borrow()
        val slice = nioBuffer.sliceSafe(offset, length)
        try {
            do {
                val partSize = minOf(slice.remaining(), array.size)
                if (partSize == 0) break

                slice.get(array, 0, partSize)
                stream.write(array, 0, partSize)
            } while (true)
        } finally {
            ByteArrayPool.recycle(array)
        }
    }

    override fun closeDestination() {
        stream.close()
    }
}

public fun OutputStream.asOutput(): DROP_Output = OutputStreamAdapter(DROP_ChunkBuffer.Pool, this)

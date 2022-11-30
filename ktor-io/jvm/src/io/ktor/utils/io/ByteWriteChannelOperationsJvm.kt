package io.ktor.utils.io

import io.ktor.io.*
import io.ktor.utils.io.pool.*
import java.nio.*

private val TODO_POOL = ByteBufferPool()

public fun ByteWriteChannel.writeAvailable(src: ByteBuffer): Int {
    val result = src.remaining()
    writeBuffer(ByteBufferBuffer(src))
    return result
}

public fun ByteWriteChannel.writeByteBuffer(src: ByteBuffer) {
    writeBuffer(ByteBufferBuffer(src))
}

/**
 * Invokes [block] when it will be possible to write at least [min] bytes
 * providing byte buffer to it so lambda can write to the buffer
 * up to [ByteBuffer.remaining] bytes. If there are no [min] bytes spaces available then the invocation could
 * suspend until the requirement will be met.
 *
 * Warning: it is not guaranteed that all of remaining bytes will be represented as a single byte buffer
 * eg: it could be 4 bytes available for write but the provided byte buffer could have only 2 remaining bytes:
 * in this case you have to invoke write again (with decreased [min] accordingly).
 *
 * @param min amount of bytes available for write, should be positive
 * @param block to be invoked when at least [min] bytes free capacity available
 */
public fun ByteWriteChannel.write(min: Int = 1, block: (ByteBuffer) -> Unit): Int {
    val buffer = ByteBuffer.allocate(4096)
    block(buffer)

    buffer.flip()
    val result = buffer.remaining()
    if (buffer.hasRemaining()) {
        writeBuffer(ByteBufferBuffer(buffer))
    }
    return result
}

/**
 * Invokes [block] for every free buffer until it return `false`. It will also suspend every time when no free
 * space available for write.
 *
 * @param block to be invoked when there is free space available for write
 */
public suspend fun ByteWriteChannel.writeWhile(block: (ByteBuffer) -> Boolean) {
    TODO()
}

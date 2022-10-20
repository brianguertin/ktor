package io.ktor.utils.io

import io.ktor.io.*
import io.ktor.utils.io.pool.*
import java.nio.*

private val TODO_POOL = ByteBufferPool()

public suspend fun ByteWriteChannel.writeAvailable(src: ByteBuffer): Int {
    TODO()
}

public suspend fun ByteWriteChannel.writeByteBuffer(src: ByteBuffer) {
    TODO()
}

/**
 * Invokes [block] if it is possible to write at least [min] byte
 * providing byte buffer to it so lambda can write to the buffer
 * up to [ByteBuffer.remaining] bytes. If there are no [min] bytes spaces available then the invocation returns 0.
 *
 * Warning: it is not guaranteed that all of remaining bytes will be represented as a single byte buffer
 * eg: it could be 4 bytes available for write but the provided byte buffer could have only 2 remaining bytes:
 * in this case you have to invoke write again (with decreased [min] accordingly).
 *
 * @param min amount of bytes available for write, should be positive
 * @param block to be invoked when at least [min] bytes free capacity available
 *
 * @return number of consumed bytes or -1 if the block wasn't executed.
 */
public fun ByteWriteChannel.writeAvailable(min: Int = 1, block: (ByteBuffer) -> Unit): Int {
    TODO()
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
public fun ByteWriteChannel.write(min: Int = 1, block: (ByteBuffer) -> Unit) {
    val buffer = ByteBuffer.allocate(4096)
    block(buffer)

    buffer.flip()
    if (buffer.hasRemaining()) {
        writeBuffer(ByteBufferBuffer(buffer))
    }
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

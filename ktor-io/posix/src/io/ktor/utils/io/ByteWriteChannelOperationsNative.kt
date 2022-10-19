package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlinx.cinterop.*

/**
 * Invokes [block] if it is possible to write at least [min] byte
 * providing buffer to it so lambda can write to the buffer
 * up to [DROP_Buffer.writeRemaining] bytes. If there are no [min] bytes spaces available then the invocation returns -1.
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
public fun ByteWriteChannel.writeAvailable(min: Int, block: (DROP_Buffer) -> Unit): Int {
    TODO()
}

/**
 * Writes as much as possible and only suspends if buffer is full
 */
public suspend fun ByteWriteChannel.writeAvailable(src: CPointer<ByteVar>, offset: Int, length: Int): Int {
    TODO()
}

/**
 * Writes as much as possible and only suspends if buffer is full
 */
public suspend fun ByteWriteChannel.writeAvailable(src: CPointer<ByteVar>, offset: Long, length: Long): Int {
    TODO()
}

/**
 * Writes all [src] bytes and suspends until all bytes written. Causes flush if buffer filled up or when [autoFlush]
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeFully(src: CPointer<ByteVar>, offset: Int, length: Int) {
    TODO()
}

/**
 * Writes all [src] bytes and suspends until all bytes written. Causes flush if buffer filled up or when [autoFlush]
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeFully(src: CPointer<ByteVar>, offset: Long, length: Long) {
    TODO()
}

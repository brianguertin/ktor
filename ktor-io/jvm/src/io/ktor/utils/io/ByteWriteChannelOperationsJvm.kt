package io.ktor.utils.io

import java.nio.*

public suspend fun ByteWriteChannel.writeAvailable(src: ByteBuffer): Int {
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }
    joining?.let { resolveDelegation(this, it)?.let { return it.writeAvailable(src) } }

    val copied = writeAsMuchAsPossible(src)
    if (copied > 0) return copied

    joining?.let { resolveDelegation(this, it)?.let { return it.writeAvailableSuspend(src) } }
    return writeAvailableSuspend(src)
}

public suspend fun ByteWriteChannel.writeFully(src: ByteBuffer) {
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }
    joining?.let { resolveDelegation(this, it)?.let { return it.writeFully(src) } }

    writeAsMuchAsPossible(src)
    if (!src.hasRemaining()) return

    return writeFullySuspend(src)
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
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }

    require(min > 0) { "min should be positive" }
    require(min <= BYTE_BUFFER_CAPACITY) { "Min($min) shouldn't be greater than $BYTE_BUFFER_CAPACITY" }

    var result = 0
    var written = false

    writing { dst, state ->
        val locked = state.tryWriteAtLeast(min)
        if (locked <= 0) {
            return@writing
        }

        // here we have locked all remaining for write bytes
        // however we don't know how many bytes will be actually written
        // so later we have to return (locked - actuallyWritten) bytes back

        // it is important to lock bytes to fail concurrent tryLockForRelease
        // once we have locked some bytes, tryLockForRelease will fail so it is safe to use buffer

        dst.prepareBuffer(writePosition, locked)

        val position = dst.position()
        val l = dst.limit()
        block(dst)
        check(l == dst.limit()) { "Buffer limit modified" }

        result = dst.position() - position
        check(result >= 0) { "Position has been moved backward: pushback is not supported" }
        if (result < 0) throw IllegalStateException()

        dst.bytesWritten(state, result)

        if (result < locked) {
            state.completeRead(locked - result) // return back extra bytes (see note above)
            // we use completeRead in spite of that it is write block
            // we don't need to resume write as we are already in writing block
        }

        written = true
    }

    if (!written) {
        return -1
    }

    return result
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
public suspend fun ByteWriteChannel.write(min: Int = 1, block: (ByteBuffer) -> Unit) {
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }
    require(min > 0) { "min should be positive" }
    require(min <= BYTE_BUFFER_CAPACITY) { "Min($min) should'nt be greater than ($BYTE_BUFFER_CAPACITY)" }

    while (true) {
        val writeAvailable = writeAvailable(min, block)
        if (writeAvailable >= 0) {
            break
        }

        awaitFreeSpaceOrDelegate(min, block)
    }
}

/**
 * Invokes [block] for every free buffer until it return `false`. It will also suspend every time when no free
 * space available for write.
 *
 * @param block to be invoked when there is free space available for write
 */
public suspend fun ByteWriteChannel.writeWhile(block: (ByteBuffer) -> Boolean) {
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }

    if (!writeWhileNoSuspend(block)) return
    closed?.let { rethrowClosed(it.sendException) }
    return writeWhileSuspend(block)
}

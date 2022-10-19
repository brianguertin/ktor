/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.utils.io.internal.*
import java.nio.*

public suspend fun ByteReadChannel.readAvailable(dst: ByteBuffer): Int {
    if (this is EmptyByteReadChannel) return -1
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }
    val consumed = readAsMuchAsPossible(dst)

    return when {
        consumed == 0 && closed != null -> {
            if (state.capacity.flush()) {
                readAsMuchAsPossible(dst)
            } else {
                -1
            }
        }

        consumed > 0 || !dst.hasRemaining() -> consumed
        else -> readAvailableSuspend(dst)
    }
}

public suspend fun ByteReadChannel.readFully(dst: ByteBuffer): Int {
    if (this is EmptyByteReadChannel) return -1
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }

    val rc = readAsMuchAsPossible(dst)
    if (!dst.hasRemaining()) return rc
    return readFullySuspend(dst, rc)
}

/**
 * Invokes [block] if it is possible to read at least [min] byte
 * providing byte buffer to it so lambda can read from the buffer
 * up to [ByteBuffer.available] bytes. If there are no [min] bytes available then the invocation returns 0.
 *
 * Warning: it is not guaranteed that all of available bytes will be represented as a single byte buffer
 * eg: it could be 4 bytes available for read but the provided byte buffer could have only 2 available bytes:
 * in this case you have to invoke read again (with decreased [min] accordingly).
 *
 * @param min amount of bytes available for read, should be positive
 * @param block to be invoked when at least [min] bytes available
 *
 * @return number of consumed bytes or -1 if the block wasn't executed.
 */
public fun ByteReadChannel.readAvailable(min: Int = 1, block: (ByteBuffer) -> Unit): Int {
    if (this is EmptyByteReadChannel) return -1
    check(this is ByteBufferChannel) { "This method is only available for ByteBufferChannel, not for ${this::class}" }
    require(min > 0) { "min should be positive" }
    require(min <= BYTE_BUFFER_CAPACITY) { "Min($min) shouldn't be greater than $BYTE_BUFFER_CAPACITY" }

    var result = 0
    val read = reading { state ->
        val locked = state.tryReadAtLeast(min)

        if (locked <= 0 || locked < min) {
            return@reading false
        }

        // here we have locked all available for read bytes
        // however we don't know how many bytes will be actually read
        // so later we have to return (locked - actuallyRead) bytes back

        // it is important to lock bytes to fail concurrent tryLockForRelease
        // once we have locked some bytes, tryLockForRelease will fail so it is safe to use buffer

        val position = position()
        val limit = limit()
        block(this)
        check(limit == limit()) { "Buffer limit shouldn't be modified." }

        result = position() - position
        check(result >= 0) { "Position shouldn't been moved backwards." }

        bytesRead(state, result)

        if (result < locked) {
            state.completeWrite(locked - result) // return back extra bytes (see note above)
            // we use completeWrite in spite of that it is read block
            // we don't need to resume read as we are already in read block

            // flush returned bytes back for subsequent reads
            state.flush()
        }

        return@reading true
    }

    if (!read) return -1
    return result
}

/**
 * Invokes [consumer] when it will be possible to read at least [min] bytes
 * providing byte buffer to it so lambda can read from the buffer
 * up to [ByteBuffer.remaining] bytes. If there are no [min] bytes available then the invocation could
 * suspend until the requirement will be met.
 *
 * If [min] is zero then the invocation will suspend until at least one byte available.
 *
 * Warning: it is not guaranteed that all of remaining bytes will be represented as a single byte buffer
 * eg: it could be 4 bytes available for read but the provided byte buffer could have only 2 remaining bytes:
 * in this case you have to invoke read again (with decreased [min] accordingly).
 *
 * It will fail with [EOFException] if not enough bytes ([availableForRead] < [min]) available
 * in the channel after it is closed.
 *
 * [consumer] lambda should modify buffer's position accordingly. It also could temporarily modify limit however
 * it should restore it before return. It is not recommended to access any bytes of the buffer outside of the
 * provided byte range [position(); limit()) as there could be any garbage or incomplete data.
 *
 * @param min amount of bytes available for read, should be positive or zero
 * @param consumer to be invoked when at least [min] bytes available for read
 */
public suspend fun ByteReadChannel.read(min: Int = 1, consumer: (ByteBuffer) -> Unit) {
    check(this is ByteBufferChannel)
    require(min >= 0) { "min should be positive or zero" }

    val read = reading {
        val av = it.availableForRead
        if (av <= 0 || av < min) {
            return@reading false
        }

        val position = this.position()
        val oldLimit = this.limit()
        consumer(this)

        check(oldLimit == this.limit()) { "Buffer limit modified." }
        val delta = position() - position

        check(delta >= 0) { "Position has been moved backward: pushback is not supported." }
        check(it.tryReadExact(delta))

        bytesRead(it, delta)

        return@reading true
    }

    if (!read) {
        if (isClosedForRead) {
            return
        }

        readBlockSuspend(min, consumer)
    }
}

@Deprecated("Use read { } instead.")
public fun <R> ByteReadChannel.lookAhead(visitor: LookAheadSession.() -> R): R {
    check(this is ByteBufferChannel)
    closedCause?.let { return visitor(FailedLookAhead(it)) }
    if (state === ReadWriteBufferState.Terminated) {
        return visitor(TerminatedLookAhead)
    }

    var result: R? = null
    val continueReading = reading {
        result = visitor(this@lookAhead)
        true
    }

    if (!continueReading) {
        closedCause?.let { return visitor(FailedLookAhead(it)) }
        return visitor(TerminatedLookAhead)
    }

    return result!!
}

@Deprecated("Use read { } instead.")
public suspend fun <R> ByteReadChannel.lookAheadSuspend(visitor: suspend LookAheadSuspendSession.() -> R): R {
    check(this is ByteBufferChannel)
    closedCause?.let { return visitor(FailedLookAhead(it)) }
    if (state === ReadWriteBufferState.Terminated) {
        return visitor(TerminatedLookAhead)
    }

    var result: Any? = null
    val rc = reading {
        result = visitor(this@lookAheadSuspend)
        true
    }

    if (!rc) {
        closedCause?.let { return visitor(FailedLookAhead(it)) }
        if (state === ReadWriteBufferState.Terminated) {
            return visitor(TerminatedLookAhead)
        }

        try {
            result = visitor(this)
        } finally {
            val stateSnapshot = state

            if (!stateSnapshot.idle && stateSnapshot !== ReadWriteBufferState.Terminated) {
                if (stateSnapshot is ReadWriteBufferState.Reading ||
                    stateSnapshot is ReadWriteBufferState.ReadingWriting
                ) {
                    restoreStateAfterRead()
                }
                tryTerminate()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    return result as R
}

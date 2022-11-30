/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.io.*
import io.ktor.utils.io.internal.*
import java.io.*
import java.nio.*

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
    TODO()
}

public suspend fun ByteReadChannel.readAvailable(): ByteBuffer {
    if (!isClosedForRead && availableForRead == 0) awaitBytes()
    if (isClosedForRead) throw EOFException()

    return readByteBuffer()
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
    TODO()
}

@Deprecated("Use read { } instead.")
public fun <R> ByteReadChannel.lookAhead(visitor: LookAheadSession.() -> R): R {
    TODO()
}

@Deprecated("Use read { } instead.")
public suspend fun <R> ByteReadChannel.lookAheadSuspend(visitor: suspend LookAheadSuspendSession.() -> R): R {
    TODO()
}

public fun ByteReadChannel.readByteBuffer(): ByteBuffer {
    require(readablePacket.isNotEmpty)
    return readablePacket.readBuffer().readByteBuffer()
}

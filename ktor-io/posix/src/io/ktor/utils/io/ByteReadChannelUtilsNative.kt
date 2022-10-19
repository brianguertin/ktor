/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.utils.io.core.*
import kotlinx.cinterop.*

/**
 * Reads all [length] bytes to [dst] buffer or fails if channel has been closed.
 * Suspends if not enough bytes available.
 */
public suspend fun ByteReadChannel.readFully(dst: CPointer<ByteVar>, offset: Int, length: Int) {
    check(this is ByteChannelNative)
    require(offset >= 0L)
    require(length >= 0L)

    return when {
        closedCause != null -> throw closedCause!!
        readable.remaining >= length -> {
            val size = tryReadCPointer(dst, offset.toLong(), length.toLong())
            afterRead(size)
        }

        closed -> throw EOFException(
            "Channel is closed and not enough bytes available: required $length but $availableForRead available"
        )

        else -> readFullySuspend(dst, offset.toLong(), length.toLong())
    }
}

/**
 * Reads all [length] bytes to [dst] buffer or fails if channel has been closed.
 * Suspends if not enough bytes available.
 */
public suspend fun ByteReadChannel.readFully(dst: CPointer<ByteVar>, offset: Long, length: Long) {
    readFully(dst, offset.toInt(), length.toInt())
}

/**
 * Reads all available bytes to [dst] buffer and returns immediately or suspends if no bytes available
 * @return number of bytes were read or `-1` if the channel has been closed
 */
public suspend fun ByteReadChannel.readAvailable(dst: CPointer<ByteVar>, offset: Int, length: Int): Int {
    return readAvailable(dst, offset.toLong(), length.toLong())
}

/**
 * Reads all available bytes to [dst] buffer and returns immediately or suspends if no bytes available
 * @return number of bytes were read or `-1` if the channel has been closed
 */
public suspend fun ByteReadChannel.readAvailable(dst: CPointer<ByteVar>, offset: Long, length: Long): Int {
    check(this is ByteChannelNative)
    require(offset >= 0L)
    require(length >= 0L)
    closedCause?.let { throw it }
    if (closed && availableForRead == 0) return -1

    if (length == 0L) return 0

    if (availableForRead == 0) {
        awaitSuspend(1)
    }

    if (!readable.canRead()) {
        prepareFlushedBytes()
    }

    val size = tryReadCPointer(dst, offset, length)
    afterRead(size)
    return size
}

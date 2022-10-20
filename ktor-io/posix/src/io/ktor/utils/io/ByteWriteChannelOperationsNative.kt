package io.ktor.utils.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlinx.cinterop.*

public suspend fun ByteWriteChannel.writeCPointer(buffer: CPointer<ByteVarOf<Byte>>, size: ULong): Int {
    TODO("Not yet implemented")
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

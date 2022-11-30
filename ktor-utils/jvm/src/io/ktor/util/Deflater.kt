/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.util

import io.ktor.io.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.pool.*
import kotlinx.coroutines.*
import java.nio.*
import java.util.zip.*
import kotlin.coroutines.*

internal const val GZIP_MAGIC: Short = 0x8b1f.toShort()
internal val GZIP_HEADER_PADDING: ByteArray = ByteArray(7)

private fun Deflater.deflateTo(outBuffer: ByteBuffer) {
    if (outBuffer.hasRemaining()) {
        val written = deflate(outBuffer.array(), outBuffer.arrayOffset() + outBuffer.position(), outBuffer.remaining())
        outBuffer.position(outBuffer.position() + written)
    }
}

private fun Deflater.setInputBuffer(buffer: ByteBuffer) {
    require(buffer.hasArray()) { "buffer need to be array-backed" }
    setInput(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())
}

internal fun Checksum.updateKeepPosition(buffer: ByteBuffer) {
    require(buffer.hasArray()) { "buffer need to be array-backed" }
    update(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())
}

private suspend fun ByteWriteChannel.putGzipHeader() {
    writeShort(GZIP_MAGIC.reverseByteOrder())
    writeByte(Deflater.DEFLATED.toByte())
    writeByteArray(GZIP_HEADER_PADDING)
}

private suspend fun ByteWriteChannel.putGzipTrailer(crc: Checksum, deflater: Deflater) {
    writeInt(crc.value.toInt().reverseByteOrder())
    writeInt(deflater.totalIn.reverseByteOrder())
}

private fun ByteWriteChannel.deflateWhile(deflater: Deflater, predicate: () -> Boolean) {
    while (predicate()) {
        write {
            deflater.deflateTo(it)
        }
    }
}

/**
 * Does deflate compression
 * optionally doing CRC and writing GZIP header and trailer if [gzip] = `true`
 */
private suspend fun ByteReadChannel.deflateTo(
    destination: ByteWriteChannel,
    gzip: Boolean = true
) {
    val crc = CRC32()
    val deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)

    try {
        if (gzip) {
            destination.putGzipHeader()
        }

        while (true) {
            if (availableForRead == 0) {
                awaitBytes()
            }
            if (isClosedForRead) break

            val input = readAvailable()
            crc.updateKeepPosition(input)
            deflater.setInputBuffer(input)
            destination.deflateWhile(deflater) { !deflater.needsInput() }
        }

        closedCause?.let { throw it }

        deflater.finish()
        destination.deflateWhile(deflater) { !deflater.finished() }

        if (gzip) {
            destination.putGzipTrailer(crc, deflater)
        }
    } finally {
        deflater.end()
    }
}

/**
 * Launch a coroutine on [coroutineContext] that does deflate compression
 * optionally doing CRC and writing GZIP header and trailer if [gzip] = `true`
 */
@OptIn(DelicateCoroutinesApi::class)
public fun ByteReadChannel.deflated(
    gzip: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Unconfined
): ByteReadChannel = GlobalScope.writer(coroutineContext) {
    this@deflated.deflateTo(this, gzip)
}

/**
 * Launch a coroutine on [coroutineContext] that does deflate compression
 * optionally doing CRC and writing GZIP header and trailer if [gzip] = `true`
 */
@OptIn(DelicateCoroutinesApi::class)
public fun ByteWriteChannel.deflated(
    gzip: Boolean = true,
    coroutineContext: CoroutineContext = Dispatchers.Unconfined
): ByteWriteChannel = transform {
    onFlush {
    }

    onClose {
    }
}

/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

import io.ktor.io.*
import kotlinx.coroutines.*
import java.util.zip.*

private const val GZIP_HEADER_SIZE: Int = 10
private const val EXPANSION_FACTOR = 2

// GZIP header flags bits
private object GzipHeaderFlags {

    // Is ASCII
    const val FTEXT = 1 shl 0

    // Has header CRC16
    const val FHCRC = 1 shl 1

    // Extra fields present
    const val EXTRA = 1 shl 2

    // File name present
    const val FNAME = 1 shl 3

    // File comment present
    const val FCOMMENT = 1 shl 4
}

private infix fun Int.has(flag: Int) = this and flag != 0

/**
 * Implementation of Deflate [Encoder].
 */
public val Deflate: Encoder = object : Encoder {
    override fun CoroutineScope.encode(source: ByteReadChannel): ByteReadChannel =
        source.deflated(gzip = true, coroutineContext = coroutineContext)

    override fun CoroutineScope.decode(source: ByteReadChannel): ByteReadChannel =
        inflate(source, gzip = false)
}

/**
 * Implementation of GZip [Encoder].
 */
public val GZip: Encoder = object : Encoder {
    override fun CoroutineScope.encode(source: ByteReadChannel): ByteReadChannel =
        source.deflated(gzip = true, coroutineContext = coroutineContext)

    override fun CoroutineScope.decode(source: ByteReadChannel): ByteReadChannel = inflate(source)
}

private fun CoroutineScope.inflate(
    source: ByteReadChannel,
    gzip: Boolean = true
): ByteReadChannel = writer {
    val inflater = Inflater(true)
    val checksum = CRC32()

    if (gzip) {
        val header = source.readPacket(GZIP_HEADER_SIZE)
        val magic = header.readShort().reverseByteOrder()
        val format = header.readByte()
        val flags = header.readByte().toInt()

        // Next parts of the header are not used for now,
        // uncomment the following lines once you need them

        // val time = header.readInt()
        // val extraFlags = header.readByte()
        // val osType = header.readByte()

        // however we have to discard them to prevent a memory leak
        header.close()

        // skip the extra header if present
        if (flags and GzipHeaderFlags.EXTRA != 0) {
            val extraLen = source.readShort().toLong()
            source.discardExact(extraLen)
        }

        check(magic == GZIP_MAGIC) { "GZIP magic invalid: $magic" }
        check(format.toInt() == Deflater.DEFLATED) { "Deflater method unsupported: $format." }
        check(!(flags has GzipHeaderFlags.FNAME)) { "Gzip file name not supported" }
        check(!(flags has GzipHeaderFlags.FCOMMENT)) { "Gzip file comment not supported" }

        // skip the header CRC if present
        if (flags has GzipHeaderFlags.FHCRC) {
            source.discardExact(2)
        }
    }

    try {
        var totalSize = 0
        var data = ByteArray(0)
        while (source.awaitBytes()) {
            data = source.readBuffer().toByteArray()
            inflater.setInput(data, 0, data.size)

            while (!inflater.needsInput() && !inflater.finished()) {
                val buffer = ByteArray(data.size * EXPANSION_FACTOR)
                val size = inflater.inflate(buffer)
                totalSize += size
                checksum.update(buffer, 0, size)
                writeByteArray(buffer, 0, size)
            }

            flush()
        }

        source.closedCause?.let { throw it }

        while (!inflater.finished()) {
            val buffer = ByteArray(inflater.remaining * EXPANSION_FACTOR)
            val size = inflater.inflate(buffer)
            totalSize += size
            checksum.update(buffer, 0, size)
            writeByteArray(buffer, 0, size)
        }

        if (gzip) {
            check(inflater.remaining == 8) {
                "Expected 8 bytes in the trailer. Actual: ${inflater.remaining} $"
            }

            val buffer = ByteArrayBuffer(data, data.size - 8)
            val expectedChecksum = buffer.readInt().reverseByteOrder()
            val expectedSize = buffer.readInt().reverseByteOrder()

            check(checksum.value.toInt() == expectedChecksum) { "Gzip checksum invalid." }
            check(totalSize == expectedSize) { "Gzip size invalid. Expected $expectedSize, actual $totalSize" }
        } else {
            check(inflater.remaining == 0)
        }
    } finally {
        inflater.end()
    }
}

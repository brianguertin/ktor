/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.*
import io.ktor.io.charsets.*

@PublishedApi
internal class Utf8StringReader(
    private val input: ByteReadChannel
) : StringReader {
    override val charset: Charset = Charsets.UTF_8

    override val closedCause: Throwable?
        get() = input.closedCause

    override val readablePacket: Packet
        get() = input.readablePacket

    var chunk: String = readablePacket.clone().readString()
    var chunkStart: Int = 0
    var bytesInPacket = readablePacket.availableForRead

    override suspend fun readStringChunk(block: (chunk: String, startIndex: Int) -> Int) {
        if (chunkIsEmpty() && !awaitBytes()) return

        if (cacheIsInvalid()) {
            chunk = readablePacket.clone().readString()
            chunkStart = 0
            bytesInPacket = readablePacket.availableForRead
        }

        val consumed = block(chunk, chunkStart)
        check(consumed >= 0) { "Block should return non-negative number of consumed bytes: $consumed" }

        readablePacket.discardExact(sizeInBytes(chunk, chunkStart, chunkStart + consumed))
        chunkStart += consumed
    }

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        chunk = ""
        chunkStart = 0
        return input.awaitBytes(predicate)
    }

    override fun cancel(cause: Throwable?): Boolean {
        return input.cancel()
    }

    private fun chunkIsEmpty() = chunkStart >= chunk.length

    private fun cacheIsInvalid() = bytesInPacket != readablePacket.availableForRead
}

private fun sizeInBytes(chunk: String, startIndex: Int, endIndex: Int): Int {
    var result = 0
    for (i in startIndex until endIndex) {
        result += chunk[i].sizeInBytes()
    }
    return result
}

private fun Char.sizeInBytes(): Int {
    return when (this) {
        in '\u0000'..'\u007F' -> 1
        in '\u0080'..'\u07FF' -> 2
        in '\u0800'..'\uFFFF' -> 3
        else -> 4
    }
}

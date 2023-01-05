/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.*
import io.ktor.io.charsets.*

internal class Utf8StringReader(
    private val input: ByteReadChannel
) : StringReader {
    override val charset: Charset = Charsets.UTF_8

    var cache: String? = null
    var cacheStart: Int = 0

    override val closedCause: Throwable? = input.closedCause

    override val readablePacket: Packet = input.readablePacket

    override suspend fun readLineTo(out: Appendable, limit: Long): Boolean {
        require(limit >= 0) { "limit shouldn't be negative: $limit" }
        if (cacheIsEmpty() && !input.awaitBytes()) return false

        if (limit == Long.MAX_VALUE) {
            return readLineToNoLimit(out)
        }

        return readLineToWithLimit(out, limit)
    }

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean = input.awaitBytes(predicate)

    override fun cancel(cause: Throwable?): Boolean = input.cancel(cause)

    private suspend fun readLineToNoLimit(out: Appendable): Boolean {
        if (readLineFromCacheNoLimit(out)) return true

        while (input.awaitBytes()) {
            val chunk = input.readablePacket.clone().readString()
            val newLine = chunk.indexOf('\n')

            if (newLine == -1) {
                if (chunk.last() == '\r') {
                    TODO("Case with \\r")
                }

                out.appendDiscarding(chunk, 0, 0, chunk.length)
                continue
            }

            val hasCaret = newLine > 0 && chunk[newLine - 1] == '\r'
            val last = if (hasCaret) newLine - 1 else newLine
            out.appendDiscarding(chunk, if (hasCaret) 2 else 1, 0, last)
            if (last < chunk.length) {
                saveInCache(chunk, newLine + 1)
            }

            return true
        }

        return false
    }

    private suspend fun readLineFromCacheNoLimit(out: Appendable): Boolean {
        val chunk = cache ?: return false
        val start = cacheStart
        val position = chunk.indexOf('\n', start)
        if (position > 0) {
            val hasCaret = position == start || chunk[position - 1] != '\r'
            val last = if (hasCaret) position else position - 1
            out.appendDiscarding(chunk, if (hasCaret) 2 else 1, start, last)

            if (position == chunk.lastIndex) {
                dropCache()
            } else {
                cacheStart = position + 1
            }

            return true
        }

        if (chunk.last() == '\r') {
            TODO("Case with \\r at the end")
        }

        out.appendDiscarding(chunk, 0, start, chunk.length)
        dropCache()
        return false
    }

    private suspend fun readLineToWithLimit(out: Appendable, limit: Long): Boolean {
        var remaining = readLineFromCacheWithLimit(out, limit)
        if (remaining < 0) return true

        while (awaitBytes()) {
            val chunk = readablePacket.clone().readString()
            val newLine = chunk.indexOf('\n')

            if (newLine == -1) {
                if (chunk.length > remaining) {
                    lineIsTooLong(limit)
                }

                if (chunk.last() == '\r') {
                    TODO("Case with \\r")
                }

                out.appendDiscarding(chunk, 0, 0, chunk.length)
                remaining -= chunk.length
                continue
            }

            val hasCaret = newLine > 0 && chunk[newLine - 1] == '\r'
            val last = if (hasCaret) newLine - 1 else newLine
            if (remaining < last) {
                lineIsTooLong(limit)
            }

            out.appendDiscarding(chunk, if (hasCaret) 2 else 1, 0, last)
            if (last < chunk.length) {
                saveInCache(chunk, newLine + 1)
            }

            return true
        }

        return false
    }

    /**
     * Returns number of bytes remaining to read or -1 if delimiter was found.
     *
     * If 0 is returned, it means that bytes were added to [out] and delimiter expected to be found in the next chunk.
     */
    private suspend fun readLineFromCacheWithLimit(out: Appendable, limit: Long): Long {
        val chunk = cache ?: return limit
        val start = cacheStart
        val newLine = chunk.indexOf('\n', start)
        if (newLine == -1) {
            val remaining = limit - (chunk.length - start)
            if (remaining < 0) {
                lineIsTooLong(limit)
            }

            if (chunk.last() == '\r') {
                TODO("Case with \\r at the end")
            }

            out.appendDiscarding(chunk, 0, start, chunk.length)
            dropCache()
            return remaining
        }

        val hasCaret = newLine == start || chunk[newLine - 1] != '\r'
        val end = if (hasCaret) newLine else newLine - 1
        val length = end - start
        if (length > limit) {
            lineIsTooLong(limit)
        }

        out.appendDiscarding(chunk, if (hasCaret) 2 else 1, start, end)
        if (newLine == chunk.lastIndex) {
            dropCache()
        } else {
            cacheStart = newLine + 1
        }

        return -1
    }

    private fun saveInCache(chunk: String, start: Int) {
        cache = chunk
        cacheStart = start
    }

    private fun cacheIsEmpty(): Boolean {
        val cache = cache ?: return true
        return cacheStart >= cache.length
    }

    private fun dropCache() {
        cache = null
        cacheStart = 0
    }

    private fun lineIsTooLong(limit: Long): Nothing {
        val cause = TooLongLineException(limit)
        input.cancel(cause)
        throw cause
    }

    private fun sizeInBytes(value: String, startIndex: Int, endIndex: Int): Int {
        var size = 0
        for (index in startIndex until endIndex) {
            size += when (val char = value[index]) {
                in '\u0000'..'\u007F' -> 1
                in '\u0080'..'\u07FF' -> 2
                in '\u0800'..'\uFFFF' -> 3
                else -> 4
            }
        }

        return size
    }

    private fun Appendable.appendDiscarding(value: String, newLineSize: Int, startIndex: Int, endIndex: Int) {
        input.readablePacket.discardExact(sizeInBytes(value, startIndex, endIndex) + newLineSize)
        append(value, startIndex, endIndex)
    }
}

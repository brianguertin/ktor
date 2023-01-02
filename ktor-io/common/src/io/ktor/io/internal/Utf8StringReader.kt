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

    override val closedCause: Throwable?
        get() = TODO("Not yet implemented")

    override val readablePacket: Packet
        get() = TODO("Not yet implemented")

    override suspend fun readLineTo(out: Appendable, limit: Long): Boolean {
        require(limit >= 0) { "limit shouldn't be negative: $limit" }
        if (cacheIsEmpty() && !input.awaitBytes()) return false

        if (limit == Long.MAX_VALUE) {
            return readLineToNoLimit(out)
        }

        TODO("Case with limit")
    }

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun cancel(cause: Throwable?): Boolean {
        TODO("Not yet implemented")
    }

    private suspend fun readLineToNoLimit(out: Appendable): Boolean {
        if (readLineFromCacheNoLimit(out)) return true

        while (input.awaitBytes()) {
            val chunk = input.readablePacket.readString()
            val newLine = chunk.indexOf('\n')

            if (newLine == -1) {
                if (chunk.last() == '\r') {
                    TODO("Case with \\r")
                }

                out.append(chunk)
                continue
            }

            val last = if (newLine > 0 && chunk[newLine - 1] == '\r') newLine - 1 else newLine
            out.append(chunk, 0, last)
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
            val last = if (position == start || chunk[position - 1] != '\r') position else position - 1
            out.append(chunk, start, last)

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

        out.append(chunk, start, chunk.length)
        dropCache()
        return false
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
}

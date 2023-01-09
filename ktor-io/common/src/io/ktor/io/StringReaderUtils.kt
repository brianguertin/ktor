/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io


/**
 * Read [String] from channel until `\r\n` or `\n` or end of channel.
 *
 * The delimiter is not included in the result, but skipped from the input.
 * Returns `null` if the [input.isClosedForRead] and no characters available.
 *
 * If line is longer than limit then [TooLongLineException] is thrown. In this case reader considered as closed and
 * the input channel will be cancelled.
 */
public suspend fun StringReader.readLine(limit: Long = Long.MAX_VALUE): String? {
    val builder = StringBuilder()
    val hasBytes = readLineTo(builder, limit)
    return if (hasBytes) builder.toString() else null
}

/**
 * Reads string to [out] until `\n` or `\r\n` or end of the [StringReader] and appends it to the [out]. Delimiter is not
 * included in the result and dropped form the input.
 *
 * If line is longer than [limit], [TooLongLineException] will be thrown.
 *
 * @return `true` if some characters were read or `false` if the [StringReader] is closed and no characters available.
 */
public suspend fun StringReader.readLineTo(out: Appendable, limit: Long = Long.MAX_VALUE): Boolean {
    var remaining = limit
    var newLineFound = false
    var lastIsCaret = false
    while (!newLineFound && awaitBytes()) {
        readStringChunk { chunk, startIndex ->
            if (lastIsCaret) {
                lastIsCaret = false
                if (chunk[startIndex] == '\n') {
                    newLineFound = true
                    return@readStringChunk startIndex + 1
                }
            }

            val newLine = chunk.indexOf('\n', startIndex)
            if (newLine >= 0) {
                newLineFound = true
                return@readStringChunk newLineCase(newLine, chunk, startIndex, remaining, limit, out)
            }

            val end = if (chunk.last() == '\r') {
                lastIsCaret = true
                chunk.length - 1
            } else {
                chunk.length
            }

            val length = end - startIndex
            if (length > remaining) {
                throw TooLongLineException(limit)
            }

            out.append(chunk, startIndex, chunk.length)
            remaining -= length
            return@readStringChunk chunk.length
        }
    }

    return newLineFound || remaining < limit
}

private fun newLineCase(
    newLineIndex: Int,
    chunk: String,
    startIndex: Int,
    remaining: Long,
    limit: Long,
    out: Appendable
): Int {
    val hasCaret = newLineIndex > 0 && chunk[newLineIndex - 1] == '\r'
    val end = if (hasCaret) newLineIndex - 1 else newLineIndex

    if (end - startIndex > remaining) {
        throw TooLongLineException(limit)
    }

    out.append(chunk, startIndex, end)
    return newLineIndex + 1
}

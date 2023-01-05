/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*
import io.ktor.io.internal.*

public fun ByteReadChannel.stringReader(charset: Charset = Charsets.UTF_8): StringReader {
    if (charset == Charsets.UTF_8) {
        return Utf8StringReader(this)
    }

    TODO("Unsupported charset: $charset")
}

public interface StringReader : ByteReadChannel {
    public val charset: Charset

    /**
     * Reads line until `\n` or `\r\n` from the channel and appends it to the [out] buffer. Delimiter is not included
     * in the result and dropped form the input.
     *
     * If line is longer than [limit], [TooLongLineException] will be thrown.
     *
     * @return `true` if `\n\` or `\r\n` was read, `false` if no delimiter was found and channel is closed.
     * If no delimiter found and channel is closed, [out] will contain the rest of the input.
     */
    public suspend fun readLineTo(out: Appendable, limit: Long = Long.MAX_VALUE): Boolean
}

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
    val hasNewLine = readLineTo(builder, limit)
    return if (hasNewLine || builder.isNotEmpty()) builder.toString() else null
}

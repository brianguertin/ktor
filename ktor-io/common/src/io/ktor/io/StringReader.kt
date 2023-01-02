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

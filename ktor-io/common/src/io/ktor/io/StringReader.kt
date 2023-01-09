/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*
import io.ktor.io.internal.*

public inline fun <T> ByteReadChannel.stringReader(
    charset: Charset = Charsets.UTF_8,
    block: (reader: StringReader) -> T
): T {
    val reader = when (charset) {
        Charsets.UTF_8 -> Utf8StringReader(this)
        else -> error("Unsupported charset: $charset")
    }

    return block(reader)
}

public interface StringReader : ByteReadChannel {
    public val charset: Charset

    /**
     * Execute block with a next chunk from the channel. The [block] should return end index: the index after the first non-consumed character.
     *
     * If the exception is thrown from the [block] then the channel will be cancelled with this exception.
     */
    public suspend fun readStringChunk(block: (chunk: String, startIndex: Int) -> Int)
}

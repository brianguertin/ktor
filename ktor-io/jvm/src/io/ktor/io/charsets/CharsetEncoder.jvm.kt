/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*
import java.nio.*

public actual typealias CharsetEncoder = java.nio.charset.CharsetEncoder

public actual val CharsetEncoder.charset: Charset get() = charset()

internal actual fun CharsetEncoder.encodeCharSequence(
    input: CharSequence,
    fromIndex: Int,
    toIndex: Int,
    dst: Packet
): Int {
    TODO()
}

private fun CharsetEncoder.encodeToByteArraySlow(input: CharSequence, fromIndex: Int, toIndex: Int): ByteArray {
    val result = encode(CharBuffer.wrap(input, fromIndex, toIndex))

    val existingArray = when {
        result.hasArray() && result.arrayOffset() == 0 -> result.array().takeIf { it.size == result.remaining() }
        else -> null
    }

    return existingArray ?: ByteArray(result.remaining()).also { result.get(it) }
}

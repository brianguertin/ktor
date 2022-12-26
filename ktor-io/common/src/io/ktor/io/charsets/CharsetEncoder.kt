/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*
import io.ktor.io.internal.CharArraySequence

public expect abstract class CharsetEncoder

public expect val CharsetEncoder.charset: Charset

public fun CharsetEncoder.encodeToByteArray(
    input: CharSequence,
    fromIndex: Int = 0,
    toIndex: Int = input.length
): ByteArray = buildPacket {
    encodeCharSequence(input, fromIndex, toIndex, this)
}.toByteArray()

public fun CharsetEncoder.encode(
    input: CharSequence,
    fromIndex: Int = 0,
    toIndex: Int = input.length
): Packet = buildPacket {
    encodeCharSequence(input, fromIndex, toIndex, this)
}

public fun CharsetEncoder.encode(input: CharArray, fromIndex: Int, toIndex: Int, dst: Packet) {
    val sequence = CharArraySequence(input, fromIndex, fromIndex + toIndex)
    encodeCharSequence(sequence, 0, sequence.length, dst)
}

internal expect fun CharsetEncoder.encodeCharSequence(input: CharSequence, fromIndex: Int, toIndex: Int, dst: Packet): Int

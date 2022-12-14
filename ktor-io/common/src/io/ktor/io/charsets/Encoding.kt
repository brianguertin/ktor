/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*
import io.ktor.io.internal.*

public expect val CharsetEncoder.charset: Charset

public expect fun CharsetEncoder.encodeToByteArray(
    input: CharSequence,
    fromIndex: Int = 0,
    toIndex: Int = input.length
): ByteArray

public expect fun CharsetEncoder.encodeUTF8(input: Packet, dst: Packet)

public fun CharsetEncoder.encode(
    input: CharSequence,
    fromIndex: Int = 0,
    toIndex: Int = input.length
): Packet = buildPacket {
    encodeImpl(input, fromIndex, toIndex, this)
}

public fun CharsetEncoder.encodeUTF8(input: Packet): Packet = buildPacket {
    encodeUTF8(input, this)
}

public fun CharsetEncoder.encode(input: CharArray, fromIndex: Int, toIndex: Int, dst: Packet) {
    val sequence = CharArraySequence(input, fromIndex, fromIndex + toIndex)
    encodeImpl(sequence, 0, sequence.length, dst)
}

// ----------------------------- DECODER -------------------------------------------------------------------------------

/**
 * Decoder's charset it is created for.
 */
public expect val CharsetDecoder.charset: Charset

public fun CharsetDecoder.decode(input: Packet, max: Int = Int.MAX_VALUE): String = buildString {
    decodeBuffer(input, this, true, max)
}

public expect fun CharsetDecoder.decode(input: Packet, dst: Appendable, max: Int): Int

public expect fun CharsetDecoder.decodeExactBytes(input: Packet, inputLength: Int): String

public expect open class MalformedInputException(message: String) : Throwable

public class TooLongLineException(message: String) : MalformedInputException(message)

// ----------------------------- INTERNALS -----------------------------------------------------------------------------

internal fun CharsetEncoder.encodeArrayImpl(input: CharArray, fromIndex: Int, toIndex: Int, dst: Packet): Int {
    val length = toIndex - fromIndex
    return encodeImpl(CharArraySequence(input, fromIndex, length), 0, length, dst)
}

internal expect fun CharsetEncoder.encodeImpl(input: CharSequence, fromIndex: Int, toIndex: Int, dst: Packet): Int

internal expect fun CharsetEncoder.encodeComplete(dst: Packet): Boolean

internal expect fun CharsetDecoder.decodeBuffer(
    input: Packet,
    out: Appendable,
    lastBuffer: Boolean,
    max: Int = Int.MAX_VALUE
): Int

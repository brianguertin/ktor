/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*

public expect abstract class CharsetDecoder

/**
 * Decoder's charset it is created for.
 */
public expect val CharsetDecoder.charset: Charset

public fun CharsetDecoder.decodePacket(input: Packet): String = buildString {
    decodePacketTo(input, this)
}

public fun CharsetDecoder.decodePacketTo(input: Packet, dst: Appendable): Int {
    var result = 0
    while (input.availableForRead > 0) {
        val buffer = input.readBuffer()
        result += decodeBufferTo(buffer, dst, input.availableForRead == 0)
    }

    return result
}

public expect open class MalformedInputException(message: String) : Throwable

public class TooLongLineException(message: String) : MalformedInputException(message)

internal expect fun CharsetDecoder.decodeBufferTo(
    input: ReadableBuffer,
    out: Appendable,
    lastBuffer: Boolean
): Int

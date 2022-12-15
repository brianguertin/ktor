package io.ktor.io.charsets

import org.khronos.webgl.*

// I don't know any characters that have longer characters
internal const val MAX_CHARACTERS_SIZE_IN_BYTES: Int = 8
private const val MAX_CHARACTERS_COUNT = Int.MAX_VALUE / MAX_CHARACTERS_SIZE_IN_BYTES

internal data class DecodeBufferResult(val charactersDecoded: String, val bytesConsumed: Int)

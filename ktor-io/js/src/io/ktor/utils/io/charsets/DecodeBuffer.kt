package io.ktor.utils.io.charsets

import io.ktor.utils.io.js.*
import org.khronos.webgl.*

// I don't know any characters that have longer characters
internal const val MAX_CHARACTERS_SIZE_IN_BYTES: Int = 8
private const val MAX_CHARACTERS_COUNT = Int.MAX_VALUE / MAX_CHARACTERS_SIZE_IN_BYTES

internal data class DecodeBufferResult(val charactersDecoded: String, val bytesConsumed: Int)

internal fun Int8Array.decodeBufferImpl(nativeDecoder: Decoder, maxCharacters: Int): DecodeBufferResult {
    if (maxCharacters == 0) {
        return DecodeBufferResult("", 0)
    }

    // fast-path: try to assume that we have 1 byte per character content
    try {
        val sizeInBytes = maxCharacters.coerceAtMost(byteLength)
        val text = nativeDecoder.decode(subarray(0, sizeInBytes))
        if (text.length <= maxCharacters) {
            return DecodeBufferResult(text, sizeInBytes)
        }
    } catch (_: dynamic) {
    }

    TODO()
}

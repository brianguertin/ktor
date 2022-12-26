/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*
import kotlinx.cinterop.*
import platform.iconv.*
import platform.posix.*

public actual abstract class Charset(internal val _name: String) {
    public actual abstract fun newEncoder(): CharsetEncoder
    public actual abstract fun newDecoder(): CharsetDecoder

    public actual companion object {
        public actual fun forName(name: String): Charset {
            if (name == "UTF-8" || name == "utf-8" || name == "UTF8" || name == "utf8") return Charsets.UTF_8
            if (name == "ISO-8859-1" || name == "iso-8859-1" || name == "ISO_8859_1") return Charsets.ISO_8859_1
            if (name == "UTF-16" || name == "utf-16" || name == "UTF16" || name == "utf16") return Charsets.UTF_16

            return CharsetImpl(name)
        }

        public actual fun isSupported(charset: String): Boolean = when (charset) {
            "UTF-8", "utf-8", "UTF8", "utf8" -> true
            "ISO-8859-1", "iso-8859-1" -> true
            "UTF-16", "utf-16", "UTF16", "utf16" -> true
            else -> false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Charset) return false

        if (_name != other._name) return false

        return true
    }

    override fun hashCode(): Int {
        return _name.hashCode()
    }

    override fun toString(): String {
        return _name
    }

    public actual fun name(): String = _name
}

private class CharsetImpl(name: String) : Charset(name) {
    init {
        val v = iconv_open(name, "UTF-8")
        checkErrors(v, name)
        iconv_close(v)
    }

    override fun newEncoder(): CharsetEncoder = CharsetEncoderImpl(this)
    override fun newDecoder(): CharsetDecoder = CharsetDecoderImpl(this)
}

// -----------------------

public actual abstract class CharsetEncoder(internal val _charset: Charset)

private data class CharsetEncoderImpl(private val charset: Charset) : CharsetEncoder(charset)

public actual val CharsetEncoder.charset: Charset get() = _charset

private fun iconvCharsetName(name: String) = when (name) {
    "UTF-16" -> platformUtf16
    else -> name
}

private val negativePointer = (-1L).toCPointer<IntVar>()

private fun checkErrors(iconvOpenResults: COpaquePointer?, charset: String) {
    if (iconvOpenResults == null || iconvOpenResults === negativePointer) {
        throw IllegalArgumentException("Failed to open iconv for charset $charset with error code ${posix_errno()}")
    }
}

internal actual fun CharsetEncoder.encodeCharSequence(input: CharSequence, fromIndex: Int, toIndex: Int, dst: Packet): Int {
    TODO()
}


private fun checkIconvResult(errno: Int) {
    if (errno == EILSEQ) throw MalformedInputException("Malformed or unmappable bytes at input")
    if (errno == EINVAL) return // too few input bytes
    if (errno == E2BIG) return // too few output buffer bytes

    throw IllegalStateException("Failed to call 'iconv' with error code $errno")
}

internal actual fun CharsetDecoder.decodeBufferTo(
    input: ReadableBuffer,
    out: Appendable,
    lastBuffer: Boolean
): Int {
    TODO()
}

// ----------------------------------------------------------------------

public actual abstract class CharsetDecoder(internal val _charset: Charset)
private data class CharsetDecoderImpl(private val charset: Charset) : CharsetDecoder(charset)

public actual val CharsetDecoder.charset: Charset get() = _charset

private val platformUtf16: String = if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) "UTF-16BE" else "UTF-16LE"

// -----------------------------------------------------------

public actual object Charsets {
    public actual val UTF_8: Charset = CharsetImpl("UTF-8")
    public actual val ISO_8859_1: Charset = CharsetImpl("ISO-8859-1")
    internal val UTF_16: Charset = CharsetImpl(platformUtf16)
}

public actual open class MalformedInputException actual constructor(message: String) : Throwable(message)

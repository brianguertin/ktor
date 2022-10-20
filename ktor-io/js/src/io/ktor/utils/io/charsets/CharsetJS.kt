package io.ktor.utils.io.charsets

import io.ktor.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.js.*
import org.khronos.webgl.*

public actual abstract class Charset(internal val _name: String) {
    public actual abstract fun newEncoder(): CharsetEncoder
    public actual abstract fun newDecoder(): CharsetDecoder

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as Charset

        if (_name != other._name) return false

        return true
    }

    override fun hashCode(): Int {
        return _name.hashCode()
    }

    override fun toString(): String {
        return _name
    }

    public actual companion object {
        @Suppress("LocalVariableName")
        public actual fun forName(name: String): Charset {
            if (name == "UTF-8" || name == "utf-8" || name == "UTF8" || name == "utf8") return Charsets.UTF_8
            if (name == "ISO-8859-1" || name == "iso-8859-1" ||
                name.replace('_', '-').let { it == "iso-8859-1" || it.lowercase() == "iso-8859-1" } ||
                name == "latin1" || name == "Latin1"
            ) {
                return Charsets.ISO_8859_1
            }
            throw IllegalArgumentException("Charset $name is not supported")
        }

        public actual fun isSupported(charset: String): Boolean = when {
            charset == "UTF-8" || charset == "utf-8" || charset == "UTF8" || charset == "utf8" -> true
            charset == "ISO-8859-1" || charset == "iso-8859-1" || charset.replace('_', '-').let {
                it == "iso-8859-1" || it.lowercase() == "iso-8859-1"
            } || charset == "latin1" -> true
            else -> false
        }
    }
}

public actual val Charset.name: String get() = _name

// -----------------------

public actual abstract class CharsetEncoder(internal val _charset: Charset)
private data class CharsetEncoderImpl(private val charset: Charset) : CharsetEncoder(charset)

public actual val CharsetEncoder.charset: Charset get() = _charset

public actual fun CharsetEncoder.encodeToByteArray(input: CharSequence, fromIndex: Int, toIndex: Int): ByteArray =
    TODO()

internal actual fun CharsetEncoder.encodeImpl(input: CharSequence, fromIndex: Int, toIndex: Int, dst: Packet): Int {
    TODO()
}

public actual fun CharsetEncoder.encodeUTF8(input: Packet, dst: Packet) {
    require(charset === Charsets.UTF_8)
    // we only support UTF-8 so as far as input is UTF-8 encoded string then we simply copy bytes
    dst.writePacket(input)
}

internal actual fun CharsetEncoder.encodeComplete(dst: Packet): Boolean = true

// ----------------------------------------------------------------------

public actual abstract class CharsetDecoder(internal val _charset: Charset)

private data class CharsetDecoderImpl(private val charset: Charset) : CharsetDecoder(charset)

public actual val CharsetDecoder.charset: Charset get() = _charset

internal actual fun CharsetDecoder.decodeBuffer(
    input: Packet,
    out: Appendable,
    lastBuffer: Boolean,
    max: Int
): Int {
    TODO()
}

public actual fun CharsetDecoder.decode(input: Packet, dst: Appendable, max: Int): Int {
    TODO()
}

public actual fun CharsetDecoder.decodeExactBytes(input: Packet, inputLength: Int): String {
    TODO()
}

// -----------------------------------------------------------

public actual object Charsets {
    public actual val UTF_8: Charset = CharsetImpl("UTF-8")
    public actual val ISO_8859_1: Charset = CharsetImpl("ISO-8859-1")
}

private data class CharsetImpl(val name: String) : Charset(name) {
    override fun newEncoder(): CharsetEncoder = CharsetEncoderImpl(this)
    override fun newDecoder(): CharsetDecoder = CharsetDecoderImpl(this)
}

public actual open class MalformedInputException actual constructor(message: String) : Throwable(message)

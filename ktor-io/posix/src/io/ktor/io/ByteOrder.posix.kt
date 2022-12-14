/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public actual enum class ByteOrder {
    BIG_ENDIAN, LITTLE_ENDIAN;

    public actual companion object {
        private val native: ByteOrder = if (Platform.isLittleEndian) LITTLE_ENDIAN else BIG_ENDIAN

        public actual fun nativeOrder(): ByteOrder = native
    }
}

/**
 * Reverse number's byte order
 */
public actual fun Short.reverseByteOrder(): Short = swap(this)

/**
 * Reverse number's byte order
 */
public actual fun Int.reverseByteOrder(): Int = swap(this)

/**
 * Reverse number's byte order
 */
public actual fun Long.reverseByteOrder(): Long = swap(this)

/**
 * Reverse number's byte order
 */
public actual fun Float.reverseByteOrder(): Float = swap(this)

/**
 * Reverse number's byte order
 */
public actual fun Double.reverseByteOrder(): Double = swap(this)

private inline fun swap(s: Short): Short = (((s.toInt() and 0xff) shl 8) or ((s.toInt() and 0xffff) ushr 8)).toShort()

private inline fun swap(s: Int): Int =
    (swap((s and 0xffff).toShort()).toInt() shl 16) or (swap((s ushr 16).toShort()).toInt() and 0xffff)

private inline fun swap(s: Long): Long =
    (swap((s and 0xffffffff).toInt()).toLong() shl 32) or (swap((s ushr 32).toInt()).toLong() and 0xffffffff)

private inline fun swap(s: Float): Float = Float.fromBits(swap(s.toRawBits()))

private inline fun swap(s: Double): Double = Double.fromBits(swap(s.toRawBits()))

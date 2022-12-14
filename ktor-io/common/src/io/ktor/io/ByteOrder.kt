/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public expect enum class ByteOrder {
    BIG_ENDIAN, LITTLE_ENDIAN;

    public companion object {
        public fun nativeOrder(): ByteOrder
    }
}

/**
 * Reverse number's byte order
 */
public expect fun Short.reverseByteOrder(): Short

/**
 * Reverse number's byte order
 */
public expect fun Int.reverseByteOrder(): Int

/**
 * Reverse number's byte order
 */
public expect fun Long.reverseByteOrder(): Long

/**
 * Reverse number's byte order
 */
public expect fun Float.reverseByteOrder(): Float

/**
 * Reverse number's byte order
 */
public expect fun Double.reverseByteOrder(): Double

/**
 * Reverse number's byte order
 */
public fun UShort.reverseByteOrder(): UShort = toShort().reverseByteOrder().toUShort()

/**
 * Reverse number's byte order
 */
public fun UInt.reverseByteOrder(): UInt = toInt().reverseByteOrder().toUInt()

/**
 * Reverse number's byte order
 */
public fun ULong.reverseByteOrder(): ULong = toLong().reverseByteOrder().toULong()

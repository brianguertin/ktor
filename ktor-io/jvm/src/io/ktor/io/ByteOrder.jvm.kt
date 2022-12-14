/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io


public actual enum class ByteOrder(public val nioOrder: java.nio.ByteOrder) {
    BIG_ENDIAN(java.nio.ByteOrder.BIG_ENDIAN),
    LITTLE_ENDIAN(java.nio.ByteOrder.LITTLE_ENDIAN);

    public actual companion object {
        private val native: ByteOrder = orderOf(java.nio.ByteOrder.nativeOrder())

        public fun of(nioOrder: java.nio.ByteOrder): ByteOrder = orderOf(nioOrder)

        public actual fun nativeOrder(): ByteOrder = native
    }
}


/**
 * Reverse number's byte order
 */
@Suppress("NOTHING_TO_INLINE")
public actual inline fun Short.reverseByteOrder(): Short = java.lang.Short.reverseBytes(this)

/**
 * Reverse number's byte order
 */
@Suppress("NOTHING_TO_INLINE")
public actual inline fun Int.reverseByteOrder(): Int = java.lang.Integer.reverseBytes(this)

/**
 * Reverse number's byte order
 */
@Suppress("NOTHING_TO_INLINE")
public actual inline fun Long.reverseByteOrder(): Long = java.lang.Long.reverseBytes(this)

/**
 * Reverse number's byte order
 */
@Suppress("NOTHING_TO_INLINE")
public actual inline fun Float.reverseByteOrder(): Float =
    java.lang.Float.intBitsToFloat(
        java.lang.Integer.reverseBytes(
            java.lang.Float.floatToRawIntBits(this)
        )
    )

/**
 * Reverse number's byte order
 */
@Suppress("NOTHING_TO_INLINE")
public actual inline fun Double.reverseByteOrder(): Double =
    java.lang.Double.longBitsToDouble(
        java.lang.Long.reverseBytes(
            java.lang.Double.doubleToRawLongBits(this)
        )
    )

private fun orderOf(nioOrder: java.nio.ByteOrder): ByteOrder =
    if (nioOrder === java.nio.ByteOrder.BIG_ENDIAN) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.core.*

public interface ReadableBuffer : Closeable {
    /**
     * The index in buffer for the read operation.
     *
     * Should be between 0 and [writeIndex]
     */
    public var readIndex: Int

    /**
     * The index in buffer for the write operation.
     *
     * Should be between the [readIndex] and [capacity].
     */
    public val writeIndex: Int

    /**
     * The number of bytes can be stored in the buffer. Upper bound for write operations.
     */
    public val capacity: Int

    /**
     * Reads [Byte] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater [capacity].
     */
    public fun getByteAt(index: Int): Byte


    /**
     * Reads [Byte] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public fun readByte(): Byte {
        ensureCanRead(1)
        return getByteAt(readIndex++)
    }

    /**
     * Reads [Boolean] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater [capacity].
     */
    public fun getBooleanAt(index: Int): Boolean = getByteAt(index) != 0.toByte()

    /**
     * Read boolean from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 1.
     */
    public fun readBoolean(): Boolean = getBooleanAt(readIndex++)

    /**
     * Reads [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater [capacity].
     */
    public fun getShortAt(index: Int): Short {
        ensureCanRead(index, 2)

        val byte1 = getByteAt(index)
        val byte2 = getByteAt(index + 1)
        return Short(byte1, byte2)
    }

    /**
     * Reads [Short] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 2.
     */
    public fun readShort(): Short {
        ensureCanRead(2)

        val result = getShortAt(readIndex)
        readIndex += 2
        return result
    }

    /**
     * Reads [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun getIntAt(index: Int): Int {
        ensureCanRead(index, 4)

        val highShort = getShortAt(index)
        val lowShort = getShortAt(index + 2)
        return Int(highShort, lowShort)
    }

    /**
     * Reads [Int] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 4.
     */
    public fun readInt(): Int {
        ensureCanRead(4)

        val result = getIntAt(readIndex)
        readIndex += 4
        return result
    }

    /**
     * Reads [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity].
     */
    public fun getLongAt(index: Int): Long {
        ensureCanRead(index, 8)

        val highInt = getIntAt(index)
        val lowInt = getIntAt(index + 4)
        return Long(highInt, lowInt)
    }


    /**
     * Reads [Long] from the buffer at [readIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForRead] < 8.
     */
    public fun readLong(): Long {
        ensureCanRead(8)

        val result = getLongAt(readIndex)
        readIndex += 8
        return result
    }

    public companion object {
        /**
         * The empty buffer.
         */
        public val Empty: ReadableBuffer = EmptyBuffer
    }
}


/**
 * Check if the Buffer has [count] bytes to read.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForRead].
 */
internal fun Buffer.ensureCanRead(count: Int) {
    if (availableForRead < count) {
        throw IndexOutOfBoundsException("Can't read $count bytes. Available: $availableForRead.")
    }
}

internal fun Buffer.ensureCanRead(index: Int, count: Int) {
    if (index + count > capacity) {
        throw IndexOutOfBoundsException("Can't read $count bytes at index $index. Capacity: $capacity.")
    }
}

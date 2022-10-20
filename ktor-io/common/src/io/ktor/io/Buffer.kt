/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public interface Buffer : ReadableBuffer {

    override var writeIndex: Int

    /**
     * Writes [Byte] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater than [capacity].
     */
    public fun setByteAt(index: Int, value: Byte)

    /**
     * Writes [Byte] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public fun writeByte(value: Byte) {
        ensureCanWrite(1)
        setByteAt(writeIndex++, value)
    }

    /**
     * Writes [Boolean] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 1] is greater than [capacity].
     */
    public fun setBooleanAt(index: Int, value: Boolean) {
        setByteAt(index, if (value) 1.toByte() else 0.toByte())
    }

    /**
     * Write boolean to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 1.
     */
    public fun writeBoolean(value: Boolean) {
        ensureCanWrite(1)
        setBooleanAt(writeIndex++, value)
    }

    /**
     * Writes [Short] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 2] is greater than [capacity].
     */
    public fun setShortAt(index: Int, value: Short) {
        ensureCanWrite(index, 2)

        setByteAt(index, value.highByte)
        setByteAt(index + 1, value.lowByte)
    }

    /**
     * Writes [Short] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 2.
     */
    public fun writeShort(value: Short) {
        ensureCanWrite(2)

        setShortAt(writeIndex, value)
        writeIndex += 2
    }

    /**
     * Writes [Int] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 4] is greater than [capacity].
     */
    public fun setIntAt(index: Int, value: Int) {
        ensureCanWrite(index, 4)

        setShortAt(index, value.highShort)
        setShortAt(index + 2, value.lowShort)
    }

    /**
     * Writes [Int] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 4.
     */
    public fun writeInt(value: Int) {
        ensureCanWrite(4)

        setIntAt(writeIndex, value)
        writeIndex += 4
    }

    /**
     * Writes [Long] at specific [index].
     *
     * The operation doesn't modify [readIndex] or [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [index + 8] is greater than [capacity] or not enough space available.
     */
    public fun setLongAt(index: Int, value: Long) {
        ensureCanWrite(index, 8)

        setIntAt(index, value.highInt)
        setIntAt(index + 4, value.lowInt)
    }

    /**
     * Writes [Long] to the buffer at [writeIndex].
     *
     * @throws IndexOutOfBoundsException if [availableForWrite] < 8.
     */
    public fun writeLong(value: Long) {
        ensureCanWrite(8)

        setLongAt(writeIndex, value)
        writeIndex += 8
    }

    /**
     * Create a copy of this buffer with new [readIndex] and [writeIndex] sharing the same memory.
     */
    public override fun clone(): Buffer

    public companion object {
        public val Empty: Buffer = EmptyBuffer
    }
}

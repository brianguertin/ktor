/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.charsets.*

public class ByteArrayBuffer(
    public val array: ByteArray,
    readIndex: Int = 0,
    writeIndex: Int = array.size
) : Buffer {

    override var writeIndex: Int = writeIndex
        set(value) {
            checkWriteIndex(value)
            field = value
        }

    override var readIndex: Int = readIndex
        set(value) {
            checkReadIndex(value)
            field = value
        }

    override fun setByteAt(index: Int, value: Byte) {
        array[index] = value
    }

    override fun clone(): Buffer {
        return ByteArrayBuffer(array.copyOf(), readIndex, writeIndex)
    }

    override val capacity: Int
        get() = array.size

    override fun getByteAt(index: Int): Byte {
        return array[index]
    }

    override fun readString(charset: Charset): String {
        if (availableForRead == 0) {
            throw IndexOutOfBoundsException("No bytes available for read")
        }

        return String(array, readIndex, writeIndex - readIndex, charset)
    }

    override fun readBuffer(size: Int): ReadableBuffer {
        val array = array.sliceArray(readIndex until readIndex + size)
        val result = ByteArrayBuffer(array)
        readIndex += size
        return result
    }

    override fun readByteArray(size: Int): ByteArray {
        require(size <= availableForRead)
        readIndex += size
        return array.sliceArray(readIndex until readIndex + size)
    }

    override fun toByteArray(): ByteArray {
        val result = array.sliceArray(readIndex until writeIndex)
        readIndex = writeIndex
        return result
    }

    override fun close() {
    }

    private fun checkReadIndex(value: Int) {
        if (value < 0) {
            throw IllegalArgumentException("Read index must be >=0, but got $value")
        }
        if (value > writeIndex) {
            throw IllegalArgumentException("Read index($value) must be less than or equal to write index($writeIndex)")
        }
    }

    private fun checkWriteIndex(value: Int) {
        if (value < readIndex) {
            throw IllegalArgumentException("Write index($value) must be greater than or equal to read index($readIndex")
        }
    }
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.charsets.*

internal object EmptyBuffer : Buffer {
    override var writeIndex: Int
        get() = 0
        set(value) {
            require(value == 0) { "Can't set writeIndex to $value for empty buffer" }
        }

    override fun setByteAt(index: Int, value: Byte) {
        throw IndexOutOfBoundsException("Can't set byte at $index for empty buffer")
    }

    override fun clone(): Buffer = this

    override var readIndex: Int
        get() = 0
        set(value) {
            require(value == 0) { "Can't set readIndex to $value for empty buffer" }
        }

    override val capacity: Int
        get() = 0

    override fun getByteAt(index: Int): Byte {
        throw IndexOutOfBoundsException("Can't get byte at $index for empty buffer")
    }

    override fun readString(charset: Charset): String {
        throw IndexOutOfBoundsException("Can't get byte at 0 for empty buffer")
    }

    override fun readBuffer(size: Int): ReadableBuffer {
        require(size == 0) { "Can't read $size bytes from empty buffer" }
        return this
    }

    override fun readByteArray(size: Int): ByteArray {
        require(size == 0) { "Can't read $size bytes from empty buffer" }
        return ByteArray(0)
    }

    override fun toByteArray(): ByteArray = ByteArray(0)

    override fun close() {
    }
}

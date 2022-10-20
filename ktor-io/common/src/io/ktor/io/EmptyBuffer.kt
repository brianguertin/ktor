/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

internal object EmptyBuffer : Buffer {
    override var writeIndex: Int
        get() = 0
        set(value) {
            require(value == 0) { "Can't set writeIndex to $value for empty buffer" }
        }

    override fun setByteAt(index: Int, value: Byte) {
        throw IndexOutOfBoundsException("Can't set byte at $index for empty buffer")
    }

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

    override fun close() {
    }
}

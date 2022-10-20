/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.charsets.*
import java.nio.*

public class ByteBufferBuffer(
    override val state: ByteBuffer
) : Buffer, WithByteBuffer {
    override var writeIndex: Int
        get() = state.limit()
        set(value) {
            state.limit(value)
        }

    override fun setByteAt(index: Int, value: Byte) {
        state.put(index, value)
    }

    override fun readBuffer(size: Int): ReadableBuffer {
        require(size <= availableForRead)

        val newState = state.slice().apply {
            limit(size)
        }

        readIndex += size

        return ByteBufferBuffer(newState)
    }

    override fun toByteArray(): ByteArray {
        if (state.hasArray()) {
            val arrayOffset = state.arrayOffset()
            val startIndex = readIndex + arrayOffset
            val endIndex = writeIndex + arrayOffset
            val result = state.array().sliceArray(startIndex until endIndex)
            readIndex = writeIndex
            return result
        }

        TODO("Direct buffers are not supported")
    }

    override fun readByteArray(size: Int): ByteArray {
        require(size <= availableForRead)
        if (state.hasArray()) {
            val arrayOffset = state.arrayOffset()
            val startIndex = readIndex + arrayOffset
            val endIndex = startIndex + size
            val result = state.array().sliceArray(startIndex until endIndex)
            readIndex += size

            return result
        }

        TODO("Can't read from direct buffer")
    }

    override fun clone(): Buffer {
        return ByteBufferBuffer(state.duplicate())
    }

    override var readIndex: Int
        get() = state.position()
        set(value) {
            state.position(value)
        }

    override val capacity: Int
        get() = state.capacity()

    override fun getByteAt(index: Int): Byte {
        return state[index]
    }

    override fun close() {
    }

    override fun readString(charset: Charset): String {
        if (charset != Charsets.UTF_8) {
            TODO("Chraset $charset is not supported")
        }

        if (!state.hasArray()) {
            TODO("Only buffers with array are supported")
        }

        return String(state.array(), state.arrayOffset() + state.position(), state.remaining())
    }
}

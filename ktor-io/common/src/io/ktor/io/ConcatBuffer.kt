/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.charsets.*
import kotlin.math.*

public class ConcatBuffer(
    public val buffers: ArrayDeque<ReadableBuffer> = ArrayDeque()
) : ReadableBuffer {
    override var capacity: Int = buffers.sumOf { it.availableForRead }
        set(value) {
            field = value
            writeIndex = value
        }

    override var writeIndex: Int = capacity
        private set

    override var readIndex: Int = 0

    override fun getByteAt(index: Int): Byte {
        var bufferIndex = 0
        var offset = 0

        while (buffers[bufferIndex].availableForRead < index - offset) {
            offset += buffers[bufferIndex].availableForRead
            bufferIndex++
        }

        return buffers[bufferIndex].getByteAt(index - offset)
    }

    override fun readString(charset: Charset): String {
        TODO("Not yet implemented")
    }

    override fun readBuffer(size: Int): ReadableBuffer {
        val first = buffers.first()
        if (first.availableForRead <= size) {
            val result = first.readBuffer(size)
            if (first.isEmpty) buffers.removeFirst()
            return result
        }

        val result = ConcatBuffer()
        var remaining = size
        while (remaining >= 0) {
            val current = buffers.first()
            if (current.availableForRead <= remaining) {
                result.buffers.addLast(current)
                remaining -= current.availableForRead
                buffers.removeFirst()
                continue
            }

            result.buffers.addLast(current.readBuffer(remaining))
            remaining = 0
        }

        return result
    }

    override fun readByteArray(size: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun toByteArray(): ByteArray {
        val result = ByteArray(availableForRead)
        var offset = 0
        while (buffers.isNotEmpty()) {
            val current = buffers.first()
            val read = current.toByteArray()
            read.copyInto(result, offset)
            offset += read.size
            if (current.isEmpty) buffers.removeFirst()
        }

        return result
    }

    override fun clone(): ReadableBuffer {
        val result = ConcatBuffer().also {
            it.buffers.addAll(buffers.map(ReadableBuffer::clone))
            it.readIndex = readIndex
            it.capacity = capacity
        }
        return result
    }

    public fun appendLast(buffer: Buffer) {
        buffers.add(buffer)
        capacity += buffer.availableForRead
        writeIndex += buffer.availableForRead
    }

    public fun discardFirst(): ReadableBuffer {
        val result = buffers.removeFirst()
        capacity -= result.availableForRead
        writeIndex = capacity
        readIndex = max(0, readIndex - result.availableForRead)

        return result
    }

    override fun close() {
        buffers.forEach { it.close() }
    }
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*

public class Packet : Closeable {
    private val state = ArrayDeque<ReadableBuffer>()
    private var writeBuffer: Buffer = Buffer.Empty

    public var availableForRead: Int = state.sumOf { it.availableForRead }
        private set(value) {
            field = value
            validate()
        }

    public fun peek(): ReadableBuffer {
        validate()

        return state.first()
    }

    public fun validate() {
        if (state.sumOf { it.availableForRead } != availableForRead) {
            println("ERROR")
            error("ERROR")
        }
    }

    public fun readBuffer(): ReadableBuffer {
        val result = state.first()
        state.removeFirst()
        availableForRead -= result.availableForRead
        updateWriteBuffer()
        return result
    }

    public fun readBuffers(): List<ReadableBuffer> {
        val result = state.toList()
        state.clear()
        availableForRead = 0
        writeBuffer = Buffer.Empty
        return result
    }

    public fun readByte(): Byte {
        checkCanRead(1)
        val result = state.first().readByte()
        availableForRead--
        dropFirstIfNeeded()
        return result
    }

    public fun readShort(): Short {
        checkCanRead(2)
        val result = state.first().readShort()
        availableForRead -= 2
        dropFirstIfNeeded()
        return result
    }

    public fun readLong(): Long {
        checkCanRead(8)
        val result = state.first().readLong()
        availableForRead -= 8
        dropFirstIfNeeded()
        return result
    }

    public fun readInt(): Int {
        checkCanRead(4)
        val result = state.first().readInt()
        availableForRead -= 4
        dropFirstIfNeeded()
        return result
    }

    public fun indexOf(buffer: Buffer): Int {
        TODO()
    }

    public fun discard(limit: Int): Int {
        validate()
        if (limit >= availableForRead) {
            val result = availableForRead
            close()
            return result
        }

        var remaining = limit
        while (remaining > 0) {
            val current = state.first()
            if (current.availableForRead > remaining) {
                current.discard(remaining)
                remaining = 0
                break
            }

            state.removeFirst()
            remaining -= current.availableForRead
            current.close()
        }

        val result = limit - remaining
        availableForRead -= result
        return result
    }

    public fun writeBuffer(buffer: ReadableBuffer) {
        if (buffer.isEmpty) {
            buffer.close()
            return
        }

        state.addLast(buffer)
        writeBuffer = Buffer.Empty
        availableForRead += buffer.availableForRead

        validate()
    }

    public fun writeByte(value: Byte) {
        prepareWriteBuffer().writeByte(value)
        availableForRead += 1
    }

    public fun writeShort(value: Short) {
        prepareWriteBuffer().writeShort(value)
        availableForRead += 2
    }

    public fun writeInt(value: Int) {
        prepareWriteBuffer().writeInt(value)
        availableForRead += 4
    }

    public fun writeLong(value: Long) {
        prepareWriteBuffer().writeLong(value)
        availableForRead += 8
    }

    public fun toByteArray(): ByteArray {
        val result = ByteArray(availableForRead)

        var offset = 0
        for (buffer in state) {
            val array = buffer.toByteArray()
            array.copyInto(result, offset)
            offset += array.size
        }

        check(offset == availableForRead) {
            "Internal error: total read size is != available for read: $offset != $availableForRead"
        }

        state.clear()
        availableForRead = 0
        writeBuffer = Buffer.Empty

        return result
    }

    public fun readByteArray(length: Int): ByteArray {
        checkCanRead(length)

        if (state.first().availableForRead >= length) {
            val result = state.first().readByteArray(length)
            availableForRead -= length

            dropFirstIfNeeded()
            return result
        }

        val result = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val current = state.first()
            val size = minOf(current.availableForRead, length - offset)
            val chunk = current.readByteArray(size)
            chunk.copyInto(result, offset)
            offset += size
            availableForRead -= size

            dropFirstIfNeeded()
        }

        return result
    }

    public fun writeByteArray(array: ByteArray, offset: Int = 0, length: Int = array.size - offset) {
        val buffer = ByteArrayBuffer(array, offset, length)
        writeBuffer(buffer)
    }

    public fun readString(charset: Charset = Charsets.UTF_8): String {
        if (availableForRead == 0) return ""

        if (state.size == 1) {
            val buffer = state.removeFirst()
            val result = buffer.readString(charset)
            writeBuffer = Buffer.Empty
            availableForRead = 0
            return result
        }

        return buildString {
            while (state.isNotEmpty()) {
                append(state.removeFirst().readString(charset))
            }

            availableForRead = 0
            writeBuffer = Buffer.Empty
        }
    }

    public fun writeString(
        value: CharSequence,
        offset: Int = 0,
        length: Int = value.length - offset,
        charset: Charset = Charsets.UTF_8
    ) {
        writeString(value.substring(offset, offset + length), charset = charset)
    }

    public fun writeString(
        value: String,
        offset: Int = 0,
        length: Int = value.length - offset,
        charset: Charset = Charsets.UTF_8
    ) {
        if (charset == Charsets.UTF_8) {
            val data = value.encodeToByteArray(offset, offset + length)
            writeByteArray(data)
            return
        }

        TODO("Unsupported charset: $charset")
    }

    public fun readPacket(length: Int): Packet {
        checkCanRead(length)
        if (length == availableForRead) return steal()

        var remaining = length
        val result = Packet()
        while (state.isNotEmpty() && remaining > state.first().availableForRead) {
            remaining -= state.first().availableForRead
            result.writeBuffer(state.first())
            state.removeFirst()
        }

        if (remaining > 0) {
            result.writeBuffer(state.first().readBuffer(remaining))
        }

        availableForRead -= length
        updateWriteBuffer()
        return result
    }

    public fun readDouble(): Double {
        return Double.fromBits(readLong())
    }

    public fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    public fun discardExact(count: Int): Int {
        checkCanRead(count)
        return discard(count)
    }

    public fun writePacket(value: Packet) {
        state.addAll(value.state)
        availableForRead += value.availableForRead

        writeBuffer = if (value.writeBuffer.isNotFull) {
            value.writeBuffer
        } else {
            Buffer.Empty
        }

        value.state.clear()
        value.availableForRead = 0
        value.writeBuffer = Buffer.Empty
    }

    public fun writeUByte(value: UByte) {
        writeByte(value.toByte())
    }

    public fun writeDouble(value: Double) {
        writeLong(value.toBits())
    }

    public fun writeFloat(value: Float) {
        writeInt(value.toBits())
    }

    public fun steal(): Packet {
        return Packet().also { it.writePacket(this) }
    }

    public fun clone(): Packet {
        val result = Packet()
        state.forEach { result.writeBuffer(it.clone()) }
        return result
    }

    override fun close() {
        state.forEach { it.close() }
        state.clear()
        availableForRead = 0
        writeBuffer = Buffer.Empty
    }

    private fun dropFirstIfNeeded() {
        if (state.first().isEmpty) {
            state.removeFirst()
            updateWriteBuffer()
        }
    }

    private fun updateWriteBuffer() {
        if (state.isEmpty()) writeBuffer = Buffer.Empty
    }

    private fun prepareWriteBuffer(count: Int = 1): Buffer {
        if (writeBuffer.availableForWrite < count) {
            writeBuffer = createBuffer()
            state.addLast(writeBuffer)
        }

        return writeBuffer
    }

    private fun createBuffer(): Buffer = ByteArrayBuffer(ByteArray(16 * 1024)).apply {
        writeIndex = 0
    }

    public companion object {
        public val Empty: Packet = Packet()
    }
}

private fun Packet.checkCanRead(count: Int) {
    if (availableForRead < count) {
        throw EOFException("Not enough bytes available for read: $availableForRead, required: $count")
    }
}

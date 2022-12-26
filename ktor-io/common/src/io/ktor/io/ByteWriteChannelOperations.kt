/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*

public fun ByteWriteChannel.writeBuffer(buffer: ReadableBuffer) {
    checkCanWrite()
    writablePacket.writeBuffer(buffer)
}

public fun ByteWriteChannel.writePacket(packet: Packet) {
    checkCanWrite()
    writablePacket.writePacket(packet)
}

/**
 * Writes long number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeLong(l: Long) {
    checkCanWrite()
    writablePacket.writeLong(l)
}

/**
 * Writes int number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeInt(i: Int) {
    checkCanWrite()
    writablePacket.writeInt(i)
}

/**
 * Writes short number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeShort(s: Short) {
    checkCanWrite()
    writablePacket.writeShort(s)
}

/**
 * Writes byte and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeByte(b: Byte) {
    checkCanWrite()
    writablePacket.writeByte(b)
}

/**
 * Writes double number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeDouble(d: Double) {
    checkCanWrite()
    writablePacket.writeDouble(d)
}

/**
 * Writes float number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeFloat(f: Float) {
    checkCanWrite()
    writablePacket.writeFloat(f)
}

public fun ByteWriteChannel.writeByteArray(
    value: ByteArray,
    offset: Int = 0,
    length: Int = value.size - offset
) {
    checkCanWrite()
    writablePacket.writeBuffer(ByteArrayBuffer(value, offset, length))
}

public fun ByteWriteChannel.writeString(value: String, charset: Charset = Charsets.UTF_8) {
    checkCanWrite()
    writablePacket.writeString(value, charset = charset)
}

internal fun ByteWriteChannel.checkCanWrite() {
    if (!isClosedForWrite) return
    closedCause?.let { throw it }
    throw IllegalStateException("Channel is closed for writing.")
}

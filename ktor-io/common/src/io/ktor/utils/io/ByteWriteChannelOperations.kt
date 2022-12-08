/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.io.*
import io.ktor.utils.io.charsets.*

public fun ByteWriteChannel.writeBuffer(buffer: ReadableBuffer) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeBuffer(buffer)
}

public fun ByteWriteChannel.writePacket(packet: Packet) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writePacket(packet)
}

/**
 * Writes long number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeLong(l: Long) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeLong(l)
}

/**
 * Writes int number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeInt(i: Int) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeInt(i)
}

/**
 * Writes short number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeShort(s: Short) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeShort(s)
}

/**
 * Writes byte and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeByte(b: Byte) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeByte(b)
}

/**
 * Writes double number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeDouble(d: Double) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeDouble(d)
}

/**
 * Writes float number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public fun ByteWriteChannel.writeFloat(f: Float) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeFloat(f)
}

public fun ByteWriteChannel.writeByteArray(
    value: ByteArray,
    offset: Int = 0,
    length: Int = value.size - offset
) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeBuffer(ByteArrayBuffer(value, offset, length))
}

public fun ByteWriteChannel.writeString(value: String, charset: Charset = Charsets.UTF_8) {
    check(!isClosedForWrite) { "Can't write to closed channel." }
    writablePacket.writeString(value, charset = charset)
}

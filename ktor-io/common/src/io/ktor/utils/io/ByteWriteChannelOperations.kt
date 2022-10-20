/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.io.*
import io.ktor.utils.io.charsets.*

public fun ByteWriteChannel.writeBuffer(buffer: Buffer) {
    writablePacket.writeBuffer(buffer)
}

public fun ByteWriteChannel.writePacket(packet: Packet) {
    writablePacket.writePacket(packet)
}

/**
 * Writes long number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeLong(l: Long) {
    writablePacket.writeLong(l)
}

/**
 * Writes int number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeInt(i: Int) {
    writablePacket.writeInt(i)
}

/**
 * Writes short number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeShort(s: Short) {
    writablePacket.writeShort(s)
}

/**
 * Writes byte and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeByte(b: Byte) {
    writablePacket.writeByte(b)
}

/**
 * Writes double number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeDouble(d: Double) {
    writablePacket.writeDouble(d)
}

/**
 * Writes float number and suspends until written.
 * Crashes if channel get closed while writing.
 */
public suspend fun ByteWriteChannel.writeFloat(f: Float) {
    writablePacket.writeFloat(f)
}

public fun ByteWriteChannel.writeByteArray(
    value: ByteArray,
    offset: Int = 0,
    length: Int = value.size - offset
) {
    writablePacket.writeBuffer(ByteArrayBuffer(value, offset, length))
}

public suspend fun ByteWriteChannel.writeString(value: String, charset: Charset = Charsets.UTF_8) {
    writablePacket.writeString(value, charset = charset)
}

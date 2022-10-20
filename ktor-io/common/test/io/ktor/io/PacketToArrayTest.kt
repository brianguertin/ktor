/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.core.*
import kotlin.test.*

class PacketToArrayTest {
    val packet = Packet()

    @Test
    fun testEmpty() {
        assertArrayEquals(ByteArray(0), packet.toByteArray())
    }

    @Test
    fun testWriteByte() {
        packet.writeByte(42)
        val actual = packet.toByteArray()
        assertArrayEquals(ByteArray(1) { 42 }, actual)
    }

    @Test
    fun testWriteUByte() {
        packet.writeUByte(255u)
        val actual = packet.toByteArray()
        assertArrayEquals(ByteArray(1) { 255u.toByte() }, actual)
    }

    @Test
    fun testWriteEmptyPacket() {
        packet.writePacket(Packet())
        assertArrayEquals(ByteArray(0), packet.toByteArray())
    }

    @Test
    fun testWritePacketTwice() {
        val message = buildPacket {
            writeByte(90)
        }

        val expected = ByteArray(2) { 90 }
        packet.writePacket(message.clone())
        packet.writePacket(message)
        assertArrayEquals(expected, packet.toByteArray())
    }

    @Test
    fun testWriteByteAndPacket() {
        packet.writeByte(42)
        packet.writePacket(buildPacket {
            writeByte(10)
        })
        packet.writeByte(42)

        val expected = ByteArray(3).apply {
            set(0, 42)
            set(1, 10)
            set(2, 42)
        }
        assertArrayEquals(expected, packet.toByteArray())
    }

    @Test
    fun testWriteShort() {
        packet.writeShort(16004)
        val expected = ByteArray(2).apply {
            set(0, 62)
            set(1, -124)
        }
        assertArrayEquals(expected, packet.toByteArray())
    }


    @Test
    fun testWriteInt() {
        packet.writeInt(123456)
        val expected = ByteArray(4).apply {
            set(1, 1)
            set(2, -30)
            set(3, 64)
        }

        assertArrayEquals(expected, packet.toByteArray())
    }

    @Test
    fun testWriteByteArray() {
        packet.writeByteArray(ByteArray(4) { it.toByte() })
        assertArrayEquals(ByteArray(4) { it.toByte() }, packet.toByteArray())
    }
}

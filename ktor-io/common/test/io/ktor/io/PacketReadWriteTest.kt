/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.core.*
import kotlin.test.*

class PacketReadWriteTest {
    val packet = Packet()

    @Test
    fun testByte() {
        packet.writeByte(42)
        packet.writeByte(43)
        assertEquals(42, packet.readByte())
        assertEquals(43, packet.readByte())
    }

    @Test
    fun testShort() {
        packet.writeShort(42)
        assertEquals(0, packet.readByte())
        assertEquals(42, packet.readByte())
    }

    @Test
    fun testInt() {
        packet.writeInt(42)
        assertEquals(0, packet.readByte())
        assertEquals(0, packet.readByte())
        assertEquals(0, packet.readByte())
        assertEquals(42, packet.readByte())
    }

    @Test
    fun testWriteIntArrayPacketInt() {
        packet.writeInt(42)
        packet.writePacket(buildPacket {
            writeByteArray(ByteArray(1) { 77 })
        })
        packet.writeInt(42)

        assertEquals(42, packet.readInt())
        assertEquals(77, packet.readByte())
        assertEquals(42, packet.readInt())
    }

    @Test
    fun testString() {
        packet.writeString("Hello, world!")
        assertEquals("Hello, world!", packet.readString())
    }
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.errors.*
import kotlin.test.*

open class BytePacketBuildTest {
    @Test
    fun smokeSingleBufferTest() {
        val p = buildPacket {
            val ba = ByteArray(2)
            ba[0] = 0x11
            ba[1] = 0x22
            writeByteArray(ba)

            writeByte(0x12)
            writeShort(0x1234)
            writeInt(0x12345678)
            writeDouble(1.25)
            writeFloat(1.25f)
            writeLong(0x123456789abcdef0)
            writeLong(0x123456789abcdef0)

            writeString("OK\n")
        }

        assertEquals(45, p.availableForRead)
        val ba = p.readByteArray(2)

        assertEquals(0x11, ba[0])
        assertEquals(0x22, ba[1])

        assertEquals(0x12, p.readByte())
        assertEquals(0x1234, p.readShort())
        assertEquals(0x12345678, p.readInt())
        assertEquals(1.25, p.readDouble())
        assertEquals(1.25f, p.readFloat())

        val ll = (1..8).map { p.readByte().toInt() and 0xff }.joinToString()
        assertEquals("18, 52, 86, 120, 154, 188, 222, 240", ll)
        assertEquals(0x123456789abcdef0, p.readLong())

        assertEquals("OK", p.readLine())
//        assertEquals("1|2|3", p.readLine())

        assertTrue { p.isEmpty }
    }

    @Test
    fun smokeMultiBufferTest() {
        val p = buildPacket {
            writeByteArray(ByteArray(9999))
            writeByte(0x12)
            writeShort(0x1234)
            writeInt(0x12345678)
            writeDouble(1.25)
            writeFloat(1.25f)
            writeLong(0x123456789abcdef0)

            writeString("OK\n")
        }

        assertEquals(10034, p.availableForRead)

        val ba = p.readByteArray(9999)
        assertEquals(0x12, p.readByte())
        assertEquals(0x1234, p.readShort())
        assertEquals(0x12345678, p.readInt())
        assertEquals(1.25, p.readDouble())
        assertEquals(1.25f, p.readFloat())
        assertEquals(0x123456789abcdef0, p.readLong())

        assertEquals("OK", p.readLine())
        assertEquals("1|2|3", p.readLine())
        assertTrue { p.isEmpty }
    }

    @Test
    fun testSingleBufferSkipTooMuch() {
        val p = buildPacket {
            writeByteArray(ByteArray(9999))
        }

        assertEquals(9999, p.discard(10000))
        assertTrue { p.isEmpty }
    }

    @Test
    fun testSingleBufferSkip() {
        val p = buildPacket {
            writeByteArray("ABC123".toByteArray0())
        }

        assertEquals(3, p.discard(3))
        assertEquals("123", p.readLine())
        assertTrue { p.isEmpty }
    }

    @Test
    fun testSingleBufferSkipExact() {
        val p = buildPacket {
            writeByteArray("ABC123".toByteArray0())
        }

        p.discardExact(3)
        assertEquals("123", p.readLine())
        assertTrue { p.isEmpty }
    }

    @Test
    fun testSingleBufferSkipExactTooMuch() {
        val p = buildPacket {
            writeByteArray("ABC123".toByteArray0())
        }

        assertFailsWith<EOFException> {
            p.discardExact(1000)
        }
        assertTrue { p.isEmpty }
    }

    @Test
    fun testMultiBufferSkipTooMuch() {
        val p = buildPacket {
            writeByteArray(ByteArray(99999))
        }

        assertEquals(99999, p.discard(1000000))
        assertTrue { p.isEmpty }
    }

    @Test
    fun testMultiBufferSkip() {
        val p = buildPacket {
            writeByteArray(ByteArray(99999))
            writeByteArray("ABC123".toByteArray0())
        }

        assertEquals(99999 + 3, p.discard(99999 + 3))
        assertEquals("123", p.readLine())
        assertTrue { p.isEmpty }
    }

    @Test
    fun testNextBufferBytesStealing() {
        val p = buildPacket {
            repeat(PACKET_BUFFER_SIZE + 3) {
                writeByte(1)
            }
        }

        assertEquals(PACKET_BUFFER_SIZE + 3, p.availableForRead)
        p.readByteArray(PACKET_BUFFER_SIZE - 1)
        assertEquals(0x01010101, p.readInt())
        assertTrue { p.isEmpty }
    }

    @Test
    fun testNextBufferBytesStealingFailed() {
        val p = buildPacket {
            repeat(PACKET_BUFFER_SIZE + 1) {
                writeByte(1)
            }
        }

        p.readByteArray(PACKET_BUFFER_SIZE - 1)

        try {
            p.readInt()
            fail()
        } catch (_: EOFException) {
        } finally {
            p.close()
        }
    }

    @Test
    fun testReadByteEmptyPacket() {
        assertFailsWith<EOFException> {
            Packet.Empty.readByte()
        }

        assertFailsWith<EOFException> {
            val p = buildPacket {
                writeInt(1)
            }

            try {
                p.readInt()
                p.readByte()
            } finally {
                p.close()
            }
        }
    }

    @Test
    fun testRecycleInTheRightPool() {
        val packet = Packet("hello".encodeToByteArray())
        val result = buildPacket { writePacket(packet) }
        result.close()
    }

    private inline fun buildPacket(block: Packet.() -> Unit): Packet {
        val builder = Packet()
        try {
            block(builder)
            return builder
        } catch (t: Throwable) {
            builder.close()
            throw t
        }
    }

    private fun String.toByteArray0(): ByteArray {
        val result = ByteArray(length)

        for (i in indices) {
            val v = this[i].code and 0xff
            if (v > 0x7f) fail()
            result[i] = v.toByte()
        }

        return result
    }

    companion object {
        const val PACKET_BUFFER_SIZE: Int = 4096
    }
}

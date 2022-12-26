/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*
import kotlin.test.*

open class BytePacketStringTest {
    @Test
    fun testSingleBufferReadText() {
        val p = buildPacket {
            writeString("ABC")
        }

        assertEquals("ABC", p.readString())
    }

    @Test
    fun testSingleBufferMultibyteReadText() {
        val p = buildPacket {
            writeString("ABC\u0422")
        }

        assertEquals("ABC\u0422", p.readString())
    }

    @Test
    fun testMultiBufferReadText() {
        val size = 100000
        val ba = ByteArray(size) {
            'x'.code.toByte()
        }
        val s = CharArray(size) {
            'x'
        }.joinToString("")

        val packet = buildPacket {
            writeByteArray(ba)
        }

        assertEquals(s, packet.readString())
    }

    @Test
    fun testDecodePacketSingleByte() {
        val packet = buildPacket {
            writeString("1")
        }

        try {
            assertEquals("1", Charsets.UTF_8.newDecoder().decodePacket(packet))
        } finally {
            packet.close()
        }
    }

    @Test
    fun testDecodePacketMultiByte() {
        val packet = buildPacket {
            writeString("\u0422")
        }

        try {
            assertEquals("\u0422", Charsets.UTF_8.newDecoder().decodePacket(packet))
        } finally {
            packet.close()
        }
    }

    @Test
    fun testDecodePacketMultiByteSeveralCharacters() {
        val packet = buildPacket {
            writeString("\u0422e\u0438")
        }

        try {
            assertEquals("\u0422e\u0438", Charsets.UTF_8.newDecoder().decodePacket(packet))
        } finally {
            packet.close()
        }
    }

    @Test
    fun testEncode() {
        assertTrue { byteArrayOf(0x41).contentEquals(Charsets.UTF_8.newEncoder().encode("A").toByteArray()) }
        assertTrue {
            byteArrayOf(0x41, 0x42, 0x43).contentEquals(Charsets.UTF_8.newEncoder().encode("ABC").toByteArray())
        }
        assertTrue {
            byteArrayOf(0xd0.toByte(), 0xa2.toByte(), 0x41, 0xd0.toByte(), 0xb8.toByte()).contentEquals(
                Charsets.UTF_8.newEncoder().encode("\u0422A\u0438").toByteArray()
            )
        }
    }

    @Test
    fun testToByteArray() {
        assertEquals(
            byteArrayOf(0xF0.toByte(), 0xA6.toByte(), 0x88.toByte(), 0x98.toByte()).hexdump(),
            "\uD858\uDE18".toByteArray().hexdump()
        )
    }

    @Test
    fun testEncodeToByteArraySequence() {
        assertEquals(
            byteArrayOf(0xF0.toByte(), 0xA6.toByte(), 0x88.toByte(), 0x98.toByte()).hexdump(),
            Charsets.UTF_8.newEncoder().encodeToByteArray(
                StringBuilder().apply { append("\uD858\uDE18") }
            ).hexdump()
        )
    }

    @Test
    fun stringCtor() {
        val bytes = byteArrayOf(0xF0.toByte(), 0xA6.toByte(), 0x88.toByte(), 0x98.toByte())
        val actual = String(bytes)

        assertEquals("\uD858\uDE18", actual)
    }

    @Test
    fun stringConstructorFromSlice() {
        val helloString = "Hello, world"
        val helloBytes = helloString.toByteArray()

        assertEquals("Hello", String(helloBytes, 0, 5))
        assertEquals("ello", String(helloBytes, 1, 4))
        assertEquals("ello, ", String(helloBytes, 1, 6))
        assertEquals("world", String(helloBytes, 7, 5))
    }

    @Test
    fun stringCtorEmpty() {
        val actual = String(ByteArray(0))
        assertEquals("", actual)
    }

    @Test
    fun testStringCtorRange() {
        assertEquals("@C", String(byteArrayOf(64, 64, 67, 67), length = 2, offset = 1))
    }

    private fun ByteArray.hexdump() = joinToString(separator = " ") { (it.toInt() and 0xff).toString(16) }
}

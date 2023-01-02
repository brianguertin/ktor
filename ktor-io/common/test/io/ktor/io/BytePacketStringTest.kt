/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

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
    fun testToByteArray() {
        assertEquals(
            byteArrayOf(0xF0.toByte(), 0xA6.toByte(), 0x88.toByte(), 0x98.toByte()).hexdump(),
            "\uD858\uDE18".toByteArray().hexdump()
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

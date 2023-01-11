/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlin.test.*

class PacketTest {

    @Test
    fun testReadByteArrayFromChunked() {
        val packet = buildPacket {
            writeByteArray(byteArrayOf(1))
            writeByteArray(byteArrayOf(2))
            writeByteArray(byteArrayOf(3))
            writeByteArray(byteArrayOf(1, 2))
        }

        val result = packet.readByteArray(4)
        assertArrayEquals(byteArrayOf(1, 2, 3, 1), result)
        assertEquals(1, packet.availableForRead)

        val result2 = packet.readByteArray(1)
        assertArrayEquals(byteArrayOf(2), result2)

        assertEquals(0, packet.availableForRead)
    }

    @Test
    fun testIndexOfEmpty() {
        val emptyPacket = buildPacket { }
        val packet = buildPacket {
            writeByte(1)
            writeByte(2)
            writeByte(3)
        }

        assertEquals(0, emptyPacket.indexOf(ReadableBuffer.Empty))
        assertEquals(0, packet.indexOf(ReadableBuffer.Empty))
    }

    @Test
    fun testIndexOfSingle() {
        val packet = buildPacket {
            writeByte(1)
            writeByte(2)
            writeByte(3)
        }

        assertEquals(0, packet.indexOf(ByteArrayBuffer(byteArrayOf(1))))
        assertEquals(1, packet.indexOf(ByteArrayBuffer(byteArrayOf(2))))
        assertEquals(2, packet.indexOf(ByteArrayBuffer(byteArrayOf(3))))

        assertEquals(-1, packet.indexOf(ByteArrayBuffer(byteArrayOf(4))))
    }

    @Test
    fun testIndexOfMultiple() {
        val packet = buildPacket {
            repeat(1000) {
                writeByte(it.toByte())
            }
        }

        assertEquals(0, packet.indexOf(ByteArrayBuffer(byteArrayOf(0, 1))))
        assertEquals(1, packet.indexOf(ByteArrayBuffer(byteArrayOf(1, 2))))
        assertEquals(3, packet.indexOf(ByteArrayBuffer(byteArrayOf(3, 4))))
        assertEquals(-1, packet.indexOf(ByteArrayBuffer(byteArrayOf(3, 5))))
    }

    @Test
    fun testByteFragmented() {
        val packet = buildPacket {
            repeat(1000) {
                writeByteArray(byteArrayOf(it.toByte()))
            }
        }

        assertEquals(0, packet.indexOf(ByteArrayBuffer(byteArrayOf(0, 1))))
        assertEquals(1, packet.indexOf(ByteArrayBuffer(byteArrayOf(1, 2))))
        assertEquals(3, packet.indexOf(ByteArrayBuffer(byteArrayOf(3, 4))))
        assertEquals(-1, packet.indexOf(ByteArrayBuffer(byteArrayOf(3, 5))))

        assertEquals(1, packet.indexOf(ByteArrayBuffer(byteArrayOf(1, 2, 3, 4))))
    }
}

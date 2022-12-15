/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*
import kotlin.test.*

class BytePacketReadTest {
    @Test
    fun testReadText() {
        val packet = buildPacket {
            writeByte(0xc6.toByte())
            writeByte(0x86.toByte())
        }

        assertEquals("\u0186", packet.readString(charset = Charsets.UTF_8))
        assertEquals(0, packet.availableForRead)
    }

    @Test
    fun testReadBytesAll() {
        val pkt = buildPacket {
            writeInt(0x01020304)
        }

        try {
            assertTrue { byteArrayOf(1, 2, 3, 4).contentEquals(pkt.toByteArray()) }
        } finally {
            pkt.close()
        }
    }

    @Test
    fun testReadBytesExact1() {
        val pkt = buildPacket {
            writeInt(0x01020304)
        }

        try {
            assertTrue { byteArrayOf(1, 2, 3, 4).contentEquals(pkt.readByteArray(4)) }
        } finally {
            pkt.close()
        }
    }

    @Test
    fun testReadBytesExact2() {
        val pkt = buildPacket {
            writeInt(0x01020304)
        }

        try {
            assertTrue { byteArrayOf(1, 2).contentEquals(pkt.readByteArray(2)) }
        } finally {
            pkt.close()
        }
    }

    @Test
    fun testReadBytesExact3() {
        val pkt = buildPacket {
            writeInt(0x01020304)
        }

        try {
            assertTrue { byteArrayOf().contentEquals(pkt.readByteArray(0)) }
        } finally {
            pkt.close()
        }
    }

    @Test
    fun testReadBytesExactFails() {
        val pkt = buildPacket {
            writeInt(0x01020304)
        }

        try {
            assertFails {
                pkt.readByteArray(9)
            }
        } finally {
            pkt.close()
        }
    }

    @Test
    fun testReadBytesOf1() {
        val pkt = buildPacket {
            writeInt(0x01020304)
        }

        try {
            assertTrue { byteArrayOf(1, 2, 3).contentEquals(pkt.readByteArray(3)) }
        } finally {
            pkt.close()
        }
    }
}

/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import java.nio.*
import kotlin.test.*

class BytePacketBuildTestExtended {
    @Test
    fun smokeSingleBufferTestExtended() {
        val p = buildPacket {
            writeByteArray(ByteArray(2))
            writeByteBuffer(ByteBuffer.allocate(3))

            writeByte(0x12)
            writeShort(0x1234)
            writeInt(0x12345678)
            writeDouble(1.23)
            writeFloat(1.23f)
            writeLong(0x123456789abcdef0)

            writeString("OK\n")
        }

        assertEquals(35, p.availableForRead)

        p.readByteArray(2)
        p.readByteArray(3)

        assertEquals(0x12, p.readByte())
        assertEquals(0x1234, p.readShort())
        assertEquals(0x12345678, p.readInt())
        assertEquals(1.23, p.readDouble())
        assertEquals(1.23f, p.readFloat())
        assertEquals(0x123456789abcdef0, p.readLong())

        assertEquals("OK\n", p.readString())
    }

    @Test
    fun smokeMultiBufferTestExtended() {
        val p = buildPacket {
            writeByteArray(ByteArray(9999))
            writeByteBuffer(ByteBuffer.allocate(8888))
            writeByte(0x12)
            writeShort(0x1234)
            writeInt(0x12345678)
            writeDouble(1.23)
            writeFloat(1.23f)
            writeLong(0x123456789abcdef0)

            writeString("OK\n")
            writeString("1|2|3")
        }

        assertEquals(18922, p.availableForRead)

        p.readByteArray(9999)
        p.readByteArray(8888)
        assertEquals(0x12, p.readByte())
        assertEquals(0x1234, p.readShort())
        assertEquals(0x12345678, p.readInt())
        assertEquals(1.23, p.readDouble())
        assertEquals(1.23f, p.readFloat())
        assertEquals(0x123456789abcdef0, p.readLong())

        assertEquals("OK\n1|2|3", p.readString())
    }
}

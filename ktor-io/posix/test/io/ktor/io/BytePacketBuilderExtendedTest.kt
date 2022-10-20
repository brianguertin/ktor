/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import kotlin.test.*

class BytePacketBuilderExtendedTest : BytePacketBuildTest() {
    private val buffer = nativeHeap.allocArray<ByteVar>(8888)

    @Test
    fun smokeSingleBufferTestExtended() {
        val p = buildPacket {
            writeByteArray(ByteArray(2))
            writeCPointer(buffer, 3)

            writeByte(0x12)
            writeShort(0x1234)
            writeInt(0x12345678)
            writeDouble(1.23)
            writeFloat(1.23f)
            writeLong(0x123456789abcdef0)

            writeString("OK\n")
        }

        assertEquals(40, p.availableForRead)

        p.readByteArray(2)
        p.readCPointer(buffer, 3)

        assertEquals(0x12, p.readByte())
        assertEquals(0x1234, p.readShort())
        assertEquals(0x12345678, p.readInt())
        assertEquals(1.23, p.readDouble())
        assertEquals(1.23f, p.readFloat())
        assertEquals(0x123456789abcdef0, p.readLong())

        assertEquals("OK", p.readLine())
        assertEquals("1|2|3", p.readLine())
    }

    @Test
    fun smokeMultiBufferTestExtended() {
        val p = buildPacket {
            writeByteArray(ByteArray(9999))
            writeCPointer(buffer, 8888)
            writeByte(0x12)
            writeShort(0x1234)
            writeInt(0x12345678)
            writeDouble(1.23)
            writeFloat(1.23f)
            writeLong(0x123456789abcdef0)

            writeString("OK\n")
        }

        assertEquals(18922, p.availableForRead)

        p.readByteArray(9999)
        p.readCPointer(buffer, 8888)
        assertEquals(0x12, p.readByte())
        assertEquals(0x1234, p.readShort())
        assertEquals(0x12345678, p.readInt())
        assertEquals(1.23, p.readDouble())
        assertEquals(1.23f, p.readFloat())
        assertEquals(0x123456789abcdef0, p.readLong())

        assertEquals("OK", p.readLine())
        assertEquals("1|2|3", p.readLine())
    }
}

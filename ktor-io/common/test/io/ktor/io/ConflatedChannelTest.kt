/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlin.test.*

class ConflatedChannelTest {

    @Test
    fun testReadFromEmpty() = testSuspend {
        val channel = ByteReadChannel { }

        with(channel) {
            assertFailsWith<EOFException> {
                readByte()
            }
            assertFailsWith<EOFException> {
                readShort()
            }
            assertFailsWith<EOFException> {
                readInt()
            }
            assertFailsWith<EOFException> {
                readLong()
            }

            assertEquals(ReadableBuffer.Empty, readBuffer())
        }
    }

    @Test
    fun testReadByte() = testSuspend {
        fun channel() = ByteReadChannel {
            writeByte(1)
        }

        with(channel()) {
            assertEquals(1, readByte().toInt())
            assertFailsWith<EOFException> {
                readByte()
            }
        }

        with(channel()) {
            assertFailsWith<EOFException> {
                readShort()
            }

            assertEquals(1, readByte().toInt())
        }
    }

    @Test
    fun testReadByteFromBuffer() = testSuspend {
        fun channel() = ByteReadChannel {
            writeByte(1)
        }

        with(channel()) {
            assertEquals(1, readBuffer().readByte().toInt())
        }
    }

    @Test
    fun testWriteByteArray() = testSuspend {
        val channel = ByteReadChannel {
            writeByteArray(byteArrayOf(1, 2, 3))
        }

        assertEquals(1, channel.readByte())
        assertEquals(2, channel.readByte())
        assertEquals(3, channel.readByte())

        assertFailsWith<EOFException> {
            channel.readByte()
        }
    }

    @Test
    fun testReadBuffer() = testSuspend {
        val channel = ByteReadChannel {
            writeByte(1)
        }

        val buffer = channel.readBuffer()
        assertEquals(1, buffer.availableForRead)
        assertEquals(1, buffer.readByte().toInt())
    }

    @Test
    fun testWriteToClosedChannel() = testSuspend {
        val channel = ConflatedByteChannel()
        channel.close()

        assertFailsWith<IllegalStateException> {
            channel.writeByte(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeShort(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeInt(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeLong(1)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeDouble(1.0)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeFloat(1.0f)
        }

        assertFailsWith<IllegalStateException> {
            channel.writeByteArray(ByteArray(0))
        }

        assertFailsWith<IllegalStateException> {
            channel.writeBuffer(Buffer.Empty)
        }

        assertFailsWith<IllegalStateException> {
            channel.writePacket(Packet())
        }

        assertFailsWith<IllegalStateException> {
            channel.writeString("")
        }

        Unit
    }
}

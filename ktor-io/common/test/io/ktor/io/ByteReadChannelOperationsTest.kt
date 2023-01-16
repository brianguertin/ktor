/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlinx.coroutines.*
import kotlin.test.*

class ByteReadChannelExtensionsTest {

    @Test
    fun testReadUtf8LineTo() = testSuspend {
        val channel = ByteReadChannel(
            buildString {
                append("GET / HTTP/1.1\n")
                append("Host: 127.0.0.1:9090\n")
                append("Accept-Charset: UTF-8\n")
                append("Accept: */*\n")
                append("User-Agent: Ktor client\n")
            }
        )

        channel.stringReader { reader ->
            assertEquals("GET / HTTP/1.1", reader.readLine())
            assertEquals("Host: 127.0.0.1:9090", reader.readLine())
            assertEquals("Accept-Charset: UTF-8", reader.readLine())
            assertEquals("Accept: */*", reader.readLine())
            assertEquals("User-Agent: Ktor client", reader.readLine())

            assertFalse(reader.readLineTo(StringBuilder()))
        }
    }

    @Test
    fun testReadUtf8LineWithCaretTo() = testSuspend {
        val channel = ByteReadChannel(
            buildString {
                append("GET / HTTP/1.1\r\n")
                append("Host: 127.0.0.1:9090\r\n")
                append("Accept-Charset: UTF-8\r\n")
                append("Accept: */*\r\n")
                append("User-Agent: Ktor client\r\n")
            }
        )

        channel.stringReader { reader ->
            assertEquals("GET / HTTP/1.1", reader.readLine())
            assertEquals("Host: 127.0.0.1:9090", reader.readLine())
            assertEquals("Accept-Charset: UTF-8", reader.readLine())
            assertEquals("Accept: */*", reader.readLine())
            assertEquals("User-Agent: Ktor client", reader.readLine())

            assertFalse(reader.readLineTo(StringBuilder()))
        }
    }

    @Test
    fun testReadPacket() = testSuspend {
        val channel = ByteReadChannel {
            writeByteArray(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        }

        val packet = channel.readPacket(4)
        assertEquals(4, packet.availableForRead)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), packet.toByteArray())
        assertEquals(6, channel.availableForRead)
    }

    @Test
    fun testReadPacketChunked() = testSuspend {
        val channel = ByteReadChannel {
            repeat(10) {
                val value = it + 1
                writeByteArray(byteArrayOf(value.toByte()))
            }
        }

        val packet = channel.readPacket(4)
        assertEquals(4, packet.availableForRead)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), packet.toByteArray())
        assertEquals(6, channel.availableForRead)
    }

    @Test
    fun testCopyToFromClosed() = testSuspend {
        val channel = ByteReadChannel {
            writeByteArray(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        }

        val out = ConflatedByteChannel()
        val result = async {
            assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), out.toByteArray())
        }

        channel.copyAndClose(out)
        result.await()
    }

    @Test
    fun testCopyToFromClosedWithLimit() = testSuspend {
        val channel = ByteReadChannel {
            writeByteArray(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        }

        val out = ConflatedByteChannel()
        val result = async {
            assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), out.toByteArray())
        }

        channel.copyAndClose(out, limit = 5)
        result.await()
    }

    @Test
    fun testCopyTo() = testSuspend {
        val latch = Job()
        val channel = ByteReadChannel {
            writeByteArray(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
            flush()
            latch.join()
            writeByte(42)
        }

        val out = ConflatedByteChannel()
        val result = async {
            assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), out.toByteArray())
        }
        channel.copyAndClose(out, limit = 10)
        result.await()
        latch.complete()

        assertEquals(42, channel.readByte())
    }
}

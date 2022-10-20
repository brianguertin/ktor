/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.http.cio

import io.ktor.http.cio.*
import io.ktor.io.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.*
import kotlin.test.*
import kotlin.text.String
import kotlin.text.toByteArray

class ChunkedTest {
    @Test(expected = EOFException::class)
    fun testEmptyBroken() = runBlocking {
        val bodyText = ""
        val ch = ByteReadChannel(bodyText.toByteArray())
        ByteReadChannel {
            decodeChunked(ch, this)
        }.toByteArray()
    }

    @Test
    fun testChunkedWithContentLength() = runBlocking {
        val chunkedContent = listOf(
            "3\r\n",
            "a=1\r\n",
            "0\r\n",
            "\r\n",
        )

        val input = writer {
            chunkedContent.forEach {
                channel.writeString(it)
            }
        }

        val output = writer {
            decodeChunked(input, channel)
        }

        val content = output.readRemaining().readString()
        assertEquals("a=1", content)
    }

    @Test(expected = EOFException::class)
    fun testEmptyWithoutCrLf() = runBlocking {
        val bodyText = "0"
        val ch = ByteReadChannel(bodyText.toByteArray())

        ByteReadChannel {
            decodeChunked(ch, this)
        }.toByteArray()
    }

    @Test
    fun testEmpty() = runBlocking {
        val bodyText = "0\r\n\r\n"
        val ch = ByteReadChannel(bodyText.toByteArray())

        val parsed = ByteReadChannel {
            decodeChunked(ch, this)
        }

        assertEquals(0, parsed.availableForRead)
        assertTrue { parsed.isClosedForRead }
    }

    @Test
    fun testEmptyWithTrailing() = runBlocking {
        val bodyText = "0\r\n\r\ntrailing"
        val ch = ByteReadChannel(bodyText.toByteArray())
        val parsed = ByteReadChannel {
            decodeChunked(ch, this)
        }

        assertEquals(0, parsed.availableForRead)
        assertTrue { parsed.isClosedForRead }
        assertEquals("trailing", ch.readRemaining().readString())
    }

    @Test
    fun testContent() = runBlocking {
        val bodyText = "3\r\n123\r\n0\r\n\r\n"
        val ch = ByteReadChannel(bodyText.toByteArray())

        val parsed = ByteReadChannel {
            decodeChunked(ch, this)
        }

        assertEquals("123", parsed.readLine())
    }

    @Test
    fun testContentMultipleChunks() = runBlocking {
        val bodyText = "3\r\n123\r\n2\r\n45\r\n1\r\n6\r\n0\r\n\r\n"
        val ch = ByteReadChannel(bodyText.toByteArray())
        val parsed = ByteReadChannel {
            decodeChunked(ch, this)
        }

        assertEquals("123456", parsed.readLine())
    }

    @Test
    fun testContentMixedLineEndings() = runBlocking {
        val bodyText = "3\n123\n2\r\n45\r\n1\r6\r0\r\n\n"
        val ch = ByteReadChannel(bodyText.toByteArray())
        val parsed = ByteReadChannel {
            decodeChunked(ch, this)
        }

        assertEquals("123456", parsed.readLine())
    }

    @Test
    fun testEncodeEmpty() = runBlocking {
        val encoded = ByteReadChannel {
            encodeChunked(this, ByteReadChannel.Empty)
        }

        yield()
        val encodedText = encoded.readRemaining().readString()
        assertEquals("0\r\n\r\n", encodedText)
    }
}

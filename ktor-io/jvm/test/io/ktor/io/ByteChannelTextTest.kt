/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.*
import kotlin.test.*

class ByteChannelTextTest {

    @Test
    fun testReadUtf8LineThrowTooLongLine() = runBlocking<Unit> {
        val line100 = (0..99).joinToString("")
        val channel = ByteReadChannel {
            writeString(line100)
        }

        assertFailsWith<TooLongLineException> {
            channel.readLine(limit = 50)
        }
    }

    @Test
    fun testReadUtf8Line32k() = runBlocking {
        val line = "x".repeat(32 * 1024)
        val bytes = line.encodeToByteArray()
        val channel = ByteReadChannel(bytes)

        val result = channel.readLine()
        assertEquals(line, result)
    }

    @Test
    fun testReadLineUtf8Chunks() = runBlocking {
        val line = "x".repeat(32 * 1024)
        val channel = writer {
            writeString(line)
        }

        val result = channel.readLine()
        assertEquals(line, result)
    }

    @Test
    fun test2EmptyLines() {
        val text = ByteReadChannel("\r\n\r\n")

        runBlocking {
            assertEquals("", text.readLine())
            assertEquals("", text.readLine())
            assertNull(text.readLine())
        }
    }
}

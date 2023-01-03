/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.jvm.javaio.*
import kotlin.test.*

class InputStreamAdapterTest {

    @Test
    fun testReadChannelAsStream() {
        val stream = ByteReadChannel("Hello, world!").toInputStream()

        val result = StringBuilder()
        while (true) {
            val byte = stream.read()
            if (byte == -1) break
            result.append(byte.toChar())
        }

        assertEquals("Hello, world!", result.toString())
    }

    @Test
    fun testReadChannelAsStreamWithArray() {
        val stream = ByteReadChannel("Hello, world!").toInputStream()

        val result = StringBuilder()
        val buffer = ByteArray(1)
        while (true) {
            val read = stream.read(buffer)
            if (read == -1) break
            result.append(buffer[0].toInt().toChar())
        }

        assertEquals("Hello, world!", result.toString())
    }

    @Test
    fun testReadWithBiggerArray() {
        val stream = ByteReadChannel("Hello, world!").toInputStream()
        val buffer = ByteArray(100)
        val read = stream.read(buffer)
        val result = String(buffer, 0, read)
        assertEquals("Hello, world!", result)

        assertEquals(-1, stream.read(buffer))
    }

    @Test
    fun testChannelIsCancelled() {
        val channel = ConflatedByteChannel()
        val stream = channel.toInputStream()
        channel.cancel(Exception("Expected"))

        val error = assertFailsWith<Exception> {
            stream.read()
        }

        assertEquals("Expected", error.message)
    }
}

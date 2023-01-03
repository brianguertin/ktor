/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlin.test.*

class OutputStreamAdapterTest {
    @Test
    fun testWriteMessage() = runBlocking {
        val message = "Hello, world!"

        val channel = ByteReadChannel {
            toOutputStream().writer().use {
                it.write(message)
            }
        }

        assertEquals(message, channel.readString())
    }

    @Test
    fun testFlush(): Unit = runBlocking {
        val channel = ConflatedByteChannel()
        val stream = channel.toOutputStream()

        val result = async {
            channel.readByte()
        }

        stream.write(42)
        assertTrue(result.isActive)
        stream.flush()

        assertEquals(42, result.await())

        async {
            assertFailsWith<EOFException> {
                channel.readByte()
            }
        }

        stream.close()
    }
}

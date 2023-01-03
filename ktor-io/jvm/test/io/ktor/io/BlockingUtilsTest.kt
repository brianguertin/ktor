/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.jvm.javaio.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import kotlin.test.*

class BlockingUtilsTest {

    @Test
    fun testCopyToOutputStream() = runBlocking {
        val channel = ByteReadChannel("Hello, world!")
        val result = ByteArrayOutputStream()
        channel.copyTo(result)

        assertEquals("Hello, world!", result.toString())
    }

    @Test
    fun testCopyToOutputStreamWithLimit() = runBlocking {
        val channel = ByteReadChannel("Hello, world!")
        val result = ByteArrayOutputStream()

        channel.copyTo(result, limit = 1)
        assertEquals("H", result.toString())

        channel.copyTo(result, limit = 1)
        assertEquals("He", result.toString())
    }
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
import io.ktor.utils.io.*
import kotlin.test.*

class ByteReadChannelExtensionsTest {

    @Test
    fun testReadUtf8LineTo() = testSuspend {
        val channel = ByteReadChannel(buildString {
            append("GET / HTTP/1.1\n")
            append("Host: 127.0.0.1:9090\n")
            append("Accept-Charset: UTF-8\n")
            append("Accept: */*\n")
            append("User-Agent: Ktor client\n")
        })

        assertEquals("GET / HTTP/1.1", channel.readLine())
        assertEquals("Host: 127.0.0.1:9090", channel.readLine())
        assertEquals("Accept-Charset: UTF-8", channel.readLine())
        assertEquals("Accept: */*", channel.readLine())
        assertEquals("User-Agent: Ktor client", channel.readLine())

        assertFalse(channel.readUTF8LineTo(StringBuilder()))
    }


    @Test
    fun testReadUtf8LineWithCarretTo() = testSuspend {
        val channel = ByteReadChannel(buildString {
            append("GET / HTTP/1.1\r\n")
            append("Host: 127.0.0.1:9090\r\n")
            append("Accept-Charset: UTF-8\r\n")
            append("Accept: */*\r\n")
            append("User-Agent: Ktor client\r\n")
        })

        assertEquals("GET / HTTP/1.1", channel.readLine())
        assertEquals("Host: 127.0.0.1:9090", channel.readLine())
        assertEquals("Accept-Charset: UTF-8", channel.readLine())
        assertEquals("Accept: */*", channel.readLine())
        assertEquals("User-Agent: Ktor client", channel.readLine())

        assertFalse(channel.readUTF8LineTo(StringBuilder()))
    }

    suspend fun ByteReadChannel.readLine(): String = buildString {
        assertTrue(readUTF8LineTo(this))
    }
}

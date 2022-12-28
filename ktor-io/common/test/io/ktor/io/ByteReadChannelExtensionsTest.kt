/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.test.dispatcher.*
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

        channel.stringReader().use {
            assertEquals("GET / HTTP/1.1", it.readLine())
            assertEquals("Host: 127.0.0.1:9090", it.readLine())
            assertEquals("Accept-Charset: UTF-8", it.readLine())
            assertEquals("Accept: */*", it.readLine())
            assertEquals("User-Agent: Ktor client", it.readLine())

            assertFalse(it.readLineTo(StringBuilder()))
        }
    }

    @Test
    fun testReadUtf8LineWithCarretTo() = testSuspend {
        val channel = ByteReadChannel(
            buildString {
                append("GET / HTTP/1.1\r\n")
                append("Host: 127.0.0.1:9090\r\n")
                append("Accept-Charset: UTF-8\r\n")
                append("Accept: */*\r\n")
                append("User-Agent: Ktor client\r\n")
            }
        )

        channel.stringReader().use {
            assertEquals("GET / HTTP/1.1", it.readLine())
            assertEquals("Host: 127.0.0.1:9090", it.readLine())
            assertEquals("Accept-Charset: UTF-8", it.readLine())
            assertEquals("Accept: */*", it.readLine())
            assertEquals("User-Agent: Ktor client", it.readLine())

            assertFalse(it.readLineTo(StringBuilder()))
        }
    }
}

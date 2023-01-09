/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.http.cio

import io.ktor.http.cio.*
import io.ktor.io.*
import io.ktor.test.dispatcher.*
import kotlin.test.*

class ResponseParserTest {
    @Test
    fun parseStatusCodeShouldBeValid(): Unit = testSuspend {
        listOf(
            """
            HTTP/1.1 100 OK
            """.trimIndent(),
            """
            HTTP/1.1 999 OK
            """.trimIndent()
        ).forEach {
            val response = parseResponse(ByteReadChannel(it))!!
            assertEquals("OK", response.statusText.toString())
        }
    }

    @Test
    fun parseStatusCodeShouldFailWhenOutOfRange(): Unit = testSuspend {
        assertFailsWith<ParserException> {
            parseResponse(ByteReadChannel("HTTP/1.1 0 OK"))!!
        }
        assertFailsWith<ParserException> {
            parseResponse(ByteReadChannel("HTTP/1.1 99 OK"))!!
        }
        assertFailsWith<ParserException> {
            parseResponse(ByteReadChannel("HTTP/1.1 1000 OK"))!!
        }
    }

    @Test
    fun parseStatusCodeShouldFailWhenStatusCodeIsNegative(): Unit = testSuspend {
        assertFailsWith<NumberFormatException> {
            parseResponse(ByteReadChannel("HTTP/1.1 -100 OK"))!!
        }
    }

    @Test
    fun testInvalidResponse(): Unit = testSuspend {
        val cases = listOf("A", "H", "a")

        for (case in cases) {
            assertFailsWith<ParserException> {
                parseResponse(ByteReadChannel(case))
            }
        }
    }
}

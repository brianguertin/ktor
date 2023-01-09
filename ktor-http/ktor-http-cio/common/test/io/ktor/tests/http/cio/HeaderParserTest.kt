/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.http.cio

import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.io.*
import io.ktor.test.dispatcher.*
import kotlin.test.*

private const val HTAB: Char = '\u0009'

class HeaderParserTest {

    @Test
    fun parseHeadersSmokeTest(): Unit = testSuspend {
        val encodedHeaders = """
            name: value
            name2:${HTAB}p1${HTAB}p2 p3$HTAB
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)
        val headers = parseHeaders(channel)

        try {
            assertEquals(2, headers.size)
            assertEquals("value", headers["name"].toString())
            assertEquals("p1${HTAB}p2 p3", headers["name2"].toString())
        } finally {
            headers.release()
        }
    }

    @Test
    fun testParseCookieHeader() = testSuspend {
        val rawHeaders = "Set-Cookie: ___utmvazauvysSB=kDu\u0001xSkE; path=/; Max-Age=900\r\n\r\n"

        val channel = ByteReadChannel(rawHeaders)
        val headers = parseHeaders(channel)

        try {
            val actual = headers[HttpHeaders.SetCookie].toString()
            assertEquals("___utmvazauvysSB=kDu\u0001xSkE; path=/; Max-Age=900", actual)
        } finally {
            headers.release()
        }
    }

    @Test
    fun parseHeadersNoLeadingSpace(): Unit = testSuspend {
        val encodedHeaders = """
            name:value
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)
        val headers = parseHeaders(channel)

        try {
            assertEquals(1, headers.size)
            assertEquals("value", headers["name"].toString())
        } finally {
            headers.release()
        }
    }

    @Test
    fun parseHeadersNoLeadingSpaceWithTrailingSpaces(): Unit = testSuspend {
        val encodedHeaders = """
            name:value
        """.trimIndent() + "    \r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)
        val headers = parseHeaders(channel)

        try {
            assertEquals(1, headers.size)
            assertEquals("value", headers["name"].toString())
        } finally {
            headers.release()
        }
    }

    @Test
    fun parseHeadersSpaceAfterHeaderNameShouldBeProhibited(): Unit = testSuspend {
        val encodedHeaders = """
            name :value
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)

        assertFailsWith<ParserException> {
            parseHeaders(channel).release()
        }
    }

    @Test
    fun parseHeadersSpacesInHeaderNameShouldBeProhibited(): Unit = testSuspend {
        val encodedHeaders = """
            name and more: value
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)

        assertFailsWith<ParserException> {
            parseHeaders(channel).release()
        }
    }

    @Test
    fun parseHeadersSpacesInHeaderNameShouldBeProhibitedFixed(): Unit = testSuspend {
        val encodedHeaders = """
            name-and-more: value
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)

        parseHeaders(channel).release()
    }

    @Test
    fun parseHeadersDelimitersInHeaderNameShouldBeProhibited(): Unit = testSuspend {
        val encodedHeaders = """
            name,: value
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)

        assertFailsWith<ParserException> {
            parseHeaders(channel).release()
        }
    }

    @Test
    fun parseHeadersEmptyHeaderNameShouldBeProhibited(): Unit = testSuspend {
        val encodedHeaders = """
            : value
        """.trimIndent() + "\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)

        assertFailsWith<ParserException> {
            parseHeaders(channel).release()
        }.let {
            assertTrue("Empty header names are not allowed" in it.message.orEmpty())
        }
    }

    @Test
    fun parseHeadersFoldingShouldBeProhibited(): Unit = testSuspend {
        val encodedHeaders = "A:\r\n folding\r\n\r\n"
        val channel = ByteReadChannel(encodedHeaders)

        assertFailsWith<ParserException> {
            parseHeaders(channel).release()
        }
    }
}

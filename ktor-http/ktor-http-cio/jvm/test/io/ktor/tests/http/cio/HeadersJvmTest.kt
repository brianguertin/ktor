/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.http.cio

import io.ktor.http.cio.*
import io.ktor.http.cio.internals.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.test.*

class HeadersJvmTest {
    private val builder = CharArrayBuilder()

    @AfterTest
    fun tearDown() {
        builder.release()
    }

    @Test
    fun smokeTest() = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host: localhost\r\n\r\n")
        }

        val hh = parseHeaders(ch, builder)!!

        assertEquals("localhost", hh["Host"]?.toString())
        assertEquals("localhost", hh["host"]?.toString())
        assertEquals("localhost", hh["hOst"]?.toString())
        assertEquals("localhost", hh["HOST"]?.toString())
        assertNull(hh["Host "])

        hh.release()
    }

    @Test
    fun smokeTestUnicode() = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host: unicode-\u0422\r\n\r\n")
        }
        val hh = parseHeaders(ch, builder)!!

        assertEquals("unicode-\u0422", hh["Host"]?.toString())

        hh.release()
    }

    @Test
    fun extraSpacesLeading(): Unit = runBlocking {
        val ch = ByteReadChannel {
            writeString(" Host:  localhost\r\n\r\n")
        }
        assertFailsWith<ParserException> {
            parseHeaders(ch, builder)!!.release()
        }
    }

    @Test
    fun extraSpacesMiddle(): Unit = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host:  localhost\r\n\r\n")
        }
        val hh = parseHeaders(ch, builder)!!

        assertEquals("localhost", hh["Host"]?.toString())
        hh.release()
    }

    @Test
    fun extraSpacesMiddleBeforeColon(): Unit = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host : localhost\r\n\r\n")
        }
        assertFailsWith<ParserException> {
            parseHeaders(ch, builder)!!.release()
        }
    }

    @Test
    fun extraSpacesMiddleBeforeColonNoAfter(): Unit = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host :localhost\r\n\r\n")
        }
        assertFailsWith<ParserException> {
            parseHeaders(ch, builder)!!.release()
        }
    }

    @Test
    fun extraSpacesTrailing() = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host:  localhost \r\n\r\n")
        }
        val hh = parseHeaders(ch, builder)!!

        assertEquals("localhost", hh["Host"]?.toString())
        hh.release()
    }

    @Test
    fun alternativeLineSeparatorsFirst() = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host: localhost\n\r\n")
        }
        val hh = parseHeaders(ch, builder)!!

        assertEquals("localhost", hh["Host"]?.toString())
        hh.release()
    }

    @Test
    fun alternativeLineSeparatorsSecond() = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host: localhost\n\n\n")
        }
        val hh = parseHeaders(ch, builder)!!

        assertEquals("localhost", hh["Host"]?.toString())
        hh.release()
    }

    @Test
    fun alternativeLineSeparatorsBoth() = runBlocking {
        val ch = ByteReadChannel {
            writeString("Host: localhost\n\n")
        }
        val hh = parseHeaders(ch, builder)!!

        assertEquals("localhost", hh["Host"]?.toString())
        hh.release()
    }

    @Test
    fun testExpectHttpBodyGet() = runBlocking {
        val ch = ByteReadChannel {
            writeString("GET / HTTP/1.1\nConnection: close\n\n")
        }
        val request = parseRequest(ch)!!

        try {
            assertFalse { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyGetAndContentLength() = runBlocking {
        val ch = ByteReadChannel {
            writeString("GET / HTTP/1.1\nContent-Length: 0\n\n")
        }
        val request = parseRequest(ch)!!

        try {
            assertFalse { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyGetAndContentLengthNonZero() = runBlocking {
        val ch = ByteReadChannel {
            writeString("GET / HTTP/1.1\nContent-Length: 10\n\n")
        }
        val request = parseRequest(ch)!!

        try {
            assertTrue { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyPostContentLengthZero() = runBlocking {
        val ch = ByteReadChannel {
            writeString("POST / HTTP/1.1\nContent-Length: 0\n\n")
        }
        val request = parseRequest(ch)!!

        try {
            assertFalse { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyPostContentLengthNonZero() = runBlocking {
        val ch = ByteReadChannel {
            writeString("POST / HTTP/1.1\nContent-Length: 10\n\n")
        }
        val request = parseRequest(ch)!!

        try {
            assertTrue { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyPostContentChunked() = runBlocking {
        val ch = ByteReadChannel("POST / HTTP/1.1\nTransfer-Encoding: chunked\n\n")
        val request = parseRequest(ch)!!

        try {
            assertTrue { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyPostContentType() = runBlocking {
        val ch = ByteReadChannel("POST / HTTP/1.1\nContent-Type: application/json\n\n")
        val request = parseRequest(ch)!!

        try {
            assertTrue { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testExpectHttpBodyPostOnly() = runBlocking {
        val ch = ByteReadChannel("POST / HTTP/1.1\nX: 0\n\n")
        val request = parseRequest(ch)!!

        try {
            assertFalse { expectHttpBody(request) }
        } finally {
            request.release()
        }
    }

    @Test
    fun testEmptyHeaderValue() = runBlocking {
        val ch = ByteReadChannel("Host:\r\n\r\n")
        val headers = parseHeaders(ch, builder)!!
        assertEquals("", headers["Host"]?.toString())

        headers.release()
    }

    @Test
    fun testNoColon(): Unit = runBlocking {
        val ch = ByteReadChannel("Host\r\n\r\n")

        assertFails {
            runBlocking {
                parseHeaders(ch, builder)
            }
        }
    }

    @Test
    fun testBlankHeaderValue() = runBlocking {
        val ch = ByteReadChannel("Host: \r\n\r\n")
        val headers = parseHeaders(ch, builder)!!
        assertEquals("", headers["Host"]?.toString())

        headers.release()
    }

    @Test
    fun testWrongHeader() = runBlocking<Unit> {
        val ch = ByteReadChannel("Hello world\r\n\r\n")

        assertFails {
            runBlocking {
                parseHeaders(ch, builder)
            }
        }
    }

    @Test
    fun `Host header with invalid character (slash)`() = runBlocking<Unit> {
        val ch = ByteReadChannel("Host: www/exam/ple.com\n\n")

        assertFailsWith<IllegalStateException> {
            parseHeaders(ch, builder)
        }
    }

    @Test
    fun `Host header with invalid character (question mark)`() = runBlocking<Unit> {
        val ch = ByteReadChannel("Host: www.example?com\n\n")

        assertFailsWith<IllegalStateException> {
            parseHeaders(ch, builder)
        }
    }

    @Test
    fun `Host header with invalid '#' character`() = runBlocking<Unit> {
        val ch = ByteReadChannel("Host: www.ex#mple.com\n\n")

        assertFailsWith<IllegalStateException> {
            parseHeaders(ch, builder)
        }
    }

    @Test
    fun `Host header with invalid '@' character`() = runBlocking<Unit> {
        val ch = ByteReadChannel("Host: www.ex@mple.com\n\n")

        assertFailsWith<IllegalStateException> {
            parseHeaders(ch, builder)
        }
    }
}

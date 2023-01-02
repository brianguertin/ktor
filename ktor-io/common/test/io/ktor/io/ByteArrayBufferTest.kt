/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlin.test.*

class ByteArrayBufferTest {

    @Test
    fun testEmptyToByteArray() {
        val buffer = ByteArrayBuffer(ByteArray(0))
        assertTrue {
            ByteArray(0).contentEquals(buffer.toByteArray())
        }
    }

    @Test
    fun testReadBuffer() {
        val buffer = ByteArrayBuffer("Hello".toByteArray())

        for (char in "Hello") {
            assertEquals(char.code.toByte(), buffer.readBuffer(1).readByte())
        }
    }

    @Test
    fun testReadStringFromEmpty() {
        val buffer = ByteArrayBuffer(ByteArray(0))
        assertFailsWith<IndexOutOfBoundsException> {
            buffer.readString()
        }
    }

    @Test
    fun testReadByteArrayFromEmpty() {
        val buffer = ByteArrayBuffer(ByteArray(0))
        assertFailsWith<IllegalArgumentException> {
            buffer.readByteArray(1)
        }

        assertEquals(0, buffer.readByteArray(0).size)
    }

    @Test
    fun testIllegalIndexes() {
        val buffer = ByteArrayBuffer(ByteArray(0))

        assertFailsWith<IllegalArgumentException> {
            buffer.readIndex = -1
        }
        assertFailsWith<IllegalArgumentException> {
            buffer.readIndex = 1
        }
        assertFailsWith<IllegalArgumentException> {
            buffer.writeIndex = -1
        }
        assertFailsWith<IllegalArgumentException> {
            buffer.writeIndex = 1
        }
    }
}

/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlin.test.*

class BufferOperationsTest {

    @Test
    fun testIndexOfPrefix() {
        val buffer = ByteArrayBuffer(byteArrayOf(1, 2, 3, 4, 5))

        assertEquals(0, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(1, 2, 3))))
        assertEquals(0, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(1))))
        assertEquals(0, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf())))

        assertEquals(1, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(2, 3))))
        assertEquals(1, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(2))))

        assertEquals(4, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(5))))
    }

    @Test
    fun testIndexOfPrefixNotFound() {
        val buffer = ByteArrayBuffer(byteArrayOf(1, 2, 3, 4, 5))

        assertEquals(-1, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(2, 3, 5))))
        assertEquals(-1, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(2, 3, 1))))
        assertEquals(-1, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(3, 1))))
        assertEquals(-1, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(6))))
    }

    @Test
    fun testIndexOfPrefixInTail() {
        val buffer = ByteArrayBuffer(byteArrayOf(1, 2, 3, 4, 5))

        assertEquals(0, buffer.indexOfPrefix(ByteArrayBuffer(byteArrayOf(1, 2, 3, 4, 5, 6))))
        assertEquals(-1, ByteArrayBuffer(ByteArray(0)).indexOfPrefix(ByteArrayBuffer(ByteArray(0))))
    }

}

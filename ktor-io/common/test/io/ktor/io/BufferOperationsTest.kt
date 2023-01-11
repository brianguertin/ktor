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

    @Test
    fun testCommonPrefixLengthForEmpty() {
        val empty = ByteArray(0)
        val single = ByteArrayBuffer(ByteArray(1) { 42 })
        assertEquals(0, ByteArrayBuffer(empty).commonPrefixLength(ByteArrayBuffer(empty)))

        assertEquals(0, single.commonPrefixLength(ByteArrayBuffer(empty)))
        assertEquals(0, ByteArrayBuffer(empty).commonPrefixLength(single))
    }

    @Test
    fun testCommonPrefixLengthZero() {
        assertEquals(0, ByteArrayBuffer(byteArrayOf(1, 2, 3)).commonPrefixLength(ByteArrayBuffer(byteArrayOf(4, 5, 6))))
    }

    @Test
    fun testCommonPrefixLength() {
        val single = ByteArrayBuffer(ByteArray(1) { 42 })
        assertEquals(1, single.commonPrefixLength(single))

        val first = ByteArrayBuffer(byteArrayOf(1, 2, 3, 4, 5))
        val second = ByteArrayBuffer(byteArrayOf(1, 2, 3, 4))

        assertEquals(4, first.commonPrefixLength(second))
        assertEquals(4, second.commonPrefixLength(first))
    }
}

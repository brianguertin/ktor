/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.*
import io.ktor.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.junit4.*
import org.junit.*
import org.junit.Ignore
import java.io.*
import kotlin.io.use
import kotlin.test.*
import kotlin.test.Test

class FileChannelTest {
    private val sandbox = File("build/files")
    private lateinit var temp: File

    @get:Rule
    val timeout = CoroutinesTimeout.seconds(10)

    @Before
    fun setUp() {
        if (!sandbox.mkdirs() && !sandbox.isDirectory) {
            fail()
        }

        temp = File.createTempFile("file", "", sandbox)
    }

    @Test
    fun testReadEmptyFile() = runBlocking {
        val channel = temp.readChannel()
        assertFalse(channel.awaitBytes())
    }

    @Test
    fun testEmptyFileWithInputStream() {
        assertEquals(0, temp.readChannel().toInputStream().use { it.readBytes().size })
    }

    @Test
    fun testSingleByteFile() {
        temp.writeBytes(byteArrayOf(7))

        val stream = temp.readChannel().toInputStream()
        assertEquals(listOf(7.toByte()), stream.use { it.readBytes().toList() })
    }

    @Test
    fun testSingleByteDrop1Take1() {
        temp.writeBytes(byteArrayOf(7, 8, 9))

        assertEquals(
            listOf(8.toByte()),
            temp.readChannel(start = 1L, endInclusive = 1L)
                .toInputStream()
                .use { it.readBytes().toList() }
        )
    }

    @Test
    fun test3Bytes() = runBlocking {
        temp.writeBytes(byteArrayOf(7, 42, 84))

        val result = temp.readChannel().toByteArray()
        assertArrayEquals(byteArrayOf(7, 42, 84), result)
    }

    @Test
    fun test3BytesWithStream() {
        temp.writeBytes(byteArrayOf(7, 8, 9))

        assertEquals(byteArrayOf(7, 8, 9).toList(), temp.readChannel().toInputStream().use { it.readBytes().toList() })
    }

    @Test
    fun `readChannel should not lock file pre read`() {
        // Arrange
        temp

        // Act
        @Suppress("UNUSED_VARIABLE")
        val unused = temp.readChannel()

        // Assert (we cannot delete if there is a file handle open on it)
        assertFalse(temp.delete())
    }

    @Test
    @Ignore("Does not work on team city CI for some reason")
    fun `readChannel is open during read`() {
        // Arrange
        val magicNumberBiggerThanSomeInternalBuffer = 10000
        temp.writeBytes(ByteArray(magicNumberBiggerThanSomeInternalBuffer))
        val readChannel = temp.readChannel()

        runBlocking {
            // Act - place us in the middle of reading a file
            readChannel.readByte()

            // Assert (we cannot delete if there is a file handle open on it)
            assertFalse(temp.delete())

            // And just making sure we can complete it normally.
            readChannel.readRemaining()
            assertTrue(temp.delete())
        }
    }

    @Test
    fun `readChannel should close file post read`() {
        // Arrange
        temp

        // Act
        temp.readChannel().toInputStream().readBytes()

        // Assert (we cannot delete if there is a file handle open on it)
        assertTrue(temp.delete())
    }

    @Test
    fun testReadWithoutTail() = runBlocking {
        temp.writeBytes(byteArrayOf(7, 8, 9))

        val channel = temp.readChannel(endInclusive = 1L)
        assertArrayEquals(byteArrayOf(7, 8), channel.toByteArray())
    }

    @Test
    fun testReadWithoutHead() = runBlocking {
        temp.writeBytes(byteArrayOf(7, 8, 9))
        val channel = temp.readChannel(start = 1L)
        assertArrayEquals(byteArrayOf(8, 9), channel.toByteArray())
    }

    @Test
    fun testReadWithoutHeadAndTail() = runBlocking {
        temp.writeBytes(byteArrayOf(7, 8, 9))
        val channel = temp.readChannel(start = 1L, endInclusive = 1L)
        assertArrayEquals(byteArrayOf(8), channel.toByteArray())
    }
}

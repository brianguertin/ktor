/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.utils

import io.ktor.test.dispatcher.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.coroutines.debug.junit4.*
import org.junit.*
import java.io.*
import java.nio.*
import java.util.zip.*
import kotlin.random.*
import kotlin.test.*
import kotlin.test.Test

class DeflaterReadChannelTest : CoroutineScope {
    private val testJob = Job()
    override val coroutineContext get() = testJob + Dispatchers.Unconfined

//    @get:Rule
//    val timeout = CoroutinesTimeout.seconds(60)

    @AfterTest
    fun after() {
        testJob.cancel()
    }

    @Test
    fun testWithRealFile() {
        val file = listOf(
            File("jvm/test/io/ktor/tests/utils/DeflaterReadChannelTest.kt"),
            File("ktor-server/ktor-server-tests/jvm/test/io/ktor/tests/utils/DeflaterReadChannelTest.kt")
        ).first(File::exists)

        testReadChannel(file.readText(), file.readChannel())
        testWriteChannel(file.readText(), file.readChannel())
    }

    @Test
    fun testFileChannel() {
        val file = listOf(
            File("jvm/test/io/ktor/tests/utils/DeflaterReadChannelTest.kt"),
            File("ktor-server/ktor-server-tests/jvm/test/io/ktor/tests/utils/DeflaterReadChannelTest.kt")
        ).first(File::exists)

        val content = file.readText()

        fun read(from: Long, to: Long) = file.readChannel(from, to).toInputStream().reader().readText()

        assertEquals(content.take(3), read(0, 2))
        assertEquals(content.drop(1).take(2), read(1, 2))
        assertEquals(content.takeLast(3), read(file.length() - 3, file.length() - 1))
    }

    @Test
    fun testDeflateChar() {
//        testReadChannel("1", ByteReadChannel { writeChar('1') })
        testWriteChannel("1", ByteReadChannel { writeChar('1') })
    }

    @Test
    fun testDeflateShort() {
        testReadChannel("12", ByteReadChannel { writeString("12")})
        testWriteChannel("12", ByteReadChannel { writeString("12")})
    }

    @Test
    fun testDeflateCharToArray() = runBlocking {
        val data = ByteReadChannel {
            writeString("1")
        }.deflated().toByteArray()

        val expected = ByteArrayInputStream(data).ungzip().reader().readText()
        assertEquals("1", expected)
    }

    @Test
    fun testInputStreamHasSameAsByteArray() = runBlocking {
        val fromArray = ByteReadChannel {
            writeString("1")
        }.deflated().toByteArray()

        val fromStream: ByteArray = ByteReadChannel {
            writeString("1")
        }.deflated().toInputStream().readBytes()

        assertEquals(fromArray.toList(), fromStream.toList())
    }


    @Test
    fun testDeflateCharToInputStream() = runBlocking {
        val data: ByteArray = ByteReadChannel {
            writeString("1")
        }.deflated().toInputStream().readBytes()

        val expected = ByteArrayInputStream(data).ungzip().reader().readText()
        assertEquals("1", expected)
    }

    @Test
    fun testSmallPieces() {
        val text = "The quick brown fox jumps over the lazy dog"
        assertEquals(text, asyncOf(text).toInputStream().reader().readText())

        for (step in 1..text.length) {
            val chunk = text.substring(0, step)
            println(chunk)
            testReadChannel(chunk, asyncOf(chunk))
            testWriteChannel(chunk, asyncOf(chunk))
        }
    }

    @Test
    fun testBiggerThan8k() {
        val text = buildString {
            while (length < 65536) {
                append("The quick brown fox jumps over the lazy dog")
            }
        }
        val bb = ByteBuffer.wrap(text.toByteArray(Charsets.ISO_8859_1))

        for (
        step in generateSequence(1) { it * 2 }
            .dropWhile { it < 64 }
            .takeWhile { it <= 8192 }
            .flatMap { sequenceOf(it, it - 1, it + 1) }
        ) {
            bb.clear()
            testReadChannel(text, asyncOf(bb))

            bb.clear()
            testWriteChannel(text, asyncOf(bb))
        }
    }

    @Test
    fun testLargeContent() {
        val text = buildString {
            for (i in 1..16384) {
                append("test$i\n".padStart(10, ' '))
            }
        }

        testReadChannel(text, asyncOf(text))
        testWriteChannel(text, asyncOf(text))
    }

    @Test
    fun testGzippedBiggerThan8k() {
        val text = buildString {
            for (i in 1..65536) {
                append(' ' + Random.nextInt(32, 126) % 32)
            }
        }

        testReadChannel(text, asyncOf(text))
        testWriteChannel(text, asyncOf(text))
    }

    @Test
    fun testFaultyGzippedBiggerThan8k() {
        val text = buildString {
            for (i in 1..65536) {
                append(' ' + Random.nextInt(32, 126) % 32)
            }
        }

        testFaultyWriteChannel(asyncOf(text))
    }

    private fun asyncOf(text: String): ByteReadChannel = asyncOf(ByteBuffer.wrap(text.toByteArray(Charsets.ISO_8859_1)))
    private fun asyncOf(bb: ByteBuffer): ByteReadChannel = ByteReadChannel(bb)

    private fun OutputStream.gzip() = GZIPOutputStream(this)
    private fun InputStream.ungzip() = GZIPInputStream(this)

    private fun testReadChannel(expected: String, src: ByteReadChannel) {
        assertEquals(expected, src.deflated().toInputStream().ungzip().reader().readText())
    }

    private fun testWriteChannel(expected: String, src: ByteReadChannel) {
        val channel = ByteReadChannel {
            val dst = deflated()
            src.copyAndClose(dst)
            dst.flush()
            dst.close()
        }

        val result = channel.toInputStream().ungzip().reader().readText()
        assertEquals(expected, result)
    }

    private fun testFaultyWriteChannel(src: ByteReadChannel) = runBlocking {

        withContext(Dispatchers.IO) {
            val channel = ByteReadChannel {
                val destination = deflated()
                src.copyAndClose(destination)
                destination.close(IOException("Broken pipe"))
            }

            assertFailsWith(IOException::class) { throw channel.closedCause!! }
        }
    }
}

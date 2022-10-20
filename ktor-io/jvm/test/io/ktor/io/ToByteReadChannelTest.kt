package io.ktor.io

import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import kotlin.test.*

class ToByteReadChannelTest {
    @Test
    fun testEmpty() = runBlocking {
        val channel = ByteArrayInputStream(ByteArray(0)).toByteReadChannel()
        channel.readRemaining().use { pkt ->
            assertTrue { pkt.isEmpty }
        }
    }

    @Test
    fun testSeveralBytes() = runBlocking {
        val content = byteArrayOf(1, 2, 3, 4)
        val channel = ByteArrayInputStream(content).toByteReadChannel()
        channel.readRemaining().use { pkt ->
            val bytes = pkt.toByteArray()
            assertTrue { bytes.contentEquals(content) }
        }
    }

    @Test
    fun testBigStream() = runBlocking {
        val content = ByteArray(65536 * 8)
        Random().nextBytes(content)

        val channel = ByteArrayInputStream(content).toByteReadChannel()
        channel.readRemaining().use { pkt ->
            val bytes = pkt.toByteArray()
            assertTrue { bytes.contentEquals(content) }
        }
    }

    @Test
    fun testEmptyBB() = runBlocking {
        val channel = ByteArrayInputStream(ByteArray(0)).toByteReadChannel()
        channel.readRemaining().use { pkt ->
            assertTrue { pkt.isEmpty }
        }
    }

    @Test
    fun testSeveralBytesBB() = runBlocking {
        val content = byteArrayOf(1, 2, 3, 4)
        val channel = ByteArrayInputStream(content).toByteReadChannel()
        channel.readRemaining().use { pkt ->
            val bytes = pkt.toByteArray()
            assertTrue { bytes.contentEquals(content) }
        }
    }

    @Test
    fun testBigStreamBB() = runBlocking {
        val content = ByteArray(65536 * 8)
        Random().nextBytes(content)

        val channel = ByteArrayInputStream(content).toByteReadChannel()
        channel.readRemaining().use { pkt ->
            val bytes = pkt.toByteArray()
            assertTrue { bytes.contentEquals(content) }
        }
    }
}

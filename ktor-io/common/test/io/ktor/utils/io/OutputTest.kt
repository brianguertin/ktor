package io.ktor.utils.io

import io.ktor.utils.io.bits.DROP_Memory
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlin.test.*

class OutputTest {
    @Test
    fun smokeTest() {
        val builder = DROP_BytePacketBuilder()

        val output = object : DROP_Output() {
            override fun closeDestination() {
            }

            override fun flush(source: DROP_Memory, offset: Int, length: Int) {
                builder.writeFully(source, offset, length)
            }
        }

        output.use {
            it.append("test")
        }

        val pkt = builder.build().readText()
        assertEquals("test", pkt)
    }

    @Test
    fun testCopy() {
        val result = DROP_BytePacketBuilder()

        val output = object : DROP_Output() {
            override fun closeDestination() {
            }

            override fun flush(source: DROP_Memory, offset: Int, length: Int) {
                result.writeFully(source, offset, length)
            }
        }

        val fromHead = DROP_ChunkBuffer.Pool.borrow()
        var current = fromHead
        repeat(3) {
            current.appendChars("test $it. ")
            val next = DROP_ChunkBuffer.Pool.borrow()
            current.next = next
            current = next
        }

        current.appendChars("end.")

        val from = DROP_ByteReadPacket(fromHead, DROP_ChunkBuffer.Pool)

        from.copyTo(output)
        output.flush()

        assertEquals("test 0. test 1. test 2. end.", result.build().readText())
    }
}

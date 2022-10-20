/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.test.*

class ByteChannelNativeConcurrentTest {
    private val TEST_SIZE = 10 * 1024

    @Test
    fun testReadWriteByte() {
        val channel = ByteReadChannel {
            repeat(TEST_SIZE) {
                writeByte(it.toByte())
            }
        }

        runBlocking {
            repeat(TEST_SIZE) {
                assertEquals(it.toByte(), channel.readByte())
            }
        }
    }

    @Test
    fun testReadWriteBlock() {
        val BLOCK_SIZE = 1024
        val block = createBlock(BLOCK_SIZE)

        val channel = ByteReadChannel {
            repeat(TEST_SIZE) {
                writeByteArray(block)
            }
        }

        runBlocking {
            repeat(TEST_SIZE) {
                val result = channel.readRemaining(BLOCK_SIZE.toLong())

                assertTrue {
                    block.contentEquals(result.toByteArray())
                }
            }
        }
    }

    private fun createBlock(size: Int): ByteArray = ByteArray(size) { it.toByte() }
}

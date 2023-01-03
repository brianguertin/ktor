/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import kotlin.test.*

class PacketTest {

    @Test
    fun testReadByteArrayFromChunked() {
        val packet = buildPacket {
            writeByteArray(byteArrayOf(1))
            writeByteArray(byteArrayOf(2))
            writeByteArray(byteArrayOf(3))
            writeByteArray(byteArrayOf(1, 2))
        }

        val result = packet.readByteArray(4)
        assertArrayEquals(byteArrayOf(1, 2, 3, 1), result)
        assertEquals(1, packet.availableForRead)

        val result2 = packet.readByteArray(1)
        assertArrayEquals(byteArrayOf(2), result2)

        assertEquals(0, packet.availableForRead)
    }
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.*
import kotlinx.coroutines.channels.*

public class ByteArrayWriteChannel : ByteWriteChannel {
    private val channel = Channel<ByteArray>()

    override val isClosedForWrite: Boolean
        get() = TODO("Not yet implemented")

    override val closedCause: Throwable?
        get() = TODO("Not yet implemented")

    override val writablePacket: Packet = Packet()

    override fun close(cause: Throwable?): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun flush() {
        writablePacket.toByteArray()
    }

    public suspend fun toByteArray(): ByteArray = channel.receive()
}

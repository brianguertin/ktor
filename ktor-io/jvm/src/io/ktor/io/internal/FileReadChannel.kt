/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.*
import java.nio.channels.*

internal class FileReadChannel(
    file: File,
    startIndex: Long,
    endIndex: Long
) : ByteReadChannel {
    val source: RandomAccessFile = RandomAccessFile(file, "r")
    val channel: FileChannel = source.channel

    private var remaining: Long
    private var closed = false

    init {
        remaining = if (endIndex > 0L) {
            endIndex - startIndex
        } else {
            Long.MAX_VALUE
        }

        channel.position(startIndex)
    }

    override var closedCause: Throwable? = null
        private set

    override val readablePacket: Packet = Packet()

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        closedCause?.let { throw it }
        if (closed) return readablePacket.isNotEmpty

        while (!closed && !predicate()) {
            fill()
        }

        return !closed || readablePacket.isNotEmpty
    }

    private suspend fun fill() = withContext(Dispatchers.IO) {
        if (remaining <= 0) return@withContext

        val buffer = ByteBuffer.allocate(16 * 1024)
        val count = channel.read(buffer)

        if (count < 0) {
            remaining = 0
            closed = true
            channel.close()
            source.close()
            return@withContext
        }

        buffer.flip()
        if (count > remaining) {
            buffer.limit(buffer.position() + remaining.toInt())
        }

        remaining -= buffer.remaining()
        readablePacket.writeBuffer(ByteBufferBuffer(buffer))

        if (remaining == 0L) {
            closed = true
            channel.close()
            source.close()
        }
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (closed) return false
        closed = true
        remaining = 0
        closedCause = cause

        channel.close()
        source.close()

        return true
    }
}

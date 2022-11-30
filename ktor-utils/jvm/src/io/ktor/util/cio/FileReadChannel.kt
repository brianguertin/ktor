/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.cio

import io.ktor.io.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.*
import java.nio.channels.*
import kotlin.coroutines.*

internal class FileReadChannel(
    file: File,
    startIndex: Long,
    endIndex: Long
) : ByteReadChannel {
    val source: RandomAccessFile = RandomAccessFile(file, "r")
    val channel: FileChannel = source.channel
    private var remaining: Long

    init {
        remaining = if (endIndex > 0L) {
            endIndex - startIndex
        } else {
            Long.MAX_VALUE
        }

        channel.position(startIndex)
    }

    override val isClosedForRead: Boolean get() = readablePacket.isEmpty && remaining == 0L
    override var closedCause: Throwable? = null
        private set

    override val readablePacket: Packet = Packet()

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean = withContext(Dispatchers.IO) {
        while (remaining > 0) {
            val predicateResult = predicate()
            if (predicateResult) return@withContext predicateResult

            val buffer = ByteBuffer.allocate(16 * 1024)
            val count = channel.read(buffer)

            if (count < 0) {
                remaining = 0
                return@withContext predicateResult
            }

            buffer.flip()
            if (count > remaining) {
                buffer.limit(buffer.position() + remaining.toInt())
            }

            remaining -= buffer.remaining()
            readablePacket.writeBuffer(ByteBufferBuffer(buffer))
        }

        return@withContext true
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (isClosedForRead) return false

        remaining = 0
        closedCause = cause

        channel.close()
        source.close()

        return true
    }

}

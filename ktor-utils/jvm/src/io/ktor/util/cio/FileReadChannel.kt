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
    private var remaining = endIndex - startIndex

    init {
        channel.position(startIndex)
    }

    override var isClosedForRead: Boolean = false
        private set
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
                isClosedForRead = true
                return@withContext predicateResult
            }

            if (count > remaining) {
                val newLimit = buffer.limit() - (count - remaining)
                buffer.limit(newLimit.toInt())
            } else {
                remaining -= count
            }

            buffer.flip()
            if (buffer.hasRemaining()) {
                readablePacket.writeBuffer(ByteBufferBuffer(buffer))
            }
        }

        return@withContext true
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (isClosedForRead) return false

        isClosedForRead = true
        closedCause = cause

        channel.close()
        source.close()

        return true
    }

}

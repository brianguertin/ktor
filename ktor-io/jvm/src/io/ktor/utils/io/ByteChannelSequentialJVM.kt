package io.ktor.utils.io

import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.nio.*

@Suppress("DEPRECATION", "OverridingDeprecatedMember")
public class ByteChannelSequentialJVM(
    initial: ChunkBuffer,
    autoFlush: Boolean
) : ByteChannelSequentialBase(initial, autoFlush) {

    @Volatile
    private var attachedJob: Job? = null

    @OptIn(InternalCoroutinesApi::class)
    override fun attachJob(job: Job) {
        attachedJob?.cancel()
        attachedJob = job
        job.invokeOnCompletion(onCancelling = true) { cause ->
            attachedJob = null
            if (cause != null) {
                cancel(cause.unwrapCancellationException())
            }
        }
    }

    private suspend fun writeAvailableSuspend(src: ByteBuffer): Int {
        awaitAtLeastNBytesAvailableForWrite(1)
        return writeAvailable(src)
    }

    private suspend fun writeFullySuspend(src: ByteBuffer) {
        while (src.hasRemaining()) {
            awaitAtLeastNBytesAvailableForWrite(1)
            val count = tryWriteAvailable(src)
            afterWrite(count)
        }
    }

    private fun tryWriteAvailable(src: ByteBuffer): Int {
        val srcRemaining = src.remaining()
        val availableForWrite = availableForWrite

        val count = when {
            closed -> throw closedCause ?: ClosedSendChannelException("Channel closed for write")
            srcRemaining == 0 -> 0
            srcRemaining <= availableForWrite -> {
                writable.writeFully(src)
                srcRemaining
            }
            availableForWrite == 0 -> 0
            else -> {
                val oldLimit = src.limit()
                src.limit(src.position() + availableForWrite)
                writable.writeFully(src)
                src.limit(oldLimit)
                availableForWrite
            }
        }

        afterWrite(count)
        return count
    }

    private suspend fun readAvailableSuspend(dst: ByteBuffer): Int {
        if (!await(1)) return -1
        return readAvailable(dst)
    }

    private suspend fun readFullySuspend(dst: ByteBuffer, rc0: Int): Int {
        var count = rc0

        while (dst.hasRemaining()) {
            if (!await(1)) throw EOFException("Channel closed")
            val rc = tryReadAvailable(dst)
            if (rc == -1) throw EOFException("Channel closed")
            count += rc
        }

        return count
    }

    private fun tryReadAvailable(dst: ByteBuffer): Int {
        closedCause?.let { throw it }

        if (closed && availableForRead == 0) {
            return -1
        }

        if (!readable.canRead()) {
            prepareFlushedBytes()
        }

        val count = readable.readAvailable(dst)
        afterRead(count)
        return count
    }

    private class Session(private val channel: ByteChannelSequentialJVM) : LookAheadSuspendSession {
        override suspend fun awaitAtLeast(n: Int): Boolean {
            channel.closedCause?.let { throw it }

            return channel.await(n)
        }

        override fun consumed(n: Int) {
            channel.closedCause?.let { throw it }

            channel.discard(n)
        }

        override fun request(skip: Int, atLeast: Int): ByteBuffer? {
            channel.closedCause?.let { throw it }

            if (channel.isClosedForRead) return null

            if (channel.readable.isEmpty) {
                channel.prepareFlushedBytes()
            }

            val head = channel.readable.head
            if (head.readRemaining < skip + atLeast) return null

            val buffer = head.memory.buffer.slice()
            buffer.position(head.readPosition + skip)
            buffer.limit(head.writePosition)
            return buffer
        }
    }

    /**
     * Suspend until the channel has bytes to read or gets closed. Throws exception if the channel was closed with an error.
     */
    override suspend fun awaitContent() {
        await(1)
    }
}

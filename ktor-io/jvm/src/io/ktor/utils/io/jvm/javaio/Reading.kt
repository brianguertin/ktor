package io.ktor.utils.io.jvm.javaio

import io.ktor.io.*
import io.ktor.utils.io.*
import io.ktor.utils.io.pool.*
import kotlinx.coroutines.*
import java.io.*

/**
 * Copies up to [limit] bytes from [this] input stream to CIO byte [channel] blocking on reading [this] stream
 * and suspending on [channel] if required
 *
 * @return number of bytes copied
 */
public suspend fun InputStream.copyTo(channel: ByteWriteChannel, limit: Long = Long.MAX_VALUE): Long {
    return toByteReadChannel().copyTo(channel, limit)
}

/**
 * Open a channel and launch a coroutine to copy bytes from the input stream to the channel.
 * Please note that it may block your async code when started on [Dispatchers.Unconfined]
 * since [InputStream] is blocking on it's nature
 */
public fun InputStream.toByteReadChannel(
    pool: ObjectPool<ByteArray> = ByteArrayPool
): ByteReadChannel = object : ByteReadChannel {
    private var closed = false
    override val isClosedForRead: Boolean = false
    override var closedCause: Throwable? = null
        private set
    override val readablePacket: Packet = Packet()

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean = withContext(Dispatchers.IO) {
        while (!predicate()) {
            val buffer = pool.borrow()
            try {
                val count = read(buffer)
                if (count == -1) {
                    closed = true
                    return@withContext false
                }

                readablePacket.writeByteArray(buffer)
            } catch (cause: Throwable) {
                pool.recycle(buffer)
                closed = true
                closedCause = cause
                throw cause
            }
        }

        return@withContext true
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (closed) return false
        closed = true
        closedCause = cause
        return true
    }
}

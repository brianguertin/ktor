package io.ktor.io.jvm.javaio

import io.ktor.io.*
import kotlinx.coroutines.*
import java.io.*

/**
 * Copies up to [limit] bytes from [this] byte channel to [out] stream suspending on read channel
 * and blocking on output
 *
 * @return number of bytes copied
 */
public suspend fun ByteReadChannel.copyTo(out: OutputStream, limit: Long = Long.MAX_VALUE): Long {
    require(limit >= 0) { "Limit shouldn't be negative: $limit" }

    return withContext(Dispatchers.IO) {
        var copied = 0L

        while (awaitBytes() && copied < limit) {
            val count = minOf(limit - copied, readablePacket.availableForRead.toLong())
            val array = readByteArray(count.toInt())
            out.write(array)
            copied += array.size
        }

        copied
    }
}

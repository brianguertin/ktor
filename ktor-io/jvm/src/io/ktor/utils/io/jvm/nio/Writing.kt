package io.ktor.utils.io.jvm.nio

import io.ktor.io.*
import java.nio.channels.*

/**
 * Copy up to [limit] bytes to blocking NIO [channel].
 * Copying to a non-blocking channel requires selection and not supported.
 * It is suspended if no data are available in a byte channel but may block if destination NIO channel blocks.
 *
 * @return number of bytes copied
 */
public suspend fun ByteReadChannel.copyTo(channel: WritableByteChannel, limit: Long = Long.MAX_VALUE): Long {
    require(limit >= 0L) { "Limit shouldn't be negative: $limit" }
    if (channel is SelectableChannel && !channel.isBlocking) {
        throw IllegalArgumentException("Non-blocking channels are not supported")
    }

    var copied = 0L
    while (copied < limit) {
        if (availableForRead == 0) awaitBytes()
        val size = minOf(limit - copied, availableForRead.toLong()).toInt()
        val packet = readPacket(size)
        while (packet.isNotEmpty) {
            copied += channel.write(packet.readByteBuffer())
        }
    }

    return copied
}

/**
 * Copy up to [limit] bytes to blocking [pipe]. A shortcut to copyTo function with NIO channel destination
 *
 * @return number of bytes were copied
 */
public suspend fun ByteReadChannel.copyTo(pipe: Pipe, limit: Long = Long.MAX_VALUE): Long = copyTo(pipe.sink(), limit)

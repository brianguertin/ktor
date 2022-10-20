package io.ktor.utils.io.nio

import io.ktor.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import java.io.EOFException
import java.nio.channels.*
import kotlin.require

/**
 * Builds packet and write it to a NIO channel. May block if the channel is configured as blocking or
 * may write packet partially so this function returns remaining packet. So for blocking channel this
 * function always returns `null`.
 */
public fun WritableByteChannel.writePacket(builder: Packet.() -> Unit): Packet? {
    val packet = buildPacket(block = builder)
    return try {
        if (writePacket(packet)) null else packet
    } catch (cause: Throwable) {
        packet.close()
        throw cause
    }
}

/**
 * Writes packet to a NIO channel. May block if the channel is configured as blocking or may write packet
 * only partially if the channel is non-blocking and there is not enough buffer space.
 * @return `true` if the whole packet has been written to the channel
 */
public fun WritableByteChannel.writePacket(packet: Packet): Boolean {
    TODO()
}

/**
 * Read a packet of exactly [n] bytes. This function is useless with non-blocking channels
 */
public fun ReadableByteChannel.readPacketExact(n: Long): Packet = TODO()

/**
 * Read a packet of at least [n] bytes or all remaining. Does fail if not enough bytes remaining.
 * . This function is useless with non-blocking channels
 */
public fun ReadableByteChannel.readPacketAtLeast(n: Long): Packet = TODO()

/**
 * Read a packet of at most [n] bytes. Resulting packet could be empty however this function always reads
 * as many bytes as possible. You also can use it with non-blocking channels
 */
public fun ReadableByteChannel.readPacketAtMost(n: Long): Packet = TODO()

/**
 * Does the same as [WritableByteChannel.write] but from a [DROP_Buffer] instance
 */
@Deprecated("Use write(Memory) instead.")
public fun WritableByteChannel.write(buffer: Buffer): Int {
    TODO()
}


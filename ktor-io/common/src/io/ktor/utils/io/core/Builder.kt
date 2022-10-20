package io.ktor.utils.io.core

import io.ktor.io.*
import kotlin.contracts.*

/**
 * Build a byte packet in [block] lambda. Creates a temporary builder and releases it in case of failure
 */
@OptIn(ExperimentalContracts::class)
public inline fun buildPacket(block: Packet.() -> Unit): Packet {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val packet = Packet()
    try {
        block(packet)
        return packet
    } catch (cause: Throwable) {
        packet.close()
        throw cause
    }
}

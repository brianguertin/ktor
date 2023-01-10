/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.internal.EmptyBuffer.getByteAt
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

public fun Packet(array: ByteArray): Packet = Packet().apply {
    writeByteArray(array)
}

public fun Packet(value: String): Packet = Packet().apply {
    writeString(value)
}

public val Packet.isEmpty: Boolean get() = availableForRead == 0

public val Packet.isNotEmpty: Boolean get() = availableForRead > 0


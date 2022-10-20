/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.tls

import io.ktor.io.*
import io.ktor.utils.io.core.*

internal fun Digest(): Digest = Digest(Packet())

@JvmInline
internal value class Digest(val state: Packet) : Closeable {

    fun update(packet: Packet) = synchronized(state) {
        if (packet.isEmpty) return
        state.writePacket(packet.clone())
    }

    fun doHash(hashName: String): ByteArray = TODO()

    override fun close() {
        state.close()
    }
}

internal operator fun Digest.plusAssign(record: TLSHandshake) {
    check(record.type != TLSHandshakeType.HelloRequest)

    update(
        buildPacket {
            writeTLSHandshakeType(record.type, record.packet.availableForRead)
            if (record.packet.isNotEmpty) writePacket(record.packet.clone())
        }
    )
}

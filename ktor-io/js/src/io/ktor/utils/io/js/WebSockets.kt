package io.ktor.utils.io.js

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import org.khronos.webgl.*
import org.w3c.dom.*

public fun WebSocket.sendPacket(packet: DROP_ByteReadPacket) {
    send(packet.readArrayBuffer())
}

public inline fun WebSocket.sendPacket(block: DROP_BytePacketBuilder.() -> Unit) {
    sendPacket(buildPacket(block = block))
}

public inline fun MessageEvent.packet(): DROP_ByteReadPacket {
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE", "UnsafeCastFromDynamic")
    return DROP_ByteReadPacket(
        DROP_ChunkBuffer(DROP_Memory.of(data.asDynamic() as DataView), null, DROP_ChunkBuffer.NoPool),
        DROP_ChunkBuffer.NoPool
    )
}

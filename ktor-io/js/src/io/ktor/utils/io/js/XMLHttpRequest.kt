package io.ktor.utils.io.js

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import org.khronos.webgl.*
import org.w3c.xhr.*

public inline fun XMLHttpRequest.sendPacket(block: DROP_BytePacketBuilder.() -> Unit) {
    sendPacket(buildPacket(block = block))
}

public fun XMLHttpRequest.sendPacket(packet: DROP_ByteReadPacket) {
    send(packet.readArrayBuffer())
}

@Suppress("UnsafeCastFromDynamic", "DEPRECATION")
public fun XMLHttpRequest.responsePacket(): DROP_ByteReadPacket = when (responseType) {
    XMLHttpRequestResponseType.ARRAYBUFFER -> DROP_ByteReadPacket(
        DROP_ChunkBuffer(
            DROP_Memory.of(response.asDynamic() as DataView),
            null,
            DROP_ChunkBuffer.NoPool
        ),
        DROP_ChunkBuffer.NoPoolManuallyManaged
    )
    XMLHttpRequestResponseType.EMPTY -> DROP_ByteReadPacket.Empty
    else -> throw IllegalStateException("Incompatible type $responseType: only ARRAYBUFFER and EMPTY are supported")
}

package io.ktor.utils.io.js

import io.ktor.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import org.khronos.webgl.*
import org.w3c.xhr.*

public inline fun XMLHttpRequest.sendPacket(block: Packet.() -> Unit) {
    sendPacket(buildPacket(block = block))
}

public fun XMLHttpRequest.sendPacket(packet: Packet) {
    send(packet.readArrayBuffer())
}

@Suppress("UnsafeCastFromDynamic", "DEPRECATION")
public fun XMLHttpRequest.responsePacket(): Packet = when (responseType) {
    XMLHttpRequestResponseType.ARRAYBUFFER -> TODO()
    XMLHttpRequestResponseType.EMPTY -> Packet.Empty
    else -> throw IllegalStateException("Incompatible type $responseType: only ARRAYBUFFER and EMPTY are supported")
}

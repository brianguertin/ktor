/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.http.cio

import io.ktor.http.*
import io.ktor.io.*

/**
 * Builds an HTTP request or response
 */
public class RequestResponseBuilder {
    private val packet = Packet()

    /**
     * Append response status line
     */
    public fun responseLine(version: CharSequence, status: Int, statusText: CharSequence) {
        packet.writeString(version)
        packet.writeByte(SP)
        packet.writeString(status.toString())
        packet.writeByte(SP)
        packet.writeString(statusText)
        packet.writeByte(CR)
        packet.writeByte(LF)
    }

    /**
     * Append request line
     */
    public fun requestLine(method: HttpMethod, uri: CharSequence, version: CharSequence) {
        packet.writeString(method.value)
        packet.writeByte(SP)
        packet.writeString(uri)
        packet.writeByte(SP)
        packet.writeString(version)
        packet.writeByte(CR)
        packet.writeByte(LF)
    }

    /**
     * Append a line
     */
    public fun line(line: CharSequence) {
        packet.writeString(line)
        packet.writeByte(CR)
        packet.writeByte(LF)
    }

    /**
     * Append raw bytes
     */
    public fun bytes(content: ByteArray, offset: Int = 0, length: Int = content.size - offset) {
        packet.writeByteArray(content, offset, length)
    }

    /**
     * Append header line
     */
    public fun headerLine(name: String, value: String) {
        packet.writeString(name)
        packet.writeString(": ")
        packet.writeString(value)
        packet.writeByte(CR)
        packet.writeByte(LF)
    }

    /**
     * Append an empty line (CR + LF in fact)
     */
    public fun emptyLine() {
        packet.writeByte(CR)
        packet.writeByte(LF)
    }

    /**
     * Build a packet of request/response
     */
    public fun build(): Packet = packet

    /**
     * Release all resources hold by the builder
     */
    public fun release() {
        packet.close()
    }
}


private const val SP: Byte = 0x20
private const val CR: Byte = 0x0d
private const val LF: Byte = 0x0a

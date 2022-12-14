/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.io.charsets.*
import kotlinx.coroutines.*

/**
 * Creates channel for reading from the specified byte array. Please note that it could use [content] directly
 * or copy its bytes depending on the platform.
 */
public fun ByteReadChannel(content: ByteArray, offset: Int = 0, length: Int = content.size): ByteReadChannel {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= content.size) {
        "offset + length shouldn't be greater than content size: ${content.size}"
    }

    return ByteReadChannel {
        writeByteArray(content, offset, length)
    }
}

public fun ByteReadChannel(packet: Packet): ByteReadChannel = object : ByteReadChannel {
    override val isClosedForRead: Boolean
        get() = packet.isEmpty

    override var closedCause: Throwable? = null
        private set

    override val readablePacket: Packet = packet

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean = predicate()

    override fun cancel(cause: Throwable?): Boolean {
        if (closedCause != null || packet.isEmpty) return false
        readablePacket.close()
        closedCause = cause
        return true
    }
}

public fun ByteReadChannel(
    block: suspend ByteWriteChannel.() -> Unit
): ByteReadChannel = GlobalScope.writer(Dispatchers.Unconfined) {
    block()
}



public fun ByteReadChannel(text: String, charset: Charset = Charsets.UTF_8): ByteReadChannel = ByteReadChannel {
    writeString(text, charset)
}

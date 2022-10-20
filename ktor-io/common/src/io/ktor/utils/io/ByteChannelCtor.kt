/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ktor.utils.io

import io.ktor.io.*
import io.ktor.utils.io.charsets.*
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

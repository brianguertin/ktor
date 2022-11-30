/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.io.*

public fun ByteWriteChannel.transform(
    block: suspend ByteWriteChannelTransformer.() -> Unit
): ByteWriteChannel = object : ByteWriteChannel, ByteWriteChannelTransformer {
    override val isClosedForWrite: Boolean
        get() = TODO("Not yet implemented")
    override val closedCause: Throwable?
        get() = TODO("Not yet implemented")
    override val writablePacket: Packet
        get() = TODO("Not yet implemented")

    override fun close(cause: Throwable?): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun flush() {
        TODO("Not yet implemented")
    }

    override fun onFlush(block: suspend ByteWriteChannel.(chunk: Packet) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun onClose(block: suspend ByteWriteChannel.(lastChunk: Packet) -> Unit) {
        TODO("Not yet implemented")
    }
}

public interface ByteWriteChannelTransformer {
    public fun onFlush(block: suspend ByteWriteChannel.(chunk: Packet) -> Unit)

    public fun onClose(block: suspend ByteWriteChannel.(lastChunk: Packet) -> Unit)
}


/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import java.nio.*

internal val EmptyByteBuffer: ByteBuffer = ByteBuffer.allocate(0)

public interface WithByteBuffer {
    public val state: ByteBuffer

    public fun readByteBuffer(): ByteBuffer
}

public fun Packet.writeByteBuffer(value: ByteBuffer) {
    val buffer = ByteBufferBuffer(value)
    writeBuffer(buffer)
}


public fun ReadableBuffer.readByteBuffer(): ByteBuffer {
    if (this is WithByteBuffer) {
        return readByteBuffer()
    }

    return ByteBuffer.wrap(toByteArray())
}

public fun Packet.readByteBuffer(): ByteBuffer {
    return readBuffer().readByteBuffer()
}

public suspend fun ByteReadChannel.readByteBuffer(): ByteBuffer {
    if (!awaitBytes()) return EmptyByteBuffer
    return readablePacket.readByteBuffer()
}

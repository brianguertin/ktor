/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import java.nio.*

public interface WithByteBuffer {
    public val state: ByteBuffer
}

public fun Packet.writeByteBuffer(value: ByteBuffer) {
    val buffer = ByteBufferBuffer(value)
    writeBuffer(buffer)
}


public fun ReadableBuffer.readByteBuffer(): ByteBuffer {
    if (this is WithByteBuffer) {
        return state
    }

    return ByteBuffer.wrap(toByteArray())
}

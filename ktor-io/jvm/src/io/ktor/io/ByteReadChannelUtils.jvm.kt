/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import java.nio.*

public suspend fun ByteReadChannel.readByteBuffer(): ByteBuffer {
    if (!isClosedForRead && availableForRead == 0) awaitBytes()
    if (isClosedForRead) throw EOFException()

    return readablePacket.readBuffer().readByteBuffer()
}

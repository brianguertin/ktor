/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.utils.io

import io.ktor.utils.io.core.*
import org.khronos.webgl.*

public suspend fun ByteReadChannel.readFully(dst: ArrayBuffer, offset: Int, length: Int) {
    check(this is ByteChannelJS)
    if (availableForRead >= length) {
        closedCause?.let { throw it }
        readable.readFully(dst, offset, length)
        afterRead(length - offset)
        return
    }

    return readFullySuspend(dst, offset, length)
}

public suspend fun ByteReadChannel.readAvailable(dst: ArrayBuffer, offset: Int, length: Int): Int {
    check(this is ByteChannelJS)

    return if (readable.isEmpty) {
        readAvailableSuspend(dst, offset, length)
    } else {
        closedCause?.let { throw it }
        val count = readable.readAvailable(dst, offset, length)
        afterRead(count)
        count
    }
}

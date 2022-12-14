/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.*

internal object EmptyByteReadChannel : ByteReadChannel {
    override val isClosedForRead: Boolean = true
    override val closedCause: Throwable? = null
    override val readablePacket: Packet = Packet()

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        return false
    }

    override fun cancel(cause: Throwable?): Boolean {
        return false
    }
}

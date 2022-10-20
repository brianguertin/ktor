/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.servlet

import io.ktor.io.*
import io.ktor.utils.io.*
import io.ktor.utils.io.pool.*
import javax.servlet.*

internal fun servletWriter(output: ServletOutputStream): ByteWriteChannel = ServletWriter(output)

internal val ArrayPool = object : DefaultPool<ByteArray>(1024) {
    override fun produceInstance() = ByteArray(4096)
    override fun validateInstance(instance: ByteArray) {
        if (instance.size != 4096) {
            throw IllegalArgumentException(
                "Tried to recycle wrong ByteArray instance: most likely it hasn't been borrowed from this pool"
            )
        }
    }
}

private const val MAX_COPY_SIZE = 512 * 1024 // 512K

private class ServletWriter(val output: ServletOutputStream) : WriteListener, ByteWriteChannel {
    override fun onWritePossible() {
        TODO("Not yet implemented")
    }

    override fun onError(t: Throwable?) {
        TODO("Not yet implemented")
    }

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
}

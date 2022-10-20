/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.server.servlet

import io.ktor.io.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import javax.servlet.*

internal fun CoroutineScope.servletReader(input: ServletInputStream, contentLength: Int): ByteReadChannel =
    ServletReader(input, contentLength)

private class ServletReader(val input: ServletInputStream, val contentLength: Int) : ReadListener, ByteReadChannel {
    override fun onDataAvailable() {
        TODO("Not yet implemented")
    }

    override fun onAllDataRead() {
        TODO("Not yet implemented")
    }

    override fun onError(t: Throwable?) {
        TODO("Not yet implemented")
    }

    override val isClosedForRead: Boolean
        get() = TODO("Not yet implemented")
    override val closedCause: Throwable?
        get() = TODO("Not yet implemented")
    override val readablePacket: Packet
        get() = TODO("Not yet implemented")

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun cancel(cause: Throwable?): Boolean {
        TODO("Not yet implemented")
    }
}

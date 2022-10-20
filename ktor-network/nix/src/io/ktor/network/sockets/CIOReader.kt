/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets

import io.ktor.io.*
import io.ktor.network.selector.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import platform.posix.*

internal fun CoroutineScope.attachForReadingImpl(
    descriptor: Int,
    selectable: Selectable,
    selector: SelectorManager
): ByteReadChannel = object : ByteReadChannel {
    override val isClosedForRead: Boolean
        get() = TODO("Not yet implemented")
    override val closedCause: Throwable?
        get() = TODO("Not yet implemented")
    override val readablePacket: Packet
        get() = TODO("Not yet implemented")

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        TODO()
    }

    override fun cancel(cause: Throwable?): Boolean {
        TODO("Not yet implemented")
    }

}
/**
writer(Dispatchers.Unconfined) {
while (!channel.isClosedForWrite) {
var close = false
TODO()
//        val count = channel.write { memory, startIndex, endIndex ->
//            val bufferStart = memory.pointer + startIndex
//            val size = endIndex - startIndex

//
//            bytesRead
//        }

channel.flush()
if (close) {
channel.close()
break
}

if (count == 0) {
try {
selector.select(selectable, SelectInterest.READ)
} catch (_: IOException) {
break
}
}
}
//.apply {
//    invokeOnCompletion {
//        shutdown(descriptor, SHUT_RD)
//    }
//}

channel.closedCause?.let { throw it }
}
 */

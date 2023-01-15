/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.utils

import io.ktor.client.content.*
import io.ktor.io.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
internal fun ByteReadChannel.observable(
    context: CoroutineContext,
    contentLength: Long?,
    listener: ProgressListener
): ByteReadChannel = GlobalScope.writer(context) {
    val total = contentLength ?: -1
    var bytesSend = 0L
    while (this@observable.awaitBytes()) {
        bytesSend += this@observable.readablePacket.availableForRead
        listener(bytesSend, total)
        writePacket(this@observable.readablePacket)
        flush()
    }
}

/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.util.cio

import io.ktor.io.*
import io.ktor.io.pool.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.*
import kotlin.coroutines.*

/**
 * Open a channel and launch a coroutine to copy bytes from the input stream to the channel.
 * Please note that it may block your async code when started on [Dispatchers.Unconfined]
 * since [InputStream] is blocking on it's nature
 */
@OptIn(DelicateCoroutinesApi::class)
public fun InputStream.toByteReadChannel(
    pool: ObjectPool<ByteBuffer> = KtorDefaultPool,
    context: CoroutineContext = Dispatchers.IO
): ByteReadChannel = GlobalScope.writer(context) {
    try {
        while (true) {
            coroutineContext.ensureActive()

            val buffer = ByteBuffer.allocate(8192)
            val readCount = read(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())
            if (readCount < 0) break
            if (readCount == 0) continue

            buffer.position(buffer.position() + readCount)
            buffer.flip()
            writeByteBuffer(buffer)
            flush()
        }
    } catch (cause: Throwable) {
        this@toByteReadChannel.close()
        throw cause
    } finally {
        this@toByteReadChannel.close()
    }
}

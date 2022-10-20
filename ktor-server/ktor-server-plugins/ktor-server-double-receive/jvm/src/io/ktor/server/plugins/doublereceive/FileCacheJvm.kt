// ktlint-disable filename
/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.plugins.doublereceive

import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.*
import kotlin.coroutines.*

internal actual class FileCache actual constructor(
    private val body: ByteReadChannel,
    bufferSize: Int,
    context: CoroutineContext
) : DoubleReceiveCache {
    private val file = File.createTempFile("ktor-double-receive-cache", ".tmp")

    @OptIn(DelicateCoroutinesApi::class)
    private val saveJob = GlobalScope.launch(context + Dispatchers.IO) {
        TODO()
    }

    actual override fun read(): ByteReadChannel = file.readChannel()

    actual override fun dispose() {
        runCatching {
            saveJob.cancel()
        }
        runCatching {
            file.delete()
        }
        if (!body.isClosedForRead) {
            runCatching {
                body.cancel()
            }
        }
    }
}

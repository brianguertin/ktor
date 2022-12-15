/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.winhttp.internal

import io.ktor.io.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

internal fun WinHttpRequest.readBody(
    callContext: CoroutineContext
): ByteReadChannel = GlobalScope.writer(callContext) {
    while (true) {
        val availableBytes = queryDataAvailable()
        if (availableBytes <= 0) break

        val content = ByteArray(availableBytes)
        val readBytes = content.usePinned { dst ->
            readData(dst, availableBytes)
        }

        writeByteArray(content, 0, readBytes)
    }
}

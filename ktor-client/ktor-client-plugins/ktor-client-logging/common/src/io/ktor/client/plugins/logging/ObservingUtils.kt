/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.client.plugins.logging

import io.ktor.http.content.*
import io.ktor.io.*
import io.ktor.util.*
import kotlinx.coroutines.*

internal suspend fun OutgoingContent.observe(log: ByteWriteChannel): OutgoingContent = when (this) {
    is OutgoingContent.ByteArrayContent -> {
        log.writeByteArray(bytes())
        log.close()
        this
    }
    is OutgoingContent.ReadChannelContent -> {
        val content = readFrom()

        val responseChannel = GlobalScope.writer(Dispatchers.Unconfined) {
            content.copyToBoth(log, this)
        }
        LoggedContent(this, responseChannel)
    }
    is OutgoingContent.WriteChannelContent -> {
        val content = toReadChannel()
        val responseChannel = GlobalScope.writer(Dispatchers.Unconfined) {
            content.copyToBoth(log, this)
        }
        LoggedContent(this, responseChannel)
    }
    else -> {
        log.close()
        this
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun OutgoingContent.WriteChannelContent.toReadChannel(): ByteReadChannel =
    GlobalScope.writer(Dispatchers.Default) {
        writeTo(this)
    }

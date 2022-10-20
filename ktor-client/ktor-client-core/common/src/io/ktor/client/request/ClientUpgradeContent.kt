/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.client.request

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*

@Suppress("KDocMissingDocumentation")
@InternalAPI
public abstract class ClientUpgradeContent : OutgoingContent.NoContent() {
    public val output: ByteWriteChannel get() = TODO()

    public suspend fun pipeTo(output: ByteWriteChannel) {
        TODO()
    }

    public abstract fun verify(headers: Headers)
}

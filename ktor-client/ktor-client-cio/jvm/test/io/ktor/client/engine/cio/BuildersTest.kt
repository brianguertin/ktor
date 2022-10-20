/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.client.engine.cio

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import kotlin.test.*

class BuildersTest {

    @Test
    fun testResolvingWsFunction() = runBlocking {
        try {
            HttpClient(CIO).ws("http://localhost") {}
        } catch (_: Throwable) {
            // no op
        }
    }

    @Test
    fun testGetGoogle() = runBlocking {
        HttpClient(CIO) {
            followRedirects = false
        }.use {
            val data = it.get("https://www.baeldung.com/java-filechannel")
            val message = data.bodyAsText()
            println(message)
        }
    }
}

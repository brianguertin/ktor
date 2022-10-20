/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.sockets.tests

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.test.*

class TcpSocketTest {

    @Test
    fun testEcho() = testSockets { selector ->
        val tcp = aSocket(selector).tcp()
        val server = tcp.bind("localhost", 8000)

        val serverConnectionPromise = async {
            server.accept()
        }

        val clientConnection = tcp.connect("localhost", 8000)
        val serverConnection = serverConnectionPromise.await()

        val clientOutput = clientConnection.attachForWriting()
        try {
            clientOutput.writeString("Hello, world\n")
            clientOutput.flush()
        } finally {
            clientOutput.close()
        }

        val serverInput = serverConnection.attachForReading()
        val message = serverInput.readLine()
        assertEquals("Hello, world", message)

        val serverOutput = serverConnection.attachForWriting()
        try {
            serverOutput.writeString("Hello From Server\n")
            serverOutput.flush()

            val clientInput = clientConnection.attachForReading()
            val echo = clientInput.readLine()

            assertEquals("Hello From Server", echo)
        } finally {
            serverOutput.close()
        }

        serverConnection.close()
        clientConnection.close()

        server.close()
    }

    @Test
    fun testEchoOverUnixSockets() = testSockets { selector ->
        if (!supportsUnixDomainSockets()) return@testSockets

        val socketPath = createTempFilePath("ktor-echo-test")

        val tcp = aSocket(selector).tcp()
        val server = tcp.bind(UnixSocketAddress(socketPath))

        val serverConnectionPromise = async {
            server.accept()
        }

        val clientConnection = tcp.connect(UnixSocketAddress(socketPath))
        val serverConnection = serverConnectionPromise.await()

        val clientOutput = clientConnection.attachForWriting()
        try {
            clientOutput.writeString("Hello, world\n")
            clientOutput.flush()
        } finally {
            clientOutput.close()
        }

        val serverInput = serverConnection.attachForReading()
        val message = serverInput.readLine()
        assertEquals("Hello, world", message)

        val serverOutput = serverConnection.attachForWriting()
        try {
            serverOutput.writeString("Hello From Server\n")
            serverOutput.flush()

            val clientInput = clientConnection.attachForReading()
            val echo = clientInput.readLine()

            assertEquals("Hello From Server", echo)
        } finally {
            serverOutput.close()
        }

        serverConnection.close()
        clientConnection.close()

        server.close()

        removeFile(socketPath)
    }
}


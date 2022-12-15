/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.network.tls

import io.ktor.io.*
import io.ktor.network.sockets.*
import io.ktor.network.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.nio.*
import kotlin.coroutines.*

internal actual suspend fun openTLSSession(
    socket: Socket,
    input: ByteReadChannel,
    output: ByteWriteChannel,
    config: TLSConfig,
    context: CoroutineContext
): Socket {
    val handshake = TLSClientHandshake(input, output, config, context)
    try {
        handshake.negotiate()
    } catch (cause: ClosedSendChannelException) {
        throw TLSException("Negotiation failed due to EOS", cause)
    }
    return TLSSocket(handshake.input, handshake.output, socket, context)
}

private class TLSSocket(
    private val input: ReceiveChannel<TLSRecord>,
    private val output: SendChannel<TLSRecord>,
    private val socket: Socket,
    override val coroutineContext: CoroutineContext
) : CoroutineScope, Socket by socket {

    override fun attachForReading(): ByteReadChannel =
        writer(coroutineContext + CoroutineName("cio-tls-input-loop")) {
            appDataInputLoop(this)
        }

    override fun attachForWriting(): ByteWriteChannel =
        reader(coroutineContext + CoroutineName("cio-tls-output-loop")) {
            appDataOutputLoop(this)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun appDataInputLoop(pipe: ByteWriteChannel) {
        try {
            input.consumeEach { record ->
                val packet = record.packet
                val length = packet.availableForRead
                when (record.type) {
                    TLSRecordType.ApplicationData -> {
                        pipe.writePacket(record.packet)
                        pipe.flush()
                    }
                    else -> throw TLSException("Unexpected record ${record.type} ($length bytes)")
                }
            }
        } catch (cause: Throwable) {
        } finally {
            pipe.close()
        }
    }

    private suspend fun appDataOutputLoop(
        pipe: ByteReadChannel
    ) {
        while (pipe.availableForRead > 0 || pipe.awaitBytes()) {
            output.send(TLSRecord(TLSRecordType.ApplicationData, packet = pipe.readablePacket.steal()))
        }
    }

    override fun dispose() {
        socket.dispose()
    }
}

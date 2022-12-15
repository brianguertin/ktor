/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.network.tls.cipher

import io.ktor.io.*
import io.ktor.io.pool.*
import io.ktor.network.util.*
import java.lang.Integer.*
import java.nio.*
import javax.crypto.*

internal fun Packet.cipherLoop(cipher: Cipher, header: Packet.() -> Unit = {}): Packet = buildPacket {
    header()

    var destination = ByteBuffer.allocate(4096)
    while (this@cipherLoop.isNotEmpty) {
        val srcBuffer = this@cipherLoop.readByteBuffer()
        val requiredOutputSize = cipher.getOutputSize(srcBuffer.remaining())
        if (requiredOutputSize > destination.remaining()) {
            destination.flip()
            writeByteBuffer(destination)
            destination = ByteBuffer.allocate(max(4096, requiredOutputSize))

        }

        cipher.update(srcBuffer, destination)
        check(!srcBuffer.hasRemaining())
    }

    destination.flip()

    val requiredBufferSize = cipher.getOutputSize(0)
    if (requiredBufferSize == 0 && !destination.hasRemaining()) return@buildPacket

    if (requiredBufferSize <= destination.remaining()) {
        if (requiredBufferSize != 0) cipher.doFinal(EmptyByteBuffer, destination)
        writeByteBuffer(destination)
    } else {
        destination.flip()
        writeByteBuffer(destination)
        writeByteArray(cipher.doFinal())
    }
}

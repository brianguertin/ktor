/*
 * Copyright 2014-2023 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public fun ByteReadChannel.limited(length: Long): ByteReadChannel = object : ByteReadChannel {
    var remaining = length

    override var closedCause: Throwable? = null
        private set

    override val readablePacket: Packet = Packet()

    override suspend fun awaitBytes(predicate: () -> Boolean): Boolean {
        try {
            while (!predicate() && remaining > 0) {
                if (remaining <= 0) return readablePacket.isNotEmpty
                if (this@limited.readablePacket.isEmpty) this@limited.awaitBytes()
                if (remaining > this@limited.readablePacket.availableForRead) {
                    remaining -= this@limited.readablePacket.availableForRead
                    readablePacket.writePacket(this@limited.readablePacket)
                } else {
                    val packet = this@limited.readPacket(remaining.toInt())
                    remaining = 0
                    readablePacket.writePacket(packet)
                }
            }
        } catch (cause: Throwable) {
            closedCause = cause
            throw cause
        }

        return readablePacket.isNotEmpty
    }

    override fun cancel(cause: Throwable?): Boolean {
        if (closedCause != null) return false

        this@limited.cancel(cause)
        closedCause = cause
        return true
    }
}

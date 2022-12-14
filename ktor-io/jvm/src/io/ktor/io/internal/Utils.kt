package io.ktor.io.internal

import java.nio.*

internal fun ByteBuffer.isEmpty() = !hasRemaining()

internal fun ByteBuffer.startsWith(prefix: ByteBuffer, prefixSkip: Int = 0): Boolean {
    val size = minOf(remaining(), prefix.remaining() - prefixSkip)
    if (size <= 0) return false

    val position = position()
    val prefixPosition = prefix.position() + prefixSkip

    for (i in 0 until size) {
        if (get(position + i) != prefix.get(prefixPosition + i)) return false
    }

    return true
}

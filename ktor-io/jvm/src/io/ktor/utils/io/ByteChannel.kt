package io.ktor.utils.io

import java.nio.*

/**
 * Creates channel for reading from the specified byte buffer.
 */
public fun ByteReadChannel(content: ByteBuffer): ByteReadChannel = ByteReadChannel {
    writeByteBuffer(content)
}

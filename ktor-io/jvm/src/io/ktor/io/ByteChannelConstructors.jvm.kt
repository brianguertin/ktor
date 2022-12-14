/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import java.nio.*

/**
 * Creates channel for reading from the specified byte buffer.
 */
public fun ByteReadChannel(content: ByteBuffer): ByteReadChannel = ByteReadChannel {
    writeByteBuffer(content)
}

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.internal

import io.ktor.io.pool.*
import java.nio.*

internal val BUFFER_SIZE = getIOIntProperty("BufferSize", 4096)
private val BUFFER_POOL_SIZE = getIOIntProperty("BufferPoolSize", 2048)

internal val BufferPool: ObjectPool<ByteBuffer> = DirectByteBufferPool(BUFFER_POOL_SIZE, BUFFER_SIZE)

/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util.cio

import io.ktor.io.pool.*
import io.ktor.io.pool.DefaultPool
import io.ktor.util.*
import io.ktor.io.pool.ByteBufferPool
import java.nio.*

internal const val DEFAULT_BUFFER_SIZE = 4098
internal const val DEFAULT_KTOR_POOL_SIZE = 2048

/**
 * The default ktor byte buffer pool
 */
public val KtorDefaultPool: ObjectPool<ByteBuffer> = ByteBufferPool(DEFAULT_KTOR_POOL_SIZE, DEFAULT_BUFFER_SIZE)

@InternalAPI
@Suppress("KDocMissingDocumentation")
@Deprecated(
    level = DeprecationLevel.ERROR,
    message = "ByteBufferPool is moved to `io` module",
    replaceWith = ReplaceWith("ByteBufferPool", "io.ktor.utils.io.pool.ByteBufferPool")
)
public class ByteBufferPool : DefaultPool<ByteBuffer>(DEFAULT_KTOR_POOL_SIZE) {
    override fun produceInstance(): ByteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    override fun clearInstance(instance: ByteBuffer): ByteBuffer = instance.apply { clear() }
}

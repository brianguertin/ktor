/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.pool

import io.ktor.io.*

/**
 * Borrows and instance of [T] from the pool, invokes [block] with it and finally recycles it
 */
public inline fun <T : Any, R> ObjectPool<T>.useInstance(block: (T) -> R): R {
    val instance = borrow()
    try {
        return block(instance)
    } finally {
        recycle(instance)
    }
}

public interface ObjectPool<T : Any> : Closeable {
    /**
     * Pool capacity
     */
    public val capacity: Int

    /**
     * borrow an instance. Pool can recycle an old instance or create a new one
     */
    public fun borrow(): T

    /**
     * Recycle an instance. Should be recycled what was borrowed before otherwise could fail
     */
    public fun recycle(instance: T)

    /**
     * Dispose the whole pool. None of borrowed objects could be used after the pool gets disposed
     * otherwise it can result in undefined behaviour
     */
    public fun dispose()

    /**
     * Does pool dispose
     */
    override fun close() {
        dispose()
    }
}

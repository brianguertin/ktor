/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.pool

import kotlinx.atomicfu.*

/**
 * A pool that produces at most one instance
 */
public abstract class SingleInstancePool<T : Any> : ObjectPool<T> {
    private val borrowed = atomic(0)
    private val disposed = atomic(false)

    private val instance = atomic<T?>(null)

    /**
     * Creates a new instance of [T]
     */
    protected abstract fun produceInstance(): T

    /**
     * Dispose [instance] and release its resources
     */
    protected abstract fun disposeInstance(instance: T)

    final override val capacity: Int get() = 1

    final override fun borrow(): T {
        borrowed.update {
            if (it != 0) throw IllegalStateException("Instance is already consumed")
            1
        }

        val instance = produceInstance()
        this.instance.value = instance

        return instance
    }

    final override fun recycle(instance: T) {
        if (this.instance.value !== instance) {
            if (this.instance.value == null && borrowed.value != 0) {
                throw IllegalStateException("Already recycled or an irrelevant instance tried to be recycled")
            }

            throw IllegalStateException("Unable to recycle irrelevant instance")
        }

        this.instance.value = null

        if (!disposed.compareAndSet(false, true)) {
            throw IllegalStateException("An instance is already disposed")
        }

        disposeInstance(instance)
    }

    final override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            val value = instance.value ?: return
            instance.value = null

            disposeInstance(value)
        }
    }
}

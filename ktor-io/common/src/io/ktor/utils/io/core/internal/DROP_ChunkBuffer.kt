package io.ktor.utils.io.core.internal

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.pool.*
import kotlinx.atomicfu.*

public open class DROP_ChunkBuffer(
    memory: DROP_Memory,
    origin: DROP_ChunkBuffer?,
    internal val parentPool: ObjectPool<DROP_ChunkBuffer>?
) : DROP_Buffer(memory) {
    init {
        require(origin !== this) { "A chunk couldn't be a view of itself." }
    }

    private val nextRef: AtomicRef<DROP_ChunkBuffer?> = atomic(null)
    private val refCount = atomic(1)

    /**
     * Reference to an origin buffer view this was copied from
     */
    public var origin: DROP_ChunkBuffer? = origin
        private set

    /**
     * Reference to next buffer view. Useful to chain multiple views.
     * @see appendNext
     * @see cleanNext
     */
    public var next: DROP_ChunkBuffer?
        get() = nextRef.value
        set(newValue) {
            if (newValue == null) {
                cleanNext()
            } else {
                appendNext(newValue)
            }
        }

    public val referenceCount: Int get() = refCount.value

    private fun appendNext(chunk: DROP_ChunkBuffer) {
        if (!nextRef.compareAndSet(null, chunk)) {
            throw IllegalStateException("This chunk has already a next chunk.")
        }
    }

    public fun cleanNext(): DROP_ChunkBuffer? {
        return nextRef.getAndSet(null)
    }

    override fun duplicate(): DROP_ChunkBuffer = (origin ?: this).let { newOrigin ->
        newOrigin.acquire()
        DROP_ChunkBuffer(memory, newOrigin, parentPool).also { copy ->
            duplicateTo(copy)
        }
    }

    public open fun release(pool: ObjectPool<DROP_ChunkBuffer>) {
        if (release()) {
            val origin = origin
            if (origin != null) {
                unlink()
                origin.release(pool)
            } else {
                val poolToUse = parentPool ?: pool
                poolToUse.recycle(this)
            }
        }
    }

    internal fun unlink() {
        if (!refCount.compareAndSet(0, -1)) {
            throw IllegalStateException("Unable to unlink: buffer is in use.")
        }

        cleanNext()
        origin = null
    }

    /**
     * Increase ref-count. May fail if already released.
     */
    internal fun acquire() {
        refCount.update { old ->
            if (old <= 0) throw IllegalStateException("Unable to acquire chunk: it is already released.")
            old + 1
        }
    }

    /**
     * Invoked by a pool before return the instance to a user.
     */
    internal fun unpark() {
        refCount.update { old ->
            if (old < 0) {
                throw IllegalStateException("This instance is already disposed and couldn't be borrowed.")
            }
            if (old > 0) {
                throw IllegalStateException("This instance is already in use but somehow appeared in the pool.")
            }

            1
        }
    }

    /**
     * Release ref-count.
     * @return `true` if the last usage was released
     */
    internal fun release(): Boolean {
        return refCount.updateAndGet { old ->
            if (old <= 0) throw IllegalStateException("Unable to release: it is already released.")
            old - 1
        } == 0
    }

    final override fun reset() {
        require(origin == null) { "Unable to reset buffer with origin" }

        super.reset()
        nextRef.value = null
    }

    public companion object {
        public val Pool: ObjectPool<DROP_ChunkBuffer> = object : ObjectPool<DROP_ChunkBuffer> {
            override val capacity: Int
                get() = DefaultChunkedBufferPool.capacity

            override fun borrow(): DROP_ChunkBuffer {
                return DefaultChunkedBufferPool.borrow()
            }

            override fun recycle(instance: DROP_ChunkBuffer) {
                DefaultChunkedBufferPool.recycle(instance)
            }

            override fun dispose() {
                DefaultChunkedBufferPool.dispose()
            }
        }

        /**
         * A pool that always returns [DROP_ChunkBuffer.Empty]
         */
        public val EmptyPool: ObjectPool<DROP_ChunkBuffer> = object : ObjectPool<DROP_ChunkBuffer> {
            override val capacity: Int get() = 1

            override fun borrow() = Empty

            override fun recycle(instance: DROP_ChunkBuffer) {
                require(instance === Empty) { "Only ChunkBuffer.Empty instance could be recycled." }
            }

            override fun dispose() {
            }
        }

        public val Empty: DROP_ChunkBuffer = DROP_ChunkBuffer(DROP_Memory.Empty, null, EmptyPool)

        internal val NoPool: ObjectPool<DROP_ChunkBuffer> = object : NoPoolImpl<DROP_ChunkBuffer>() {
            override fun borrow(): DROP_ChunkBuffer {
                return DROP_ChunkBuffer(DefaultAllocator.alloc(DEFAULT_BUFFER_SIZE), null, this)
            }

            override fun recycle(instance: DROP_ChunkBuffer) {
                DefaultAllocator.free(instance.memory)
            }
        }

        internal val NoPoolManuallyManaged: ObjectPool<DROP_ChunkBuffer> = object : NoPoolImpl<DROP_ChunkBuffer>() {
            override fun borrow(): DROP_ChunkBuffer {
                throw UnsupportedOperationException("This pool doesn't support borrow")
            }

            override fun recycle(instance: DROP_ChunkBuffer) {
                // do nothing: manually managed objects should be disposed manually
            }
        }
    }
}

/**
 * @return `true` if and only if there are no buffer views that share the same actual buffer. This actually does
 * refcount and only work guaranteed if other views created/not created via [DROP_Buffer.duplicate] function.
 * One can instantiate multiple buffers with the same buffer and this function will return `true` in spite of
 * the fact that the buffer is actually shared.
 */
internal fun DROP_ChunkBuffer.isExclusivelyOwned(): Boolean = referenceCount == 1

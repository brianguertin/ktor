@file:Suppress("NOTHING_TO_INLINE")

package io.ktor.utils.io.bits

import io.ktor.utils.io.core.internal.*
import java.nio.*
import kotlin.contracts.*

/**
 * Execute [block] of code providing a temporary instance of [DROP_Memory] view of this byte array range
 * starting at the specified [offset] and having the specified bytes [length].
 * By default, if neither [offset] nor [length] specified, the whole array is used.
 * An instance of [DROP_Memory] provided into the [block] should be never captured and used outside of lambda.
 */
@OptIn(ExperimentalContracts::class)
public actual inline fun <R> ByteArray.useMemory(offset: Int, length: Int, block: (DROP_Memory) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return DROP_Memory(ByteBuffer.wrap(this, offset, length).slice().order(ByteOrder.BIG_ENDIAN)).let(block)
}

/**
 * Create [DROP_Memory] view for the specified [array] range starting at [offset] and the specified bytes [length].
 */
public inline fun DROP_Memory.Companion.of(array: ByteArray, offset: Int = 0, length: Int = array.size - offset): DROP_Memory {
    return DROP_Memory(ByteBuffer.wrap(array, offset, length).slice().order(ByteOrder.BIG_ENDIAN))
}

/**
 * Create [DROP_Memory] view for the specified [buffer] range
 * starting at [ByteBuffer.position] and ending at [ByteBuffer.limit].
 * Changing the original buffer's position/limit will not affect previously created Memory instances.
 */
public inline fun DROP_Memory.Companion.of(buffer: ByteBuffer): DROP_Memory {
    return DROP_Memory(buffer.slice().order(ByteOrder.BIG_ENDIAN))
}

@PublishedApi
internal actual object DefaultAllocator : Allocator {
    override fun alloc(size: Int): DROP_Memory = DROP_Memory(ByteBuffer.allocate(size))

    override fun alloc(size: Long): DROP_Memory = alloc(size.toIntOrFail("size"))

    override fun free(instance: DROP_Memory) {
    }
}

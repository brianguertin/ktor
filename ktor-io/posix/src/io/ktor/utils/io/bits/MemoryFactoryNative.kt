@file:Suppress("NOTHING_TO_INLINE")

package io.ktor.utils.io.bits

import kotlinx.cinterop.*
import platform.posix.*
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

    return usePinned { pinned ->
        val memory = when {
            isEmpty() && offset == 0 && length == 0 -> DROP_Memory.Empty
            else -> DROP_Memory(pinned.addressOf(offset), length.toLong())
        }

        block(memory)
    }
}

/**
 * Create an instance of [DROP_Memory] view for memory region starting at
 * the specified [pointer] and having the specified [size] in bytes.
 */
@OptIn(UnsafeNumber::class)
public inline fun DROP_Memory.Companion.of(pointer: CPointer<*>, size: size_t): DROP_Memory {
    require(size.convert<ULong>() <= Long.MAX_VALUE.convert<ULong>()) {
        "At most ${Long.MAX_VALUE} (kotlin.Long.MAX_VALUE) bytes range is supported."
    }

    return of(pointer, size.convert())
}

/**
 * Create an instance of [DROP_Memory] view for memory region starting at
 * the specified [pointer] and having the specified [size] in bytes.
 */
public inline fun DROP_Memory.Companion.of(pointer: CPointer<*>, size: Int): DROP_Memory {
    return DROP_Memory(pointer.reinterpret(), size.toLong())
}

/**
 * Create an instance of [DROP_Memory] view for memory region starting at
 * the specified [pointer] and having the specified [size] in bytes.
 */
public inline fun DROP_Memory.Companion.of(pointer: CPointer<*>, size: Long): DROP_Memory {
    return DROP_Memory(pointer.reinterpret(), size)
}

/**
 * Allocate memory range having the specified [size] in bytes and provide an instance of [DROP_Memory] view for this range.
 * Please note that depending of the placement type (e.g. scoped or global) this memory instance may require
 * explicit release using [free] on the same placement.
 * In particular, instances created inside of [memScoped] block do not require to be released explicitly but
 * once the scope is leaved, all produced instances should be discarded and should be never used after the scope.
 * On the contrary instances created using [nativeHeap] do require release via [nativeHeap.free].
 */
public fun NativePlacement.allocMemory(size: Int): DROP_Memory {
    return allocMemory(size.toLong())
}

/**
 * Allocate memory range having the specified [size] in bytes and provide an instance of [DROP_Memory] view for this range.
 * Please note that depending of the placement type (e.g. scoped or global) this memory instance may require
 * explicit release using [free] on the same placement.
 * In particular, instances created inside of [memScoped] block do not require to be released explicitly but
 * once the scope is leaved, all produced instances should be discarded and should be never used after the scope.
 * On the contrary instances created using [nativeHeap] do require release via [nativeHeap.free].
 */
public fun NativePlacement.allocMemory(size: Long): DROP_Memory {
    return DROP_Memory(allocArray(size), size)
}

/**
 * Release resources that the [memory] instance is holding.
 * This function should be only used for memory instances that are produced by [allocMemory] function
 * otherwise an undefined behaviour may occur including crash or data corruption.
 */
public fun NativeFreeablePlacement.free(memory: DROP_Memory) {
    free(memory.pointer)
}

internal value class PlacementAllocator(private val placement: NativeFreeablePlacement) : Allocator {
    override fun alloc(size: Int): DROP_Memory = alloc(size.toLong())

    override fun alloc(size: Long): DROP_Memory = DROP_Memory(placement.allocArray(size), size)

    override fun free(instance: DROP_Memory) {
        placement.free(instance.pointer)
    }
}

@PublishedApi
internal actual object DefaultAllocator : Allocator by PlacementAllocator(nativeHeap)

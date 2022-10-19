package io.ktor.utils.io.bits

import io.ktor.utils.io.core.internal.*
import org.khronos.webgl.*
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
    return DROP_Memory.of(this, offset, length).let(block)
}

/**
 * Create [DROP_Memory] view for the specified [array] range starting at [offset] and the specified bytes [length].
 */
public fun DROP_Memory.Companion.of(array: ByteArray, offset: Int = 0, length: Int = array.size - offset): DROP_Memory {
    @Suppress("UnsafeCastFromDynamic")
    val typedArray: Int8Array = array.asDynamic()
    return DROP_Memory.of(typedArray, offset, length)
}

/**
 * Create [DROP_Memory] view for the specified [buffer] range starting at [offset] and the specified bytes [length].
 */
public fun DROP_Memory.Companion.of(buffer: ArrayBuffer, offset: Int = 0, length: Int = buffer.byteLength - offset): DROP_Memory {
    return DROP_Memory(DataView(buffer, offset, length))
}

/**
 * Create [DROP_Memory] view for the specified [view].
 */
public fun DROP_Memory.Companion.of(view: DataView): DROP_Memory {
    return DROP_Memory(view)
}

/**
 * Create [DROP_Memory] view for the specified [view] range starting at [offset] and the specified bytes [length].
 */
public fun DROP_Memory.Companion.of(view: ArrayBufferView, offset: Int = 0, length: Int = view.byteLength): DROP_Memory {
    return DROP_Memory.of(view.buffer, view.byteOffset + offset, length)
}

@PublishedApi
internal actual object DefaultAllocator : Allocator {
    override fun alloc(size: Int): DROP_Memory = DROP_Memory(DataView(ArrayBuffer(size)))
    override fun alloc(size: Long): DROP_Memory = DROP_Memory(DataView(ArrayBuffer(size.toIntOrFail("size"))))
    override fun free(instance: DROP_Memory) {
    }
}

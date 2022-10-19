package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public fun DROP_Input.readShort(): Short {
    return readPrimitive(2, { memory, index -> memory.loadShortAt(index) }, { readShortFallback() })
}

private fun DROP_Input.readShortFallback(): Short {
    return readPrimitiveFallback(2) { it.readShort() }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public fun DROP_Input.readInt(): Int {
    return readPrimitive(4, { memory, index -> memory.loadIntAt(index) }, { readIntFallback() })
}

private fun DROP_Input.readIntFallback(): Int {
    return readPrimitiveFallback(4) { it.readInt() }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public fun DROP_Input.readLong(): Long {
    return readPrimitive(8, { memory, index -> memory.loadLongAt(index) }, { readLongFallback() })
}

private fun DROP_Input.readLongFallback(): Long {
    return readPrimitiveFallback(8) { it.readLong() }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public fun DROP_Input.readFloat(): Float {
    return readPrimitive(4, { memory, index -> memory.loadFloatAt(index) }, { readFloatFallback() })
}

public fun DROP_Input.readFloatFallback(): Float {
    return readPrimitiveFallback(4) { it.readFloat() }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public fun DROP_Input.readDouble(): Double {
    return readPrimitive(8, { memory, index -> memory.loadDoubleAt(index) }, { readDoubleFallback() })
}

public fun DROP_Input.readDoubleFallback(): Double {
    return readPrimitiveFallback(8) { it.readDouble() }
}

private inline fun <R> DROP_Input.readPrimitive(size: Int, main: (DROP_Memory, Int) -> R, fallback: () -> R): R {
    if (headRemaining > size) {
        val index = headPosition
        headPosition = index + size
        return main(headMemory, index)
    }

    return fallback()
}

private inline fun <R> DROP_Input.readPrimitiveFallback(size: Int, read: (DROP_Buffer) -> R): R {
    val head = prepareReadFirstHead(size) ?: prematureEndOfStream(size)
    val value = read(head)
    completeReadHead(head)
    return value
}

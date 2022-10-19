@file:Suppress("Duplicates")

package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*

public fun DROP_Input.readShort(byteOrder: ByteOrder): Short =
    readPrimitiveTemplate(byteOrder, { readShort() }, { reverseByteOrder() })

public fun DROP_Input.readInt(byteOrder: ByteOrder): Int =
    readPrimitiveTemplate(byteOrder, { readInt() }, { reverseByteOrder() })

public fun DROP_Input.readLong(byteOrder: ByteOrder): Long =
    readPrimitiveTemplate(byteOrder, { readLong() }, { reverseByteOrder() })

public fun DROP_Input.readFloat(byteOrder: ByteOrder): Float =
    readPrimitiveTemplate(byteOrder, { readFloat() }, { reverseByteOrder() })

public fun DROP_Input.readDouble(byteOrder: ByteOrder): Double =
    readPrimitiveTemplate(byteOrder, { readDouble() }, { reverseByteOrder() })

public fun DROP_Input.readShortLittleEndian(): Short = readPrimitiveTemplate({ readShort() }, { reverseByteOrder() })

public fun DROP_Input.readIntLittleEndian(): Int = readPrimitiveTemplate({ readInt() }, { reverseByteOrder() })

public fun DROP_Input.readLongLittleEndian(): Long = readPrimitiveTemplate({ readLong() }, { reverseByteOrder() })

public fun DROP_Input.readFloatLittleEndian(): Float = readPrimitiveTemplate({ readFloat() }, { reverseByteOrder() })

public fun DROP_Input.readDoubleLittleEndian(): Double = readPrimitiveTemplate({ readDouble() }, { reverseByteOrder() })

public fun DROP_Buffer.readShortLittleEndian(): Short = readPrimitiveTemplate({ readShort() }, { reverseByteOrder() })

public fun DROP_Buffer.readIntLittleEndian(): Int = readPrimitiveTemplate({ readInt() }, { reverseByteOrder() })

public fun DROP_Buffer.readLongLittleEndian(): Long = readPrimitiveTemplate({ readLong() }, { reverseByteOrder() })

public fun DROP_Buffer.readFloatLittleEndian(): Float = readPrimitiveTemplate({ readFloat() }, { reverseByteOrder() })

public fun DROP_Buffer.readDoubleLittleEndian(): Double = readPrimitiveTemplate({ readDouble() }, { reverseByteOrder() })

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Input.readFullyLittleEndian(dst: UShortArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFullyLittleEndian(dst.asShortArray(), offset, length)
}

public fun DROP_Input.readFullyLittleEndian(dst: ShortArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Input.readFullyLittleEndian(dst: UIntArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFullyLittleEndian(dst.asIntArray(), offset, length)
}

public fun DROP_Input.readFullyLittleEndian(dst: IntArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Input.readFullyLittleEndian(dst: ULongArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFullyLittleEndian(dst.asLongArray(), offset, length)
}

public fun DROP_Input.readFullyLittleEndian(dst: LongArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

public fun DROP_Input.readFullyLittleEndian(dst: FloatArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

public fun DROP_Input.readFullyLittleEndian(dst: DoubleArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Input.readAvailableLittleEndian(dst: UShortArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    return readAvailableLittleEndian(dst.asShortArray(), offset, length)
}

public fun DROP_Input.readAvailableLittleEndian(dst: ShortArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Input.readAvailableLittleEndian(dst: UIntArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    return readAvailableLittleEndian(dst.asIntArray(), offset, length)
}

public fun DROP_Input.readAvailableLittleEndian(dst: IntArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Input.readAvailableLittleEndian(dst: ULongArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    return readAvailableLittleEndian(dst.asLongArray(), offset, length)
}

public fun DROP_Input.readAvailableLittleEndian(dst: LongArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

public fun DROP_Input.readAvailableLittleEndian(dst: FloatArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

public fun DROP_Input.readAvailableLittleEndian(dst: DoubleArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Buffer.readFullyLittleEndian(dst: UShortArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFullyLittleEndian(dst.asShortArray(), offset, length)
}

public fun DROP_Buffer.readFullyLittleEndian(dst: ShortArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Buffer.readFullyLittleEndian(dst: UIntArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFullyLittleEndian(dst.asIntArray(), offset, length)
}

public fun DROP_Buffer.readFullyLittleEndian(dst: IntArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Buffer.readFullyLittleEndian(dst: ULongArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFullyLittleEndian(dst.asLongArray(), offset, length)
}

public fun DROP_Buffer.readFullyLittleEndian(dst: LongArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

public fun DROP_Buffer.readFullyLittleEndian(dst: FloatArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

public fun DROP_Buffer.readFullyLittleEndian(dst: DoubleArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst, offset, length)
    val lastIndex = offset + length - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Buffer.readAvailableLittleEndian(dst: UShortArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    return readAvailableLittleEndian(dst.asShortArray(), offset, length)
}

public fun DROP_Buffer.readAvailableLittleEndian(dst: ShortArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    val lastIndex = offset + result - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
    return result
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Buffer.readAvailableLittleEndian(dst: UIntArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    return readAvailableLittleEndian(dst.asIntArray(), offset, length)
}

public fun DROP_Buffer.readAvailableLittleEndian(dst: IntArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    val lastIndex = offset + result - 1
    for (index in offset..lastIndex) {
        dst[index] = dst[index].reverseByteOrder()
    }
    return result
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun DROP_Buffer.readAvailableLittleEndian(dst: ULongArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    return readAvailableLittleEndian(dst.asLongArray(), offset, length)
}

public fun DROP_Buffer.readAvailableLittleEndian(dst: LongArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

public fun DROP_Buffer.readAvailableLittleEndian(dst: FloatArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

public fun DROP_Buffer.readAvailableLittleEndian(dst: DoubleArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    val result = readAvailable(dst, offset, length)
    if (result > 0) {
        val lastIndex = offset + result - 1
        for (index in offset..lastIndex) {
            dst[index] = dst[index].reverseByteOrder()
        }
    }
    return result
}

private inline fun <T : Any> readPrimitiveTemplate(read: () -> T, reverse: T.() -> T): T {
    return read().reverse()
}

private inline fun <T : Any> readPrimitiveTemplate(
    byteOrder: ByteOrder,
    read: () -> T,
    reverse: T.() -> T
): T {
    return when (byteOrder) {
        ByteOrder.BIG_ENDIAN -> read()
        else -> read().reverse()
    }
}

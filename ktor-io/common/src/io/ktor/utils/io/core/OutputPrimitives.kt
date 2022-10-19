package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*

public fun DROP_Output.writeShort(value: Short) {
    if (!writePrimitiveTemplate(2) { memory, index -> memory.storeShortAt(index, value) }) {
        writeShortFallback(value)
    }
}

private fun DROP_Output.writeShortFallback(value: Short) {
    if (!writePrimitiveFallbackTemplate(2) { it.writeShort(value) }) {
        writeByte(value.highByte)
        writeByte(value.lowByte)
    }
}

public fun DROP_Output.writeInt(value: Int) {
    if (!writePrimitiveTemplate(4) { memory, index -> memory.storeIntAt(index, value) }) {
        writeIntFallback(value)
    }
}

private fun DROP_Output.writeIntFallback(value: Int) {
    if (!writePrimitiveFallbackTemplate(4) { it.writeInt(value) }) {
        writeIntByteByByte(value)
    }
}

private fun DROP_Output.writeIntByteByByte(value: Int) {
    value.highShort.let {
        writeByte(it.highByte)
        writeByte(it.lowByte)
    }
    value.lowShort.let {
        writeByte(it.highByte)
        writeByte(it.lowByte)
    }
}

public fun DROP_Output.writeLong(value: Long) {
    if (!writePrimitiveTemplate(8) { memory, index -> memory.storeLongAt(index, value) }) {
        writeLongFallback(value)
    }
}

private fun DROP_Output.writeLongFallback(value: Long) {
    if (!writePrimitiveFallbackTemplate(8) { it.writeLong(value) }) {
        writeIntByteByByte(value.highInt)
        writeIntByteByByte(value.lowInt)
    }
}

public fun DROP_Output.writeFloat(value: Float) {
    if (!writePrimitiveTemplate(4) { memory, index -> memory.storeFloatAt(index, value) }) {
        writeIntFallback(value.toRawBits())
    }
}

public fun DROP_Output.writeDouble(value: Double) {
    if (!writePrimitiveTemplate(8) { memory, index -> memory.storeDoubleAt(index, value) }) {
        writeLongFallback(value.toRawBits())
    }
}

private inline fun DROP_Output.writePrimitiveTemplate(
    componentSize: Int,
    block: (DROP_Memory, index: Int) -> Unit
): Boolean {
    val index = tailPosition
    if (tailEndExclusive - index > componentSize) {
        tailPosition = index + componentSize
        block(tailMemory, index)
        return true
    }

    return false
}

private inline fun DROP_Output.writePrimitiveFallbackTemplate(
    componentSize: Int,
    writeOperation: (DROP_Buffer) -> Unit
): Boolean {
    val tail = prepareWriteHead(componentSize)
    writeOperation(tail)
    afterHeadWrite()
    return true
}

@file:Suppress("NOTHING_TO_INLINE")

package io.ktor.utils.io.core

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readUByte(): UByte = readByte().toUByte()

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readUShort(): UShort = readShort().toUShort()

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readUInt(): UInt = readInt().toUInt()

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readULong(): ULong = readLong().toULong()

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readFully(dst: UByteArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst.asByteArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readFully(dst: UShortArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst.asShortArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readFully(dst: UIntArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst.asIntArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Input.readFully(dst: ULongArray, offset: Int = 0, length: Int = dst.size - offset) {
    readFully(dst.asLongArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeUByte(v: UByte) {
    writeByte(v.toByte())
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeUShort(v: UShort) {
    writeShort(v.toShort())
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeUInt(v: UInt) {
    writeInt(v.toInt())
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeULong(v: ULong) {
    writeLong(v.toLong())
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeFully(array: UByteArray, offset: Int = 0, length: Int = array.size - offset) {
    writeFully(array.asByteArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeFully(array: UShortArray, offset: Int = 0, length: Int = array.size - offset) {
    writeFully(array.asShortArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeFully(array: UIntArray, offset: Int = 0, length: Int = array.size - offset) {
    writeFully(array.asIntArray(), offset, length)
}

@ExperimentalUnsignedTypes
public inline fun DROP_Output.writeFully(array: ULongArray, offset: Int = 0, length: Int = array.size - offset) {
    writeFully(array.asLongArray(), offset, length)
}

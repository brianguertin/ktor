package io.ktor.utils.io.core

import kotlinx.cinterop.*
import platform.posix.*

internal actual fun DROP_Buffer.discardUntilDelimiterImpl(delimiter: Byte): Int {
    val content = content
    var idx = readPosition
    val end = writePosition

    while (idx < end) {
        if (content[idx] == delimiter) break
        idx++
    }

    val start = readPosition
    discardUntilIndex(idx)
    return idx - start
}

internal actual fun DROP_Buffer.discardUntilDelimitersImpl(delimiter1: Byte, delimiter2: Byte): Int {
    val content = content
    var idx = readPosition
    val end = writePosition

    while (idx < end) {
        val v = content[idx]
        if (v == delimiter1 || v == delimiter2) break
        idx++
    }

    val start = readPosition
    discardUntilIndex(idx)
    return idx - start
}

internal actual fun DROP_Buffer.readUntilDelimiterImpl(
    delimiter: Byte,
    dst: ByteArray,
    offset: Int,
    length: Int
): Int {
    check(offset >= 0)
    check(length >= 0)
    check(offset + length <= dst.size)

    return readUntilImpl({ it == delimiter }, dst, offset, length)
}

internal actual fun DROP_Buffer.readUntilDelimitersImpl(
    delimiter1: Byte,
    delimiter2: Byte,
    dst: ByteArray,
    offset: Int,
    length: Int
): Int {
    check(offset >= 0)
    check(length >= 0)
    check(offset + length <= dst.size)
    check(delimiter1 != delimiter2)

    return readUntilImpl({ it == delimiter1 || it == delimiter2 }, dst, offset, length)
}

internal actual fun DROP_Buffer.readUntilDelimiterImpl(delimiter: Byte, dst: DROP_Output): Int {
    return readUntilImpl({ it == delimiter }, dst)
}

internal actual fun DROP_Buffer.readUntilDelimitersImpl(delimiter1: Byte, delimiter2: Byte, dst: DROP_Output): Int {
    check(delimiter1 != delimiter2)

    return readUntilImpl({ it == delimiter1 || it == delimiter2 }, dst)
}

@OptIn(UnsafeNumber::class)
private inline fun DROP_Buffer.readUntilImpl(
    predicate: (Byte) -> Boolean,
    dst: ByteArray,
    offset: Int,
    length: Int
): Int {
    if (length == 0) return 0

    val content = content
    val start = readPosition
    var i = start
    val end = i + minOf(length, readRemaining)

    while (i < end) {
        if (predicate(content[i])) break
        i++
    }

    val copied = i - start
    dst.usePinned { pinned ->
        val dstPointer = pinned.addressOf(offset)
        val srcPointer = (content + start)!!
        memcpy(dstPointer, srcPointer, copied.convert<size_t>())
    }

    discardUntilIndex(i)

    return copied
}

@OptIn(UnsafeNumber::class)
private inline fun DROP_Buffer.readUntilImpl(
    predicate: (Byte) -> Boolean,
    dst: DROP_Output
): Int {
    val content = content
    var i = readPosition
    var copiedTotal = 0

    dst.writeWhile { chunk ->
        val start = i
        val end = minOf(i + chunk.writeRemaining, writePosition)

        while (i < end) {
            if (predicate(content[i])) break
            i++
        }

        val size = i - start

        val dstPointer = (chunk.content + chunk.writePosition)!!
        val srcPointer = (content + start)!!
        memcpy(dstPointer, srcPointer, size.convert<size_t>())
        chunk.commitWritten(size)
        copiedTotal += size

        chunk.writeRemaining == 0 && i < end
    }

    discardUntilIndex(i)
    return copiedTotal
}

internal inline val DROP_Buffer.content: CPointer<ByteVar> get() = memory.pointer

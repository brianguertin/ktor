/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public val ReadableBuffer.availableForRead: Int get() = writeIndex - readIndex
public val ReadableBuffer.isEmpty: Boolean get() = availableForRead == 0
public val ReadableBuffer.isNotEmpty: Boolean get() = !isEmpty

/**
 * Check if the Buffer has [count] bytes to read.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForRead].
 */
internal fun ReadableBuffer.ensureCanRead(count: Int) {
    if (availableForRead < count) {
        throw EOFException("Can't read $count bytes. Available: $availableForRead.")
    }
}

internal fun ReadableBuffer.ensureCanRead(index: Int, count: Int) {
    if (index + count > capacity) {
        throw EOFException("Can't read $count bytes at index $index. Capacity: $capacity.")
    }
}

public fun ReadableBuffer.discard(count: Int) {
    require(count in 0..availableForRead) { "Can't discard $count bytes, available $availableForRead" }
    readIndex += count
}

internal fun ReadableBuffer.readStringUntilNewLine(): String {
    val oldIndex = readIndex
    val string = readString()
    val newLine = string.indexOf('\n')
    if (newLine == -1) {
        return string
    }

    val endIndex = if (newLine > 0 && string[newLine - 1] == '\r') newLine - 1 else newLine
    val bytesLength = string.lengthInUtf8Bytes(newLine + 1)
    readIndex = oldIndex + bytesLength
    return string.substring(0, endIndex)
}

internal fun String.lengthInUtf8Bytes(endIndex: Int): Int {
    var index = 0
    var result = 0
    while (index < endIndex) {
        val c = this[index++].code
        result += when {
            c < 0x80 -> 1
            c < 0x800 -> 2
            c < 0xd800 || c > 0xdfff -> 3
            else -> 4
        }
    }

    return result
}

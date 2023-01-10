/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public val Buffer.isFull: Boolean get() = availableForWrite == 0
public val Buffer.isNotFull: Boolean get() = !isFull
public val Buffer.availableForWrite: Int get() = capacity - writeIndex

/**
 * Find [other] buffer prefix in the current buffer.
 *
 * @return index inside current buffer or -1 if not found.
 */
internal fun Buffer.indexOfPrefix(other: Buffer): Int {
    for (index in readIndex until writeIndex) {
        var matchedLength = 0
        while (matchedLength < other.availableForRead && index + matchedLength < writeIndex) {
            if (getByteAt(index + matchedLength) != other.getByteAt(other.readIndex + matchedLength)) {
                break
            }
            matchedLength++
        }

        if (index + matchedLength == writeIndex || matchedLength == other.availableForRead) {
            return index
        }
    }

    return -1
}

/**
 * Find common prefix length between this buffer and [other] buffer.
 */
internal fun Buffer.commonPrefixLength(other: Buffer): Int {
    val minSize = minOf(availableForRead, other.availableForRead)
    var index = 0
    while (index < minSize) {
        if (getByteAt(readIndex + index) != other.getByteAt(other.readIndex + index)) break
        index++
    }
    return index
}

/**
 * Check if the Buffer has space to write [count] bytes.
 *
 * @throws IndexOutOfBoundsException if the [count] is greater [availableForWrite].
 */
internal fun Buffer.ensureCanWrite(count: Int) {
    if (availableForWrite < count) {
        throw IndexOutOfBoundsException("Can't write $count bytes. Available space: $availableForWrite.")
    }
}

internal fun Buffer.ensureCanWrite(index: Int, count: Int) {
    if (index + count > capacity) {
        throw IndexOutOfBoundsException("Can't write $count bytes at index $index. Capacity: $capacity.")
    }
}

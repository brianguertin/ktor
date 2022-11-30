/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.errors.*

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

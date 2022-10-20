/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public val Buffer.isFull: Boolean get() = availableForWrite == 0
public val Buffer.isNotFull: Boolean get() = !isFull
public val Buffer.availableForWrite: Int get() = capacity - writeIndex

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

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.charsets.*

public actual fun String(
    bytes: ByteArray,
    offset: Int,
    length: Int,
    charset: Charset
): String {
    if (charset == Charsets.UTF_8) {
        return bytes.decodeToString(offset, length)
    }

    error("Unsupported charset: $charset")
}

public actual fun String.toByteArray(
    charset: Charset
): ByteArray {
    if (charset == Charsets.UTF_8) {
        return encodeToByteArray()
    }

    error("Unsupported charset: $charset")
}

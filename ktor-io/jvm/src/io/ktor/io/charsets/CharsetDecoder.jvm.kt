/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*

public actual typealias CharsetDecoder = java.nio.charset.CharsetDecoder

public actual val CharsetDecoder.charset: Charset get() = charset()!!

internal actual fun CharsetDecoder.decodeBufferTo(
    input: ReadableBuffer,
    out: Appendable,
    lastBuffer: Boolean,
): Int {
    TODO()
}

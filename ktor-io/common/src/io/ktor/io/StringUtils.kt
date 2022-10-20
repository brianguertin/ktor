/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.charsets.*

public expect fun String(
    bytes: ByteArray,
    offset: Int = 0,
    length: Int = bytes.size - offset,
    charset: Charset = Charsets.UTF_8
): String

public expect fun String.toByteArray(
    charset: Charset = Charsets.UTF_8
): ByteArray

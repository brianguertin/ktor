/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public actual open class IOException actual constructor(
    message: String,
    cause: Throwable?
) : Exception(message, cause) {
    public actual constructor(message: String) : this(message, null)
}

public actual open class EOFException actual constructor(message: String) : IOException(message)

/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

import io.ktor.io.*
import java.nio.*
import java.nio.charset.*

@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS")
public actual typealias Charset = java.nio.charset.Charset

private fun CoderResult.throwExceptionWrapped() {
    try {
        throwException()
    } catch (original: java.nio.charset.MalformedInputException) {
        throw MalformedInputException(original.message ?: "Failed to decode bytes")
    }
}

@Suppress("ACTUAL_WITHOUT_EXPECT")
public actual open class MalformedInputException actual constructor(
    message: String
) : java.nio.charset.MalformedInputException(0) {
    private val _message = message

    override val message: String?
        get() = _message
}

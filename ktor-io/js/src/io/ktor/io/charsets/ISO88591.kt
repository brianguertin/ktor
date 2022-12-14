/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

private fun failedToMapError(ch: Int): Nothing {
    throw MalformedInputException("The character with unicode point $ch couldn't be mapped to ISO-8859-1 character")
}

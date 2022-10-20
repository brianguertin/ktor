package io.ktor.utils.io.charsets

import io.ktor.io.*
import io.ktor.utils.io.core.*
import org.khronos.webgl.*

private fun failedToMapError(ch: Int): Nothing {
    throw MalformedInputException("The character with unicode point $ch couldn't be mapped to ISO-8859-1 character")
}

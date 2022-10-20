package io.ktor.utils.io

internal fun rethrowClosed(cause: Throwable): Nothing {
    val clone = try {
        tryCopyException(cause, cause)
    } catch (_: Throwable) {
        null
    }

    throw clone ?: cause
}

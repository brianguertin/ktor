package io.ktor.utils.io

import io.ktor.io.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

@Deprecated(
    "",
    ReplaceWith("ByteReadChannel")
)
public typealias ReaderScope = ByteReadChannel

@Deprecated(
    "Use ByteReadChannel instead",
    ReplaceWith("this"),
    level = DeprecationLevel.WARNING
)
@Suppress("DEPRECATION")
public val ReaderScope.channel: ByteReadChannel get() = this

@Deprecated(
    "Use ByteReadChannel instead",
    ReplaceWith("ByteWriteChannel"),
    level = DeprecationLevel.WARNING
)
public typealias WriterScope = ByteWriteChannel

@Deprecated(
    "Use ByteWriteChannel instead",
    ReplaceWith("this"),
    level = DeprecationLevel.WARNING
)
@Suppress("DEPRECATION")
public val WriterScope.channel: ByteWriteChannel get() = this

public fun CoroutineScope.reader(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend ByteReadChannel.() -> Unit
): ByteWriteChannel {
    val result = ConflatedByteChannel()

    launch(coroutineContext) {
        result.block()
    }

    return result
}

public fun CoroutineScope.writer(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend ByteWriteChannel.() -> Unit
): ByteReadChannel {
    val result = ConflatedByteChannel()

    launch(coroutineContext) {
        try {
            result.block()
        } finally {
            result.close()
        }
    }

    return result
}

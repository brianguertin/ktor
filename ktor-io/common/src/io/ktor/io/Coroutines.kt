package io.ktor.io

import kotlinx.coroutines.*
import kotlin.coroutines.*

public fun CoroutineScope.reader(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend ByteReadChannel.() -> Unit
): ByteWriteChannel {
    val result = ConflatedByteChannel()

    launch(coroutineContext) {
        try {
            result.block()
        } catch (cause: Throwable) {
            result.cancel(cause)
        }
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
        } catch (cause: Throwable) {
            result.cancel(cause)
        } finally {
            result.close()
        }
    }

    return result
}

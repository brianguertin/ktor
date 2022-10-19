@file:Suppress("KDocMissingDocumentation")

package io.ktor.utils.io.core.internal

import io.ktor.utils.io.core.*
import kotlin.jvm.*
import kotlin.native.concurrent.*

/**
 * API marked with this annotation is internal and extremely fragile and not intended to be used by library users.
 * Such API could be changed without notice including rename, removal and behaviour change.
 * Also using API marked with this annotation could cause data loss or any other damage.
 */
@Suppress("DEPRECATION")
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class DangerousInternalIoApi

internal fun DROP_ByteReadPacket.unsafeAppend(builder: DROP_BytePacketBuilder): Int {
    val builderSize = builder.size
    val builderHead = builder.stealAll() ?: return 0

    if (builderSize <= PACKET_MAX_COPY_SIZE && builderHead.next == null && tryWriteAppend(builderHead)) {
        builder.afterBytesStolen()
        return builderSize
    }

    append(builderHead)
    return builderSize
}

@PublishedApi
internal fun DROP_Input.prepareReadFirstHead(minSize: Int): DROP_ChunkBuffer? = prepareReadHead(minSize)

@PublishedApi
internal fun DROP_Input.completeReadHead(current: DROP_ChunkBuffer) {
    when {
        current === this -> return
        !current.canRead() -> ensureNext(current)
        current.endGap < DROP_Buffer.ReservedSize -> fixGapAfterRead(current)
        else -> headPosition = current.readPosition
    }
}

@PublishedApi
internal fun DROP_Input.prepareReadNextHead(current: DROP_ChunkBuffer): DROP_ChunkBuffer? {
    if (current === this) {
        return if (canRead()) this else null
    }

    return ensureNextHead(current)
}

internal fun DROP_Output.prepareWriteHead(capacity: Int, current: DROP_ChunkBuffer?): DROP_ChunkBuffer {
    if (current != null) {
        afterHeadWrite()
    }
    return prepareWriteHead(capacity)
}

@JvmField

internal val EmptyByteArray = ByteArray(0)

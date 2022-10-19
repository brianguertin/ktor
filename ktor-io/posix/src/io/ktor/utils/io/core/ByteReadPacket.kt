@file:Suppress("FunctionName")

package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import kotlinx.cinterop.*

public actual fun ByteReadPacket(
    array: ByteArray,
    offset: Int,
    length: Int,
    block: (ByteArray) -> Unit
): DROP_ByteReadPacket {
    if (length == 0) {
        block(array)
        return DROP_ByteReadPacket.Empty
    }

    val pool = object : SingleInstancePool<DROP_ChunkBuffer>() {
        private var pinned: Pinned<*>? = null

        override fun produceInstance(): DROP_ChunkBuffer {
            check(pinned == null) { "This implementation can pin only once." }

            val content = array.pin()
            val base = content.addressOf(offset)
            pinned = content

            return DROP_ChunkBuffer(DROP_Memory.of(base, length), null, this)
        }

        override fun disposeInstance(instance: DROP_ChunkBuffer) {
            check(pinned != null) { "The array hasn't been pinned yet" }
            block(array)
            pinned?.unpin()
            pinned = null
        }
    }

    return DROP_ByteReadPacket(pool.borrow().apply { resetForRead() }, pool)
}

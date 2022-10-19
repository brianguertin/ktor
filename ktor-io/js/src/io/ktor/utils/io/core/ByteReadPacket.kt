@file:Suppress("FunctionName")

package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import org.khronos.webgl.*

public actual fun ByteReadPacket(
    array: ByteArray,
    offset: Int,
    length: Int,
    block: (ByteArray) -> Unit
): DROP_ByteReadPacket {
    val content = array.asDynamic() as Int8Array
    val sub = when {
        offset == 0 && length == array.size -> content.buffer
        else -> content.buffer.slice(offset, offset + length)
    }

    val pool = object : SingleInstancePool<DROP_ChunkBuffer>() {
        override fun produceInstance(): DROP_ChunkBuffer =
            DROP_ChunkBuffer(DROP_Memory.of(sub), null, this)

        override fun disposeInstance(instance: DROP_ChunkBuffer) {
            block(array)
        }
    }

    return DROP_ByteReadPacket(pool.borrow().apply { resetForRead() }, pool)
}

@file:Suppress("RedundantModalityModifier")

package io.ktor.utils.io.core

import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.errors.*
import io.ktor.utils.io.pool.*

public expect class EOFException(message: String) : IOException

/**
 * For streaming input it should be [DROP_Input.endOfInput] instead.
 */
@Deprecated("Use endOfInput property instead", ReplaceWith("endOfInput"))
public inline val DROP_Input.isEmpty: Boolean
    get() = endOfInput

/**
 * For streaming input there is no reliable way to detect it without triggering bytes population from the underlying
 * source. Consider using [DROP_Input.endOfInput] or use [DROP_ByteReadPacket] instead.
 */
@Deprecated(
    "This makes no sense for streaming inputs. Some use-cases are covered by endOfInput property",
    ReplaceWith("!endOfInput")
)
public val DROP_Input.isNotEmpty: Boolean
    get() {
        if (endOfInput) return false
        prepareReadFirstHead(1)?.let { found ->
            completeReadHead(found)
            return true
        }
        return false
    }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public inline val DROP_ByteReadPacket.isEmpty: Boolean
    get() = endOfInput

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public inline val DROP_ByteReadPacket.isNotEmpty: Boolean
    get() = !endOfInput

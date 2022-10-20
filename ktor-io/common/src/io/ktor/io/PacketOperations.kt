/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

public fun Packet(array: ByteArray): Packet = Packet().apply {
    writeByteArray(array)
}

public fun Packet(value: String): Packet = Packet().apply {
    writeString(value)
}

public val Packet.isEmpty: Boolean get() = availableForRead == 0

public val Packet.isNotEmpty: Boolean get() = availableForRead > 0

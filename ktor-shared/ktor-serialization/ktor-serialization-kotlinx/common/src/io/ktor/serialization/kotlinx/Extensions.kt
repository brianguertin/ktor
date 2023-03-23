/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.serialization.kotlinx

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.io.*
import io.ktor.io.charsets.*
import io.ktor.util.reflect.*
import kotlinx.serialization.*

internal expect val providers: List<KotlinxSerializationExtensionProvider>

internal fun extensions(format: SerialFormat): List<KotlinxSerializationExtension> =
    providers.mapNotNull { it.extension(format) }

/**
 * A factory for [KotlinxSerializationExtension]
 */
public interface KotlinxSerializationExtensionProvider {
    public fun extension(format: SerialFormat): KotlinxSerializationExtension?
}

/**
 * An extension for [KotlinxSerializationConverter] that can add format-specific logic
 */
public interface KotlinxSerializationExtension {

    public suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent?

    public suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any?
}

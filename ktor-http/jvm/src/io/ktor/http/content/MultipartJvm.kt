/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.http.content

import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.*

/**
 * Provides file item's content as an [InputStream]
 */
public val PartData.FileItem.streamProvider: () -> InputStream get() = { provider().toInputStream() }


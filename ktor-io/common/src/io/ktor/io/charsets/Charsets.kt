/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

// ----------------------------- REGISTRY ------------------------------------------------------------------------------
public expect object Charsets {
    public val UTF_8: Charset
    public val ISO_8859_1: Charset
}

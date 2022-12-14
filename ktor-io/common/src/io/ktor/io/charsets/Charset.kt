/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io.charsets

public expect abstract class Charset {
    public fun name(): String

    public abstract fun newEncoder(): CharsetEncoder
    public abstract fun newDecoder(): CharsetDecoder

    public companion object {
        public fun forName(name: String): Charset
        public fun isSupported(charset: String): Boolean
    }
}

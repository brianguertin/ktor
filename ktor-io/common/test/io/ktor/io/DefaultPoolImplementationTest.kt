/*
 * Copyright 2014-2022 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.io

import io.ktor.utils.io.pool.*
import kotlin.test.*

class DefaultPoolImplementationTest {
    @Test
    fun instantiateTest() {
        assertEquals(1, Impl().borrow())
    }

    private class Impl : DefaultPool<Int>(10) {
        override fun produceInstance() = 1
    }
}

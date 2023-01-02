package io.ktor.io

import io.ktor.io.charsets.*
import kotlin.test.*

class CharsetsTest {
    @Test
    fun testNonExisting() {
        assertFailsWith<IllegalArgumentException> {
            Charset.forName("abracadabra-encoding")
        }
    }

    @Test
    fun testIllegal() {
        assertFailsWith<IllegalArgumentException> {
            Charset.forName("%s")
        }
    }
}

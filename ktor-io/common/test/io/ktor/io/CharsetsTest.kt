package io.ktor.io

import io.ktor.utils.io.charsets.*
import kotlin.test.*

class CharsetsTest {
    @Test
    fun testUtf8() {
        Charset.forName("UTF-8").newEncoder().encode("test").close()
    }

    @Test
    fun testISO() {
        Charset.forName("UTF-8").newEncoder().encode("test").close()
    }

    @Test
    fun testLatin1() {
        Charset.forName("Latin1").newEncoder().encode("test").close()
    }

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

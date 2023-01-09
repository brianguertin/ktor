package io.ktor.io

import io.ktor.test.dispatcher.*
import kotlin.test.*

class StringReaderTest {

    @Test
    fun testReadEmptyString() = testSuspend {
        ByteReadChannel("\n").stringReader { reader ->
            assertEquals("", reader.readLine())
        }
    }

    @Test
    fun testReadEmptyStringWithLimit() = testSuspend {
        ByteReadChannel("\n").stringReader { reader ->
            assertEquals("", reader.readLine(limit = 0))
        }
    }

    @Test
    fun testLineWithoutNewLine() = testSuspend {
        ByteReadChannel("abc").stringReader { reader ->
            assertEquals("abc", reader.readLine())
        }
    }

    @Test
    fun testLineWithoutNewLineWithLimit() = testSuspend {
        ByteReadChannel("abc").stringReader { reader ->
            assertEquals("abc", reader.readLine(limit = 3))
        }
    }

    @Test
    fun testCaretIsNotIncluded() = testSuspend {
        ByteReadChannel("abc\r\n").stringReader { reader ->
            assertEquals("abc", reader.readLine())
        }
    }

    @Test
    fun testCaretIsNotIncludedWithLimit() = testSuspend {
        ByteReadChannel("abc\r\n").stringReader { reader ->
            assertEquals("abc", reader.readLine(limit = 3))
        }
    }

    @Test
    fun testRead2Lines() = testSuspend {
        ByteReadChannel("abc\r\ndef\r\n").stringReader { reader ->
            assertEquals("abc", reader.readLine())
            assertEquals("def", reader.readLine())
        }
    }

    @Test
    fun testRead2LinesWithLimit() = testSuspend {
        ByteReadChannel("abc\r\ndef\r\n").stringReader { reader ->
            assertEquals("abc", reader.readLine(limit = 1024))
            assertEquals("def", reader.readLine(limit = 1024))
        }
    }

    @Test
    fun testReadByteAndLine() = testSuspend {
        ByteReadChannel("abc\n").stringReader { reader ->
            assertEquals('a'.code.toByte(), reader.readByte())
            assertEquals("bc", reader.readLine())
        }
    }

    @Test
    fun testReadPacketWorksAfterReadLine() = testSuspend {
        ByteReadChannel("abc\r\ndef\r\n").stringReader { reader ->
            assertEquals("abc", reader.readLine())
            assertEquals(5, reader.availableForRead)
            assertEquals('d'.code.toByte(), reader.readByte())
            assertEquals('e'.code.toByte(), reader.readByte())
            assertEquals('f'.code.toByte(), reader.readByte())
            assertEquals('\r'.code.toByte(), reader.readByte())
            assertEquals('\n'.code.toByte(), reader.readByte())
        }
    }

    @Test
    fun testReadLineReadByteReadLine() = testSuspend {
        ByteReadChannel("abc\r\ndef\r\n").stringReader { reader ->
            assertEquals("abc", reader.readLine())
            assertEquals('d'.code.toByte(), reader.readByte())
            assertEquals("ef", reader.readLine())
        }
    }
}

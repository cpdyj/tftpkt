package space.iseki.tftpkt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OptionTest {
    @Test
    fun testParser() {
        val s = "foo\u0000bar\u0000"
        val bs = s.encodeToByteArray()
        assertEquals(
            Option("foo".encodeToByteArray(), "foo", "bar") to 8,
            Option.readOption(bs, bs.indices)
        )
    }

    @Test
    fun test2() {
        val a = "[116, 105, 109, 101, 111, 117, 116, 0, 52, 53, 0]".let(::str2Bytes)
        assertEquals(
            Option("timeout".encodeToByteArray(), "timeout", "45") to 11,
            Option.readOption(a, a.indices)
        )
    }
}

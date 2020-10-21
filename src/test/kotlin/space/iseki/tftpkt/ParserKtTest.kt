package space.iseki.tftpkt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ParserKtTest {

    @Test
    fun testReadCStyleString() {
        val s = "aaa\u0000bbb"
        assertEquals("aaa" to 4, readCStyleString(s.encodeToByteArray()))
        assertNull(readCStyleString("vvv".encodeToByteArray()))
    }

    @Test
    fun testCreateOACL() {
        val list = listOf(Option(byteArrayOf(1, 2, 3, 4), "USELESS", "AB"))
        val target = byteArrayOf(0, 6, 1, 2, 3, 4, 0, 'A'.toByte(), 'B'.toByte(), 0)
        assertEquals(target.toList(), createOACK(list).toList())
    }

    @Test
    fun testCStringEncoder() {
        repeat(100) {
            val s = buildString {
                (1..100).map { (Math.random() * 65535).toChar() }.forEach(::append)
            }
            val a = processCString(s)
            check(a.indexOf(0) == a.lastIndex) { "detected unexpected zero at ${a.indexOf(0)} in ${a.toList()}" }
        }
    }

    @Test
    fun testCreateErrorPacket() {
        val e = ClientError("test")
        val packet = "0,5,0,0,116, 101, 115, 116,0".let(::str2Bytes).toList()
        assertEquals(packet, e.createErrorPacket().toList())
    }
}

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
}

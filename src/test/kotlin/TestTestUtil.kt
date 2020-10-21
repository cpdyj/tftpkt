import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TestTestUtil {

    @Test
    fun testStr2Bytes() {
        val a = byteArrayOf(0, 1, 2, -5, 192.toByte())
        Assertions.assertEquals(a.toList(), str2Bytes(a.toList().toString()).toList())
    }
}

internal fun str2Bytes(s: String) =
    s.trim().removePrefix("[").removeSuffix("]")
        .split(",").map { it.trim().toInt().toByte() }.toByteArray()

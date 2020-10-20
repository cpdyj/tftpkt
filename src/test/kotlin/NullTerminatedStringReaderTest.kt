import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NullTerminatedStringReaderTest {

    @Test
    fun test() {
        val list = listOf("aaa", "", "bb", " 10 ")
        val b = list.joinToString(separator = "\u0000", postfix = "\u0000").encodeToByteArray()
        assertEquals(0, b[b.lastIndex])
        val r = NullTerminatedStringReader(b, b.indices)
        list.forEach {
            assertEquals(it, r.nextString())
        }
        assertEquals(null, r.nextString())
    }
}

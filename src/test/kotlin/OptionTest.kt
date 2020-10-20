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
}

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AsyncResultTest {

    @Test
    fun test() {
        val ar = AsyncResult<Int>()
        Thread { ar.submit(15) }.start()
        assertEquals(15, ar.await())
        assertThrows<RuntimeException> { ar.fail(RuntimeException("")) }
        assertThrows<RuntimeException> { ar.submit(1) }
    }

    @Test
    fun testFail() {
        val ar = AsyncResult<Int>()
        val re = RuntimeException("awsl")
        Thread { ar.tryFail(re) }.start()
        Thread { ar.tryFail(re) }.start()
        assertThrows<RuntimeException> { ar.await() }
        assertFalse(ar.succeed)
        assertEquals(re, ar.cause)
    }
}

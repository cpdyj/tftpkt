package space.iseki.tftpkt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ReadRequestTest {


    @Test
    fun test1() {
        val p1 = "[0, 1, 113, 0, 111, 99, 116, 101, 116, 0]"
        val readReq = parseRWQ(str2Bytes(p1)) as ReadRequest
        assertEquals("q", readReq.filename)
        assertFalse(readReq.needOACK())
    }

    @Test
    fun test2() {
        val p = "[0, 1, 113, 0, 111, 99, 116, 101, 116]"
        assertThrows<ClientError> { parseRWQ(str2Bytes(p)) as ReadRequest }
    }

    @Test
    fun test3() {
        val p =
            "[0, 1, 113, 0, 111, 99, 116, 101, 116, 0, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0]".let(::str2Bytes)
        val rr = parseRWQ(p) as ReadRequest
        assertEquals("q", rr.filename)
        assertEquals(1, rr.timeout)
        assertTrue(rr.needOACK())
        val pack = "[0, 6, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0]".let(::str2Bytes)
        val ack = rr.generateOACK(null)
        assertEquals(pack.toList(), ack.toList())
    }

    @Test
    fun test4() {
        val p =
            "[0, 1, 113, 0, 111, 99, 116, 101, 116, 0, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0, 116, 115, 105, 122, 101, 0, 48, 0]".let(
                ::str2Bytes)
        val rr = parseRWQ(p) as ReadRequest
        assertEquals("q", rr.filename)
        assertEquals(1, rr.timeout)
        assertTrue(rr.needOACK())
        val pack =
            "[0, 6, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0, 116, 115, 105, 122, 101, 0, 49, 50, 56, 0]".let(::str2Bytes)
        val ack = rr.generateOACK(128)
        assertEquals(pack.toList(), ack.toList())
        val pack2 = "[0, 6, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0]".let(::str2Bytes)
        val ack2 = rr.generateOACK(null)
        assertEquals(pack2.toList(), ack2.toList())
    }
}

internal class WriteRequestTest {

    @Test
    fun test1() {
        val p =
            "[0, 2, 46, 112, 103, 65, 100, 109, 105, 110, 52, 46, 115, 116, 97, 114, 116, 117, 112, 46, 108, 111, 103, 0, 111, 99, 116, 101, 116, 0]"
                .let(::str2Bytes)
        val wr = parseRWQ(p) as WriteRequest
        assertEquals(".pgAdmin4.startup.log", wr.filename)
        assertFalse(wr.needOACK())
    }

    @Test
    fun test3() {
        val p = "[0, 3, 113, 0, 111, 99, 116, 101, 116 ,0]"
        assertThrows<ClientError> { parseRWQ(str2Bytes(p)) }
    }

    @Test
    fun test2() {
        val p = "[0, 2, 113, 0, 111, 99, 116, 101, 116]"
        assertThrows<ClientError> { parseRWQ(str2Bytes(p)) as WriteRequest }
    }

    @Test
    fun test4() {
        val p =
            "[0, 2, 113, 0, 111, 99, 116, 101, 116, 0, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0, 116, 115, 105, 122, 101, 0, 48, 0]".let(
                ::str2Bytes)
        val rr = parseRWQ(p) as WriteRequest
        assertEquals("q", rr.filename)
        assertEquals(1, rr.timeout)
        assertTrue(rr.needOACK())
        val pack =
            "[0, 6, 116, 105, 109, 101, 111, 117, 116, 0, 49, 0, 116, 115, 105, 122, 101, 0,48, 0]".let(::str2Bytes)
        val ack = rr.generateOACK()
        assertEquals(pack.toList(), ack.toList())
    }

}

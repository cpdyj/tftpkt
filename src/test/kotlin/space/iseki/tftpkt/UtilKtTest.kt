package space.iseki.tftpkt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UtilKtTest {

    @Test
    fun test1() {
        assertThrows<IllegalStateException> { openUDPChannelRandomPort(-1..1) }
        assertThrows<IllegalStateException> { openUDPChannelRandomPort(64..128, 0) }
        assertThrows<IllegalStateException> { openUDPChannelRandomPort(2..1) }
        assertThrows<Throwable> { assertThrows<Exception> { } }
        val ch = openUDPChannelRandomPort()
        ch!!.close()
    }
}

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

class TFTPServer {
    private val scope = CoroutineScope(Job())
    private val looper = Looper2()

    fun begin() {
        val sc = DatagramChannel.open()
        sc.bind(InetSocketAddress(69))
        val server = UDPSocket(sc, looper)
        scope.launch {
            while (isActive) {
                val (sa, packet) = server.readPacket()
                val req = parseRWQ(packet)
            }
        }.invokeOnCompletion {
            close()
        }
    }

    fun close() {
        looper.close()
    }
}



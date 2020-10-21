package space.iseki.tftpkt

import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.SocketAddress
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
                handleRequest(scope, packet, sa)
            }
        }.invokeOnCompletion {
            close()
        }
    }

    fun close() {
        looper.close()
        scope.cancel()
    }
}


internal fun handleRequest(scope: CoroutineScope, packet: ByteArray, sa: SocketAddress) = scope.launch {

}


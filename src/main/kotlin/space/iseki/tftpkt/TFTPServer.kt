package space.iseki.tftpkt

import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class TFTPServer {
    internal val scope = CoroutineScope(SupervisorJob())
    private val looper = Looper()
    private val fileProvider = defaultProvider

    fun begin() {
        val sc = DatagramChannel.open()
        sc.bind(InetSocketAddress(69))
        val server = UDPSocket(sc, looper)
        scope.launch {
            while (isActive) {
                val (sa, packet) = server.readPacket()
                println(packet.toList())
                async {
                    try {
                        TFTPConnection(this@TFTPServer, fileProvider, looper, sa, packet).begin()
                    } catch (ce: ClientError) {
                        sc.send(ByteBuffer.wrap(ce.createErrorPacket()), sa)
                    } catch (e: Exception) {
                        sc.send(ByteBuffer.wrap(ClientError("server fail").createErrorPacket()), sa)
                        e.printStackTrace()
                    }
                }
            }
        }.invokeOnCompletion {
            it?.printStackTrace()
            close()
        }
    }

    fun close() {
        looper.close()
        scope.cancel()
    }
}


internal class TFTPConnection(
    val server: TFTPServer,
    val fileProvider: FileProvider,
    val looper: Looper,
    val sa: SocketAddress,
    val packet: ByteArray,
) {
    private val req = parseRWQ(packet)
    private val ch = openUDPChannelRandomPort() ?: cerror("open port fail")
    private val socket = UDPSocket(ch, looper, sa)


    fun begin() {
        server.scope.launch {
            when (req) {
                is ReadRequest -> handleRead(req)
                is WriteRequest -> handleWrite(req)
                else -> error("impossible")
            }
        }
    }

    private suspend fun handleRead(req: ReadRequest) {
        val fn = req.filename
        val f = fileProvider.get(fn)
        val timeout = (req.timeout ?: 4) * 1000L
        val blockSize = req.blockSize ?: 512
        var ackId = 0
        if (req.needOACK()) {
            val oack = req.generateOACK(f.size)
            // TODO: refactor withRetry
            for (i in 1..3) {
                withTimeoutOrNull(timeMillis = timeout) {
                    socket.writePacket(oack)
                    val p = socket.readPacket().second
                    ccheck(p[1] == b(4)) { "invalid ACK" }
                    ackId = p.int(2 size 2)
                    TODO()
                }

            }

        }
    }

    private suspend fun handleWrite(req: WriteRequest) {
        cerror("todo")
    }
}

interface FileProvider {

    suspend fun put(name: String): TFTPFile

    suspend fun get(name: String): TFTPFile
}

interface TFTPFile {
    val size: Int?
}

object defaultProvider : FileProvider {
    override suspend fun put(name: String): TFTPFile {
        TODO("Not yet implemented")
    }

    override suspend fun get(name: String): TFTPFile {
        TODO("Not yet implemented")
    }
}


import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class UDPSocket(val ch: DatagramChannel, val looper2: Looper2) {
    init {
        ch.configureBlocking(false)
    }

    private val locker = ReentrantLock()
    private val readBlockList = LinkedList<CancellableContinuation<Unit>>()
    private val recvList = LinkedList<Pair<SocketAddress, ByteArray>>()
    private val buf = ByteBuffer.allocate(1024)
    val handler: (Int) -> Unit = {
        println("handler")
        val l = LinkedList<CancellableContinuation<Unit>>()
        locker.withLock {
            while (true) {
                val sa = ch.receive(buf) ?: break
                buf.flip()
                val arr = ByteArray(buf.limit())
                buf.get(arr)
                recvList.push(sa to arr)
                buf.clear()
            }
            l.addAll(readBlockList)
            readBlockList.clear()
        }
        l.forEach { it.resume(Unit, null) }
    }
    val sk = looper2.addChannel(ch, handler).await()

    private val mutex = Mutex()

    suspend fun readPacket(): Pair<SocketAddress, ByteArray> {
        mutex.withLock {
            while (true) {
                locker.withLock {
                    if (recvList.isNotEmpty()) {
                        return recvList.pop()
                    }
                }
                suspendCancellableCoroutine<Unit> { cont ->
                    locker.withLock {
                        if (recvList.isEmpty()) {
                            readBlockList.push(cont)
                            return@suspendCancellableCoroutine
                        }
                    }
                    cont.resume(Unit, null)
                }
            }
        }
    }


}





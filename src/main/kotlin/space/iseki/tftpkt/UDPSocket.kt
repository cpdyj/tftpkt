package space.iseki.tftpkt

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
class UDPSocket(val ch: DatagramChannel, val looper: Looper, val sa: SocketAddress? = null) {
    init {
        ch.configureBlocking(false)
    }

    private val locker = ReentrantLock()
    private val readBlockList = LinkedList<CancellableContinuation<Unit>>()
    private val recvList = LinkedList<Pair<SocketAddress, ByteArray>>()
    private val rbuf = ByteBuffer.allocate(1024)
    val handler: (Int) -> Unit = {
        println("handler")
        val l = LinkedList<CancellableContinuation<Unit>>()
        locker.withLock {
            while (true) {
                val sa = ch.receive(rbuf) ?: break
                rbuf.flip()
                val arr = ByteArray(rbuf.limit())
                rbuf.get(arr)
                recvList.push(sa to arr)
                rbuf.clear()
            }
            l.addAll(readBlockList)
            readBlockList.clear()
        }
        l.forEach { it.resume(Unit, null) }
    }
    val sk = looper.addChannel(ch, handler).await()

    private val mutex = Mutex()

    suspend fun readPacket(): Pair<SocketAddress, ByteArray> {
        mutex.withLock {
            while (true) {
                locker.withLock {
                    check(sk.isValid)
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

    suspend fun writePacket(
        data: ByteArray,
        sa: SocketAddress = this.sa ?: error("need specific remote SocketAddress"),
    ) {

        ch.send(ByteBuffer.wrap(data), sa)
    }

}





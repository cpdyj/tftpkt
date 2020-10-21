package space.iseki.tftpkt

import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*


const val LOOPER_CLOSED = 1 shl 8

class Looper2 {
    private data class NewChannel(
        val ch: SelectableChannel,
        val handler: (Int) -> Unit,
        val callback: AsyncResult<SelectionKey>,
    )

    private var closed = false
    private val eq = LinkedList<NewChannel>()

    private val selector = Selector.open()
    private val loop = Thread {
        try {
            while (!Thread.interrupted()) {
                synchronized(eq) {
                    while (eq.isNotEmpty()) {
                        val (ch, handler, callback) = eq.peek()
                        val sk = ch.register(selector, SelectionKey.OP_READ)
                        println(sk)
                        sk.attach(handler)
                        callback.submit(sk)
                        eq.pop()
                    }
                }
                selector.select()
                println(selector.selectedKeys())
                selector.selectedKeys().removeIf {
                    val handler = it.attachment() as (Int) -> Unit
                    handler.invoke(it.readyOps())
                    true
                }
            }
        } finally {
            if (selector.isOpen) {
                selector.keys().forEach {
                    runCatching { (it.attachment() as (Int) -> Unit).invoke(LOOPER_CLOSED) }
                        .onFailure { it.printStackTrace() }
                }
                selector.close()
            }
        }
    }.apply { start() }

    fun close() {
        synchronized(eq) {
            closed = true
            loop.interrupt()
            val re = RuntimeException("looper closed")
            eq.forEach {
                runCatching {
                    it.callback.tryFail(re)
                }
            }
            eq.clear()
        }
    }

    fun addChannel(ch: SelectableChannel, handler: (Int) -> Unit): AsyncResult<SelectionKey> {
        val ar = AsyncResult<SelectionKey>()
        val nc = NewChannel(ch, handler, ar)
        synchronized(eq) {
            check(!closed) { "closed" }
            eq.push(nc)
        }
        selector.wakeup()
        return ar
    }
}


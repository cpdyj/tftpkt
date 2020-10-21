package space.iseki.tftpkt

import java.net.BindException
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import kotlin.math.max
import kotlin.math.min

internal fun openUDPChannelRandomPort(portRange: IntRange = 16384..65535, maxRetry: Int = 8): DatagramChannel? {
    check(maxRetry > 0) { "maxRetry < 1" }
    check(portRange.first in 0..65535 && portRange.last in 0..65535) { "portRange invalid: $portRange" }
    val size = portRange.run { last - first + 1 }
    check(size > 0) { "size < 1" }
    val ch = DatagramChannel.open()
    try {
        repeat(maxRetry) {
            try {
                val tp = (Math.random() * size).toInt() + portRange.first
                val p = max(min(portRange.last, tp), portRange.first)
                ch.bind(InetSocketAddress(p))
                return ch
            } catch (e: BindException) {
                // ignore it
            }
        }
    } catch (e: Exception) {
        runCatching { ch.close() }
        throw e
    }
    runCatching { ch.close() }
    return null
}

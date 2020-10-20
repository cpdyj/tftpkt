import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal inline fun b(i: Int) = i.toByte()
internal inline fun ByteArray.int(range: IntRange) = bytes2Int(sliceArray(range))
internal inline fun bytes2Int(bs: ByteArray) =
    bs.fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xff) }

internal inline infix fun Int.size(size: Int) = this until this + size
internal class NullTerminatedStringReader(val bytes: ByteArray, val range: IntRange) {
    init {
        check(range.first in bytes.indices && range.last in bytes.indices)
    }

    var pos = range.first
        private set

    /**
     * @return next byte after reading
     */
    fun nextString(): String? {
        for (i in pos..range.last) {
            if (bytes[i] == CNULL) {
                val s = String(bytes, pos, i - pos)
                pos = i + 1
                return s
            }
        }
        return null
    }
}

/**
 * @return next byte after reading
 */
internal inline fun readCStyleString(bytes: ByteArray, start: Int) = readCStyleString(bytes, start..bytes.lastIndex)

/**
 * @return next byte after reading
 */
internal fun readCStyleString(bytes: ByteArray, range: IntRange = bytes.indices): Pair<String, Int>? {
//    check(range.first in bytes.indices && range.last in bytes.indices)
    for (i in range) {
        if (bytes[i] == CNULL) {
            return String(bytes, range.first, i - range.first) to i + 1
        }
    }
    return null
}

@OptIn(ExperimentalContracts::class)
internal inline fun ccheck(value: Boolean, lazyMessage: () -> Any) {
    contract {
        returns() implies value
    }
    if (!value) {
        val message = lazyMessage()
        throw cerror(message.toString())
    }
}

internal inline fun cerror(s: String): Nothing = throw ClientError(s)
internal class ClientError(val msg: String) : RuntimeException("client error: $msg")

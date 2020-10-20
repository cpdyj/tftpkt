internal const val CNULL: Byte = 0
internal const val OACK_MAGIC_LOW_BYTE: Byte = 6
internal const val RRQ_OPCODE: Byte = 1
internal const val WRQ_OPCODE: Byte = 2

private object defaultServerOption : ServerOption {
    override val enableTimeoutInterval: Boolean = true
    override val timeoutIntervalRange: IntRange = 1..3600
    override val enableTransferSize: Boolean = true
    override val enableBlockSize: Boolean = true
    override val blockSizeRange: IntRange = 1..1024
}

interface ServerOption {
    val enableTimeoutInterval: Boolean
    val timeoutIntervalRange: IntRange
    val enableTransferSize: Boolean
    val enableBlockSize: Boolean
    val blockSizeRange: IntRange
}


internal interface Request

internal class WriteRequest(
    val finename: String,
    val options: Map<String, Option>,
    serverOption: ServerOption,
) : Request {
    private val oto =
        options["timeout"].takeIf { serverOption.enableTimeoutInterval && it in serverOption.timeoutIntervalRange }
    private val onts = options["tsize"]?.takeIf { serverOption.enableTransferSize }
    private val obs =
        options["blksize"]?.takeIf { serverOption.enableBlockSize && it in serverOption.blockSizeRange }
    val timeout = runCatching { oto?.value?.toInt() }.getOrElse { cerror("parse timeout fail") }
    val transferSize = runCatching { onts?.value?.toInt() }.getOrElse { cerror("parse transferSize fail") }
    val blockSize = runCatching { obs?.value?.toInt() }.getOrElse { cerror("parse blockSize fail") }
}

internal class ReadRequest(
    val filename: String,
    val options: Map<String, Option>,
    serverOption: ServerOption,
) : Request {
    private val oto =
        options["timeout"].takeIf { serverOption.enableTimeoutInterval && it in serverOption.timeoutIntervalRange }
    private val onts = options["tsize"]?.takeIf { serverOption.enableTransferSize }
    private val obs =
        options["blksize"]?.takeIf { serverOption.enableBlockSize && it in serverOption.blockSizeRange }
    val timeout = runCatching { oto?.value?.toInt() }.getOrElse { cerror("parse timeout fail") }
    val needTransferSize = onts != null
    val blockSize = runCatching { obs?.value?.toInt() }.getOrElse { cerror("parse blockSize fail") }

    fun needOACK() = !(timeout == null && !needTransferSize && blockSize == null)

    @OptIn(ExperimentalStdlibApi::class)
    fun generateOACK(fileSize: Int?): ByteArray {
        val ol = buildList<Option> {
            oto?.let(::add)
            obs?.let(::add)
            if (needTransferSize && fileSize != null) {
                add(obs!!.copy(value = "$fileSize"))
            }
        }
        return createOACK(ol)
    }
}

internal fun createOACK(list: List<Option>): ByteArray {
    val bytes = ByteArray(128)
    bytes[1] = OACK_MAGIC_LOW_BYTE
    var p = 2
    list.forEach {
        p += it.writeTo(bytes, p)
    }
    return bytes.sliceArray(0 until p)
}

internal data class Option(val rawKey: ByteArray, val normalizedName: String, val value: String) {
    companion object {

        /**
         * @return next byte after reading
         */
        fun readOption(bytes: ByteArray, range: IntRange): Pair<Option, Int> {
            val (nm, voff) = readCStyleString(bytes, range) ?: cerror("read key fail")
            val (value, e) = readCStyleString(bytes, voff) ?: cerror("read value fail")
            return Option(bytes.sliceArray(range.first..(voff - 2)), nm, value) to e
        }
    }

    fun writeTo(bytes: ByteArray, off: Int): Int {
        val arr = value.encodeToByteArray()
        check(bytes.size - off >= rawKey.size + arr.size + 2) { "space not enough" }
        var p = off
        rawKey.copyInto(bytes, p)
        p += rawKey.size
        bytes[p] = CNULL
        p++
        arr.copyInto(bytes, p)
        p += arr.size
        bytes[p] = CNULL
        p++
        return p - off
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Option

        if (!rawKey.contentEquals(other.rawKey)) return false
        if (normalizedName != other.normalizedName) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawKey.contentHashCode()
        result = 31 * result + normalizedName.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun parseRWQ(bytes: ByteArray): Request {
    val (filename, modeOff) = readCStyleString(bytes, 2) ?: cerror("parse filename fail")
    val (mode, optOff) = readCStyleString(bytes, modeOff) ?: cerror("parse mode fail")
    check(mode.toLowerCase() == "octet") { "unsupported mode" }
    var p = optOff
    val opts = buildMap<String, Option> {
        while (p in bytes.indices) {
            val (opt, pof) = Option.readOption(bytes, p..bytes.indices.last)
            p = pof
            put(opt.normalizedName, opt)
        }
    }
    return when (bytes[1]) {
        RRQ_OPCODE -> ReadRequest(filename, opts, defaultServerOption)
        WRQ_OPCODE -> WriteRequest(filename, opts, defaultServerOption)
        else -> cerror("illegal opcode")
    }
}

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
    val filename: String,
    val options: Map<String, Option>,
    serverOption: ServerOption,
) : Request {
    private val timeoutOption = options["timeout"]
    private val tsizeOption = options["tsize"]?.takeIf { serverOption.enableTransferSize }
    private val blksizeOption = options["blksize"]

    val timeout = runCatching {
        timeoutOption?.value?.toInt()
            ?.takeIf { serverOption.enableTimeoutInterval && it in serverOption.timeoutIntervalRange }
    }.getOrElse { cerror("parse timeout fail") }

    val transferSize = runCatching { tsizeOption?.value?.toInt() }.getOrElse { cerror("parse transferSize fail") }

    val blockSize = runCatching {
        blksizeOption?.value?.toInt()?.takeIf { serverOption.enableBlockSize && it in serverOption.blockSizeRange }
    }.getOrElse { cerror("parse blockSize fail") }

    fun needOACK() = !(timeout == null && transferSize == null && blockSize == null)

    @OptIn(ExperimentalStdlibApi::class)
    fun generateOACK(): ByteArray {
        check(needOACK())
        val ol = buildList<Option> {
            if (timeout != null) {
                add(timeoutOption!!)
            }
            if (blockSize != null) {
                add(blksizeOption!!)
            }
            if (transferSize != null) {
                add(tsizeOption!!)
            }
        }
        return createOACK(ol)
    }
}

internal class ReadRequest(
    val filename: String,
    val options: Map<String, Option>,
    serverOption: ServerOption,
) : Request {
    private val timeoutOption = options["timeout"]
    private val tsizeOption = options["tsize"]?.takeIf { serverOption.enableTransferSize }
    private val blksizeOption = options["blksize"]
    val timeout = runCatching {
        timeoutOption?.value?.toInt()
            ?.takeIf { serverOption.enableTimeoutInterval && it in serverOption.timeoutIntervalRange }
    }.getOrElse { cerror("parse timeout fail") }

    val needTransferSize = tsizeOption != null

    val blockSize = runCatching {
        blksizeOption?.value?.toInt()?.takeIf { serverOption.enableBlockSize && it in serverOption.blockSizeRange }
    }.getOrElse { cerror("parse blockSize fail") }

    fun needOACK() = !(timeout == null && !needTransferSize && blockSize == null)

    @OptIn(ExperimentalStdlibApi::class)
    fun generateOACK(fileSize: Int?): ByteArray {
        check(needOACK())
        val ol = buildList<Option> {
            if (timeout != null) {
                add(timeoutOption!!)
            }
            if (blockSize != null) {
                add(blksizeOption!!)
            }
            if (needTransferSize && fileSize != null) {
                add(tsizeOption!!.copy(value = "$fileSize"))
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

@OptIn(ExperimentalStdlibApi::class)
internal fun parseRWQ(bytes: ByteArray): Request {
    val (filename, modeOff) = readCStyleString(bytes, 2) ?: cerror("parse filename fail")
    val (mode, optOff) = readCStyleString(bytes, modeOff) ?: cerror("parse mode fail")
    check(mode.toLowerCase() == "octet") { "unsupported mode: $mode" }
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

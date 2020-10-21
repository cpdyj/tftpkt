package space.iseki.tftpkt

internal const val CNULL: Byte = 0
internal const val OACK_MAGIC_LOW_BYTE: Byte = 6
internal const val RRQ_OPCODE: Byte = 1
internal const val WRQ_OPCODE: Byte = 2


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
    ccheck(mode.toLowerCase() == "octet") { "unsupported mode: $mode" }
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

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

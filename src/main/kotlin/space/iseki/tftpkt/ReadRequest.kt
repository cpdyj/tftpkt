package space.iseki.tftpkt

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

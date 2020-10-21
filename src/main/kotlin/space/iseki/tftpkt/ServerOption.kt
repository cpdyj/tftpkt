package space.iseki.tftpkt

interface ServerOption {
    val enableTimeoutInterval: Boolean
    val timeoutIntervalRange: IntRange
    val enableTransferSize: Boolean
    val enableBlockSize: Boolean
    val blockSizeRange: IntRange
}

internal object defaultServerOption : ServerOption {
    override val enableTimeoutInterval: Boolean = true
    override val timeoutIntervalRange: IntRange = 1..3600
    override val enableTransferSize: Boolean = true
    override val enableBlockSize: Boolean = true
    override val blockSizeRange: IntRange = 1..1024
}

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

package com.yourpackage.neojmcomic.utils.crypto

import java.nio.charset.StandardCharsets

/** 加密编码公共工具类 */
object CryptoUtils {
    /** 字节数组转WordArray（32位整数数组） */
    fun bytesToWords(bytes: ByteArray): IntArray {
        val words = IntArray((bytes.size + 3) / 4)
        for (i in bytes.indices) {
            words[i ushr 2] = words[i ushr 2] or ((bytes[i].toInt() and 0xFF) shl (24 - (i % 4) * 8))
        }
        return words
    }

    /** WordArray转字节数组 */
    fun wordsToBytes(words: IntArray, sigBytes: Int): ByteArray {
        val bytes = ByteArray(sigBytes)
        for (i in 0 until sigBytes) {
            val word = words[i ushr 2]
            bytes[i] = (word ushr (24 - (i % 4) * 8)).toByte()
        }
        return bytes
    }

    /** 字符串转UTF-8字节数组 */
    fun stringToUtf8Bytes(str: String): ByteArray = str.toByteArray(StandardCharsets.UTF_8)

    /** UTF-8字节数组转字符串 */
    fun utf8BytesToString(bytes: ByteArray): String = String(bytes, StandardCharsets.UTF_8)

    /** 循环左移 */
    fun rotl(n: Int, bits: Int): Int = (n shl bits) or (n ushr (32 - bits))

    /** 循环右移 */
    fun rotr(n: Int, bits: Int): Int = (n shl (32 - bits)) or (n ushr bits)

    /** 生成指定长度随机字节数组（盐/IV） */
    fun generateRandomBytes(length: Int): ByteArray {
        val bytes = ByteArray(length)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes
    }
}

/** WordArray数据类（兼容原JS逻辑） */
data class WordArray(
    val words: IntArray,
    var sigBytes: Int
) {
    /** 拼接另一个WordArray */
    fun concat(other: WordArray): WordArray {
        val newWords = words.copyOf(words.size + other.words.size)
        System.arraycopy(other.words, 0, newWords, words.size, other.words.size)
        return WordArray(newWords, sigBytes + other.sigBytes)
    }

    /** 截取指定长度 */
    fun slice(start: Int, end: Int): WordArray {
        val newWords = words.copyOfRange(start, end)
        return WordArray(newWords, end - start)
    }

    /** 填充零字节到指定长度 */
    fun clamp() {
        words[sigBytes ushr 2] = words[sigBytes ushr 2] and (0xFFFFFFFF.toInt() shl (24 - (sigBytes % 4) * 8))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WordArray
        if (!words.contentEquals(other.words)) return false
        if (sigBytes != other.sigBytes) return false
        return true
    }

    override fun hashCode(): Int {
        var result = words.contentHashCode()
        result = 31 * result + sigBytes
        return result
    }
}

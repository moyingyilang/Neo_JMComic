package com.yourpackage.neojmcomic.utils.crypto

import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.bytesToWords
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.stringToUtf8Bytes
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.utf8BytesToString
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.wordsToBytes

/** 编码转换工具类（整合Base64、Hex、UTF8） */
object CryptoEncoding {
    // Base64编码表
    private const val BASE64_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
    private val BASE64_REVERSE_MAP = IntArray(128) { BASE64_MAP.indexOf(it.toChar()) }

    // region Base64
    /** Base64编码（字节数组→字符串） */
    fun base64Encode(bytes: ByteArray): String {
        val wordArray = WordArray(bytesToWords(bytes), bytes.size)
        return base64Stringify(wordArray)
    }

    /** Base64解码（字符串→字节数组） */
    fun base64Decode(str: String): ByteArray {
        val wordArray = base64Parse(str)
        return wordsToBytes(wordArray.words, wordArray.sigBytes)
    }

    private fun base64Stringify(wordArray: WordArray): String {
        wordArray.clamp()
        val words = wordArray.words
        val sigBytes = wordArray.sigBytes
        val base64Chars = mutableListOf<Char>()

        for (i in 0 until sigBytes step 3) {
            val byte1 = (words[i ushr 2] ushr (24 - (i % 4) * 8)) and 0xFF
            val byte2 = (words[(i + 1) ushr 2] ushr (24 - ((i + 1) % 4) * 8)) and 0xFF
            val byte3 = (words[(i + 2) ushr 2] ushr (24 - ((i + 2) % 4) * 8)) and 0xFF
            val triplet = (byte1 shl 16) or (byte2 shl 8) or byte3

            for (j in 0 until 4) {
                if (i + j * 0.75 < sigBytes) {
                    base64Chars.add(BASE64_MAP[(triplet ushr (6 * (3 - j))) and 0x3F])
                } else {
                    base64Chars.add('=')
                }
            }
        }
        return base64Chars.joinToString("")
    }

    private fun base64Parse(str: String): WordArray {
        var base64Str = str.replace(Regex("\\s+"), "") // 去除空白字符
        val paddingIndex = base64Str.indexOf('=')
        if (paddingIndex != -1) base64Str = base64Str.substring(0, paddingIndex)

        val words = mutableListOf<Int>()
        var nBytes = 0

        for (i in base64Str.indices) {
            if (i % 4 != 0) {
                val bits1 = BASE64_REVERSE_MAP[base64Str[i - 1].code] shl ((i % 4) * 2)
                val bits2 = BASE64_REVERSE_MAP[base64Str[i].code] ushr (6 - (i % 4) * 2)
                val bitsCombined = bits1 or bits2
                words[nBytes ushr 2] = words.getOrElse(nBytes ushr 2) { 0 } or (bitsCombined shl (24 - (nBytes % 4) * 8))
                nBytes++
            }
        }
        return WordArray(words.toIntArray(), nBytes)
    }
    // endregion

    // region Hex
    /** Hex编码（字节数组→字符串） */
    fun hexEncode(bytes: ByteArray): String {
        val sb = StringBuilder()
        bytes.forEach { sb.append(String.format("%02x", it)) }
        return sb.toString()
    }

    /** Hex解码（字符串→字节数组） */
    fun hexDecode(hexStr: String): ByteArray {
        require(hexStr.length % 2 == 0) { "Hex字符串长度必须为偶数" }
        val bytes = ByteArray(hexStr.length / 2)
        for (i in bytes.indices) {
            bytes[i] = hexStr.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return bytes
    }
    // endregion

    // region UTF8
    /** UTF-8编码（字符串→字节数组） */
    fun utf8Encode(str: String): ByteArray = stringToUtf8Bytes(str)

    /** UTF-8解码（字节数组→字符串） */
    fun utf8Decode(bytes: ByteArray): String = utf8BytesToString(bytes)
    // endregion
}

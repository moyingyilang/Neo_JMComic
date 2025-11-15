package com.yourpackage.neojmcomic.utils.persistence

import android.util.Base64

/** 数据编码工具（整合Base64+字节操作） */
object PersistenceEncode {
    /** 字节数组 → Base64编码字符串 */
    fun bytesToBase64(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /** Base64编码字符串 → 字节数组 */
    fun base64ToBytes(base64Str: String): ByteArray {
        return Base64.decode(base64Str, Base64.NO_WRAP)
    }

    /** 字节数组 → 十六进制字符串 */
    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        bytes.forEach { sb.append(String.format("%02x", it)) }
        return sb.toString()
    }

    /** 十六进制字符串 → 字节数组 */
    fun hexToBytes(hexStr: String): ByteArray {
        val len = hexStr.length
        val bytes = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            bytes[i / 2] = hexStr.substring(i, i + 2).toInt(16).toByte()
        }
        return bytes
    }
}

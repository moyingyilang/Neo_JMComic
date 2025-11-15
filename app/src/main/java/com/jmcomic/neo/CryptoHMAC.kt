package com.yourpackage.neojmcomic.utils.crypto

import com.yourpackage.neojmcomic.utils.crypto.CryptoEncoding.utf8Encode
import com.yourpackage.neojmcomic.utils.crypto.CryptoHash.md5
import com.yourpackage.neojmcomic.utils.crypto.CryptoHash.sha256
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.bytesToWords
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.wordsToBytes

/** HMAC签名工具类（依赖CryptoHash的MD5/SHA256） */
object CryptoHMAC {
    enum class HashAlgorithm { MD5, SHA256 }

    /** HMAC签名（字节数组→签名字节数组） */
    fun hmac(
        data: ByteArray,
        key: ByteArray,
        algorithm: HashAlgorithm = HashAlgorithm.SHA256
    ): ByteArray {
        val (hashFunc, blockSize) = when (algorithm) {
            HashAlgorithm.MD5 -> Pair(::md5, 64) // MD5块大小64字节
            HashAlgorithm.SHA256 -> Pair(::sha256, 64) // SHA256块大小64字节
        }

        // 处理密钥（超过块大小则哈希）
        val processedKey = if (key.size > blockSize) hashFunc(key) else key.copyOf(blockSize)

        // 生成内部和外部填充密钥
        val innerKey = processedKey.copyOf(blockSize)
        val outerKey = processedKey.copyOf(blockSize)
        for (i in 0 until blockSize) {
            innerKey[i] = (innerKey[i].toInt() xor 0x36).toByte()
            outerKey[i] = (outerKey[i].toInt() xor 0x5C).toByte()
        }

        // 计算内部哈希：innerKey + data
        val innerHash = hashFunc(innerKey + data)
        // 计算最终HMAC：outerKey + innerHash
        return hashFunc(outerKey + innerHash)
    }

    /** HMAC签名（字符串→签名字符串（Hex）） */
    fun hmacHex(
        data: String,
        key: String,
        algorithm: HashAlgorithm = HashAlgorithm.SHA256
    ): String {
        val dataBytes = utf8Encode(data)
        val keyBytes = utf8Encode(key)
        return CryptoEncoding.hexEncode(hmac(dataBytes, keyBytes, algorithm))
    }
}

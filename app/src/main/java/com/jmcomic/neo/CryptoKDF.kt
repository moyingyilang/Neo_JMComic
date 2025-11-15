package com.yourpackage.neojmcomic.utils.crypto

import com.yourpackage.neojmcomic.utils.crypto.CryptoHMAC.HashAlgorithm
import com.yourpackage.neojmcomic.utils.crypto.CryptoHMAC.hmac
import com.yourpackage.neojmcomic.utils.crypto.CryptoHash.MD5
import com.yourpackage.neojmcomic.utils.crypto.CryptoHash.SHA256
import com.yourpackage.neojmcomic.utils.crypto.CryptoHash.md5
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.bytesToWords
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.generateRandomBytes
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.wordsToBytes

/** 密钥派生工具类（整合EVPKDF、PBKDF2） */
object CryptoKDF {
    // region EVPKDF
    data class EvpKdfConfig(
        val keySize: Int = 4, // 密钥大小（word数，4=128位）
        val iterations: Int = 1,
        val hasher: (ByteArray) -> ByteArray = ::md5
    )

    /** EVPKDF密钥派生（密码+盐→密钥字节数组） */
    fun evpkdf(
        password: String,
        salt: ByteArray = generateRandomBytes(8), // 默认8字节盐
        config: EvpKdfConfig = EvpKdfConfig()
    ): Pair<ByteArray, ByteArray> {
        val passwordBytes = CryptoEncoding.utf8Encode(password)
        val hasher = config.hasher
        val keySizeBytes = config.keySize * 4
        val derivedKey = mutableListOf<Byte>()
        var block: ByteArray? = null

        while (derivedKey.size < keySizeBytes) {
            block?.let { hasher(it) }
            block = hasher(passwordBytes + salt)

            // 迭代
            for (i in 1 until config.iterations) {
                block = hasher(block!!)
            }

            derivedKey.addAll(block!!.toList())
        }

        return Pair(derivedKey.take(keySizeBytes).toByteArray(), salt)
    }
    // endregion

    // region PBKDF2
    data class Pbkdf2Config(
        val keySize: Int = 4, // 密钥大小（word数，4=128位）
        val iterations: Int = 250000,
        val algorithm: HashAlgorithm = HashAlgorithm.SHA256
    )

    /** PBKDF2密钥派生（密码+盐→密钥字节数组） */
    fun pbkdf2(
        password: String,
        salt: ByteArray = generateRandomBytes(8), // 默认8字节盐
        config: Pbkdf2Config = Pbkdf2Config()
    ): Pair<ByteArray, ByteArray> {
        val passwordBytes = CryptoEncoding.utf8Encode(password)
        val keySizeBytes = config.keySize * 4
        val derivedKey = mutableListOf<Byte>()
        var blockIndex = 1

        while (derivedKey.size < keySizeBytes) {
            var block = hmac(salt + intToBytes(blockIndex), passwordBytes, config.algorithm)
            var intermediate = block.copyOf()

            // 迭代
            for (i in 1 until config.iterations) {
                intermediate = hmac(intermediate, passwordBytes, config.algorithm)
                // XOR
                for (j in block.indices) {
                    block[j] = (block[j].toInt() xor intermediate[j].toInt()).toByte()
                }
            }

            derivedKey.addAll(block.toList())
            blockIndex++
        }

        return Pair(derivedKey.take(keySizeBytes).toByteArray(), salt)
    }
    // endregion

    /** 整数转4字节数组（大端） */
    private fun intToBytes(n: Int): ByteArray {
        return byteArrayOf(
            (n ushr 24).toByte(),
            (n ushr 16).toByte(),
            (n ushr 8).toByte(),
            n.toByte()
        )
    }
}

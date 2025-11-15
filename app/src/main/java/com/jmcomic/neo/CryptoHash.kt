package com.yourpackage.neojmcomic.utils.crypto

import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.bytesToWords
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.rotl
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.wordsToBytes

/** 哈希算法工具类（整合MD5、SHA256） */
object CryptoHash {
    // region MD5
    private val MD5_INIT = intArrayOf(0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476)
    private val MD5_ROUNDS = arrayOf(
        intArrayOf(7, 12, 17, 22), intArrayOf(5, 9, 14, 20),
        intArrayOf(4, 11, 16, 23), intArrayOf(6, 10, 15, 21)
    )
    private val MD5_CONSTANTS = IntArray(64) { (Math.sin((it + 1).toDouble()) * 0x100000000).toInt() and 0xFFFFFFFF.toInt() }

    /** MD5哈希（字节数组→哈希字节数组） */
    fun md5(bytes: ByteArray): ByteArray {
        val wordArray = WordArray(bytesToWords(bytes), bytes.size)
        val hashWords = md5Compute(wordArray)
        return wordsToBytes(hashWords, 16)
    }

    /** MD5哈希（字符串→哈希字符串） */
    fun md5Hex(str: String): String {
        return CryptoEncoding.hexEncode(md5(CryptoEncoding.utf8Encode(str)))
    }

    private fun md5Compute(wordArray: WordArray): IntArray {
        wordArray.clamp()
        val words = wordArray.words
        val sigBytes = wordArray.sigBytes
        val l = sigBytes * 8L

        // 填充
        words[sigBytes ushr 2] = words.getOrElse(sigBytes ushr 2) { 0 } or 0x80 shl (24 - (sigBytes % 4) * 8)
        val padIndex = ((sigBytes + 64) ushr 9) shl 4 + 14
        words[padIndex] = (l ushr 32).toInt()
        words[padIndex + 1] = l.toInt()
        wordArray.sigBytes = (padIndex + 2) * 4

        var a = MD5_INIT[0]
        var b = MD5_INIT[1]
        var c = MD5_INIT[2]
        var d = MD5_INIT[3]

        for (i in 0 until wordArray.words.size step 16) {
            val aa = a
            val bb = b
            val cc = c
            val dd = d

            // 4轮运算
            for (j in 0 until 64) {
                val round = j / 16
                val (f, g) = when (round) {
                    0 -> Pair((b and c) or (java.lang.Integer.not(b) and d), j)
                    1 -> Pair((d and b) or (java.lang.Integer.not(d) and c), (5 * j + 1) % 16)
                    2 -> Pair(b xor c xor d, (3 * j + 5) % 16)
                    else -> Pair(c xor (b or java.lang.Integer.not(d)), (7 * j) % 16)
                }

                val temp = d
                d = c
                c = b
                b = b + rotl(a + f + MD5_CONSTANTS[j] + words[i + g], MD5_ROUNDS[round][j % 4])
                a = temp
            }

            a += aa
            b += bb
            c += cc
            d += dd
        }

        return intArrayOf(a, b, c, d)
    }
    // endregion

    // region SHA256
    private val SHA256_INIT = intArrayOf(
        0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a,
        0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
    )
    private val SHA256_CONSTANTS = intArrayOf(
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    )

    /** SHA256哈希（字节数组→哈希字节数组） */
    fun sha256(bytes: ByteArray): ByteArray {
        val wordArray = WordArray(bytesToWords(bytes), bytes.size)
        val hashWords = sha256Compute(wordArray)
        return wordsToBytes(hashWords, 32)
    }

    /** SHA256哈希（字符串→哈希字符串） */
    fun sha256Hex(str: String): String {
        return CryptoEncoding.hexEncode(sha256(CryptoEncoding.utf8Encode(str)))
    }

    private fun sha256Compute(wordArray: WordArray): IntArray {
        wordArray.clamp()
        val words = wordArray.words
        val sigBytes = wordArray.sigBytes
        val nBitsTotal = sigBytes * 8L
        val nBitsLeft = sigBytes * 8

        // 填充
        words[sigBytes ushr 2] = words.getOrElse(sigBytes ushr 2) { 0 } or 0x80 shl (24 - (nBitsLeft % 32))
        val padIndex = ((nBitsLeft + 64) ushr 9) shl 4 + 14
        words[padIndex] = (nBitsTotal ushr 32).toInt()
        words[padIndex + 1] = nBitsTotal.toInt()
        wordArray.sigBytes = (padIndex + 2) * 4

        var a = SHA256_INIT[0]
        var b = SHA256_INIT[1]
        var c = SHA256_INIT[2]
        var d = SHA256_INIT[3]
        var e = SHA256_INIT[4]
        var f = SHA256_INIT[5]
        var g = SHA256_INIT[6]
        var h = SHA256_INIT[7]

        val W = IntArray(64)
        for (i in 0 until wordArray.words.size step 16) {
            // 初始化W数组
            for (j in 0 until 64) {
                W[j] = if (j < 16) words[i + j] else {
                    val gamma0x = W[j - 15]
                    val gamma0 = rotl(gamma0x, 25) xor rotl(gamma0x, 14) xor (gamma0x ushr 3)
                    val gamma1x = W[j - 2]
                    val gamma1 = rotl(gamma1x, 15) xor rotl(gamma1x, 13) xor (gamma1x ushr 10)
                    (gamma0 + W[j - 7] + gamma1 + W[j - 16]) and 0xFFFFFFFF.toInt()
                }
            }

            var temp1: Int
            var temp2: Int
            for (j in 0 until 64) {
                val ch = (e and f) xor (java.lang.Integer.not(e) and g)
                val maj = (a and b) xor (a and c) xor (b and c)
                val sigma0 = rotl(a, 30) xor rotl(a, 19) xor rotl(a, 10)
                val sigma1 = rotl(e, 26) xor rotl(e, 21) xor rotl(e, 7)

                temp1 = (h + sigma1 + ch + SHA256_CONSTANTS[j] + W[j]) and 0xFFFFFFFF.toInt()
                temp2 = (sigma0 + maj) and 0xFFFFFFFF.toInt()

                h = g
                g = f
                f = e
                e = (d + temp1) and 0xFFFFFFFF.toInt()
                d = c
                c = b
                b = a
                a = (temp1 + temp2) and 0xFFFFFFFF.toInt()
            }

            a = (a + SHA256_INIT[0]) and 0xFFFFFFFF.toInt()
            b = (b + SHA256_INIT[1]) and 0xFFFFFFFF.toInt()
            c = (c + SHA256_INIT[2]) and 0xFFFFFFFF.toInt()
            d = (d + SHA256_INIT[3]) and 0xFFFFFFFF.toInt()
            e = (e + SHA256_INIT[4]) and 0xFFFFFFFF.toInt()
            f = (f + SHA256_INIT[5]) and 0xFFFFFFFF.toInt()
            g = (g + SHA256_INIT[6]) and 0xFFFFFFFF.toInt()
            h = (h + SHA256_INIT[7]) and 0xFFFFFFFF.toInt()
        }

        return intArrayOf(a, b, c, d, e, f, g, h)
    }
    // endregion
}

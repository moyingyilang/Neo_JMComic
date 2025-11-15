package com.yourpackage.neojmcomic.utils.crypto

import com.yourpackage.neojmcomic.utils.crypto.CryptoCore.BlockCipher
import com.yourpackage.neojmcomic.utils.crypto.CryptoCore.CBCMode
import com.yourpackage.neojmcomic.utils.crypto.CryptoCore.CipherConfig
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.bytesToWords
import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.wordsToBytes

/** AES对称加密工具类（依赖CryptoCore的模式和填充） */
object CryptoAES : BlockCipher {
    // AES S盒与逆S盒
    private val SBOX = IntArray(256)
    private val INV_SBOX = IntArray(256)
    // 轮常数
    private val RCON = intArrayOf(0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36)

    init {
        // 初始化S盒和逆S盒
        val d = IntArray(256)
        for (i in 0 until 256) {
            d[i] = if (i < 128) i shl 1 else (i shl 1) xor 0x11b
        }

        var x = 0
        var xi = 0
        for (i in 0 until 256) {
            var sx = xi xor (xi shl 1) xor (xi shl 2) xor (xi shl 3) xor (xi shl 4)
            sx = (sx ushr 8) xor (sx and 0xff) xor 0x63
            SBOX[x] = sx
            INV_SBOX[sx] = x

            if (x == 0) {
                x = 1
                xi = 1
            } else {
                x = d[x] xor d[d[d[x xor d[x]]]
                xi = d[d[xi]]
            }
        }
    }

    override val blockSize: Int = 4 // 128位（4个32位word）

    // 密钥相关
    private lateinit var keySchedule: IntArray
    private lateinit var invKeySchedule: IntArray
    private var nRounds = 0

    /** AES加密（字节数组→加密后字节数组） */
    fun encrypt(
        data: ByteArray,
        key: ByteArray,
        iv: ByteArray = CryptoUtils.generateRandomBytes(16) // 默认生成16字节IV
    ): Pair<ByteArray, ByteArray> {
        initKey(key)
        val wordArray = WordArray(bytesToWords(data), data.size)
        val ivWordArray = WordArray(bytesToWords(iv), iv.size)
        val config = CipherConfig(CBCMode(this, ivWordArray))

        // 填充
        config.padding(wordArray, blockSize)
        // 加密
        val encryptedWords = processBlocks(wordArray.words, config.mode::encryptBlock)
        val encryptedBytes = wordsToBytes(encryptedWords, wordArray.sigBytes)

        return Pair(encryptedBytes, iv) // 返回密文+IV（解密需用同一IV）
    }

    /** AES解密（字节数组→解密后字节数组） */
    fun decrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        initKey(key)
        val wordArray = WordArray(bytesToWords(data), data.size)
        val ivWordArray = WordArray(bytesToWords(iv), iv.size)
        val config = CipherConfig(CBCMode(this, ivWordArray))

        // 解密
        val decryptedWords = processBlocks(wordArray.words, config.mode::decryptBlock)
        val decryptedWordArray = WordArray(decryptedWords, wordArray.sigBytes)
        // 解填充
        config.unpadding(decryptedWordArray)

        return wordsToBytes(decryptedWordArray.words, decryptedWordArray.sigBytes)
    }

    /** 初始化密钥（生成密钥调度表） */
    private fun initKey(key: ByteArray) {
        val keyWordArray = WordArray(bytesToWords(key), key.size)
        val keyWords = keyWordArray.words
        val keySize = key.size / 4 // 密钥大小（word数：16字节=4word=128位，24字节=6word=192位，32字节=8word=256位）

        nRounds = keySize + 6
        val ksRows = (nRounds + 1) * 4
        keySchedule = IntArray(ksRows)

        // 生成密钥调度表
        for (i in 0 until ksRows) {
            if (i < keySize) {
                keySchedule[i] = keyWords[i]
            } else {
                var t = keySchedule[i - 1]
                if (i % keySize == 0) {
                    // 循环左移+S盒替换+轮常数
                    t = (t shl 8) or (t ushr 24)
                    t = (SBOX[t ushr 24] shl 24) or (SBOX[(t ushr 16) and 0xff] shl 16) or (SBOX[(t ushr 8) and 0xff] shl 8) or SBOX[t and 0xff]
                    t = t xor (RCON[(i / keySize)] shl 24)
                } else if (keySize > 6 && i % keySize == 4) {
                    // S盒替换
                    t = (SBOX[t ushr 24] shl 24) or (SBOX[(t ushr 16) and 0xff] shl 16) or (SBOX[(t ushr 8) and 0xff] shl 8) or SBOX[t and 0xff]
                }
                keySchedule[i] = keySchedule[i - keySize] xor t
            }
        }

        // 生成逆密钥调度表
        invKeySchedule = IntArray(ksRows)
        for (i in 0 until ksRows) {
            val ksRow = ksRows - i
            invKeySchedule[i] = if (i % 4 == 0 || ksRow <= 4) {
                keySchedule[ksRow - if (i % 4 == 0) 4 else 0]
            } else {
                val t = keySchedule[ksRow]
                val s0 = SBOX[t ushr 24]
                val s1 = SBOX[(t ushr 16) and 0xff]
                val s2 = SBOX[(t ushr 8) and 0xff]
                val s3 = SBOX[t and 0xff]

                // 逆列混合
                (mul(0xe, s0) xor mul(0xb, s1) xor mul(0xd, s2) xor mul(0x9, s3)) shl 24 or
                (mul(0x9, s0) xor mul(0xe, s1) xor mul(0xb, s2) xor mul(0xd, s3)) shl 16 or
                (mul(0xd, s0) xor mul(0x9, s1) xor mul(0xe, s2) xor mul(0xb, s3)) shl 8 or
                (mul(0xb, s0) xor mul(0xd, s1) xor mul(0x9, s2) xor mul(0xe, s3))
            }
        }
    }

    override fun encryptBlock(words: IntArray, offset: Int) {
        // 轮密钥加（初始）
        for (i in 0 until blockSize) {
            words[offset + i] = words[offset + i] xor keySchedule[i]
        }

        // 轮变换（nRounds-1轮）
        var ksIndex = blockSize
        for (round in 1 until nRounds) {
            // 字节替换
            subBytes(words, offset)
            // 行移位
            shiftRows(words, offset)
            // 列混合
            mixColumns(words, offset)
            // 轮密钥加
            addRoundKey(words, offset, ksIndex)
            ksIndex += blockSize
        }

        // 最后一轮（无列混合）
        subBytes(words, offset)
        shiftRows(words, offset)
        addRoundKey(words, offset, ksIndex)
    }

    override fun decryptBlock(words: IntArray, offset: Int) {
        // 轮密钥加（初始）
        addRoundKey(words, offset, keySchedule.size - blockSize)

        // 轮变换（nRounds-1轮）
        var ksIndex = keySchedule.size - 2 * blockSize
        for (round in 1 until nRounds) {
            // 逆行移位
            invShiftRows(words, offset)
            // 逆字节替换
            invSubBytes(words, offset)
            // 轮密钥加
            addRoundKey(words, offset, ksIndex)
            // 逆列混合
            invMixColumns(words, offset)
            ksIndex -= blockSize
        }

        // 最后一轮（无逆列混合）
        invShiftRows(words, offset)
        invSubBytes(words, offset)
        addRoundKey(words, offset, 0)
    }

    // region AES内部变换
    private fun subBytes(words: IntArray, offset: Int) {
        for (i in 0 until blockSize) {
            val word = words[offset + i]
            words[offset + i] = (SBOX[word ushr 24] shl 24) or
                    (SBOX[(word ushr 16) and 0xff] shl 16) or
                    (SBOX[(word ushr 8) and 0xff] shl 8) or
                    SBOX[word and 0xff]
        }
    }

    private fun invSubBytes(words: IntArray, offset: Int) {
        for (i in 0 until blockSize) {
            val word = words[offset + i]
            words[offset + i] = (INV_SBOX[word ushr 24] shl 24) or
                    (INV_SBOX[(word ushr 16) and 0xff] shl 16) or
                    (INV_SBOX[(word ushr 8) and 0xff] shl 8) or
                    INV_SBOX[word and 0xff]
        }
    }

    private fun shiftRows(words: IntArray, offset: Int) {
        // 第1行（无移位）
        // 第2行（左移1字节）
        val row1 = words[offset + 1]
        words[offset + 1] = (row1 shl 8) or (row1 ushr 24)
        // 第3行（左移2字节）
        val row2 = words[offset + 2]
        words[offset + 2] = (row2 shl 16) or (row2 ushr 16)
        // 第4行（左移3字节）
        val row3 = words[offset + 3]
        words[offset + 3] = (row3 shl 24) or (row3 ushr 8)
    }

    private fun invShiftRows(words: IntArray, offset: Int) {
        // 第1行（无移位）
        // 第2行（右移1字节）
        val row1 = words[offset + 1]
        words[offset + 1] = (row1 shl 24) or (row1 ushr 8)
        // 第3行（右移2字节）
        val row2 = words[offset + 2]
        words[offset + 2] = (row2 shl 16) or (row2 ushr 16)
        // 第4行（右移3字节）
        val row3 = words[offset + 3]
        words[offset + 3] = (row3 shl 8) or (row3 ushr 24)
    }

    private fun mixColumns(words: IntArray, offset: Int) {
        for (i in 0 until blockSize) {
            val col = words[offset + i]
            val b0 = (col ushr 24) and 0xff
            val b1 = (col ushr 16) and 0xff
            val b2 = (col ushr 8) and 0xff
            val b3 = col and 0xff

            words[offset + i] = (mul(2, b0) xor mul(3, b1) xor b2 xor b3) shl 24 or
                    (b0 xor mul(2, b1) xor mul(3, b2) xor b3) shl 16 or
                    (b0 xor b1 xor mul(2, b2) xor mul(3, b3)) shl 8 or
                    (mul(3, b0) xor b1 xor b2 xor mul(2, b3))
        }
    }

    private fun invMixColumns(words: IntArray, offset: Int) {
        for (i in 0 until blockSize) {
            val col = words[offset + i]
            val b0 = (col ushr 24) and 0xff
            val b1 = (col ushr 16) and 0xff
            val b2 = (col ushr 8) and 0xff
            val b3 = col and 0xff

            words[offset + i] = (mul(0xe, b0) xor mul(0xb, b1) xor mul(0xd, b2) xor mul(0x9, b3)) shl 24 or
                    (mul(0x9, b0) xor mul(0xe, b1) xor mul(0xb, b2) xor mul(0xd, b3)) shl 16 or
                    (mul(0xd, b0) xor mul(0x9, b1) xor mul(0xe, b2) xor mul(0xb, b3)) shl 8 or
                    (mul(0xb, b0) xor mul(0xd, b1) xor mul(0x9, b2) xor mul(0xe, b3))
        }
    }

    private fun addRoundKey(words: IntArray, offset: Int, ksIndex: Int) {
        for (i in 0 until blockSize) {
            words[offset + i] = words[offset + i] xor keySchedule[ksIndex + i]
        }
    }

    // 有限域GF(2^8)乘法
    private fun mul(a: Int, b: Int): Int {
        var result = 0
        var aa = a
        var bb = b
        while (aa > 0) {
            if (aa and 1 != 0) {
                result = result xor bb
            }
            val carry = bb and 0x80
            bb = bb shl 1
            if (carry != 0) {
                bb = bb xor 0x11b
            }
            aa = aa ushr 1
        }
        return result and 0xff
    }
    // endregion

    /** 处理多个块 */
    private fun processBlocks(words: IntArray, blockProcessor: (IntArray, Int) -> Unit): IntArray {
        val processedWords = words.copyOf()
        for (i in 0 until processedWords.size step blockSize) {
            blockProcessor(processedWords, i)
        }
        return processedWords
    }
}

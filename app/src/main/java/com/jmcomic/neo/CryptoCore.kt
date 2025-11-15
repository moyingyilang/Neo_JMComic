package com.yourpackage.neojmcomic.utils.crypto

import com.yourpackage.neojmcomic.utils.crypto.CryptoUtils.wordsToBytes

/** 加密核心工具类（整合模式、填充，为AES提供支持） */
object CryptoCore {
    // region 填充模式（PKCS7）
    object Pkcs7 {
        /** 填充 */
        fun pad(data: WordArray, blockSize: Int) {
            val blockSizeBytes = blockSize * 4
            val nPaddingBytes = blockSizeBytes - (data.sigBytes % blockSizeBytes)
            val paddingWord = (nPaddingBytes shl 24) or (nPaddingBytes shl 16) or (nPaddingBytes shl 8) or nPaddingBytes
            val paddingWords = IntArray(nPaddingBytes / 4) { paddingWord }
            data.concat(WordArray(paddingWords, nPaddingBytes))
        }

        /** 解填充 */
        fun unpad(data: WordArray) {
            val nPaddingBytes = data.words[(data.sigBytes - 1) ushr 2] and 0xFF
            data.sigBytes -= nPaddingBytes
        }
    }
    // endregion

    // region 加密模式（CBC）
    class CBCMode(
        private val cipher: BlockCipher,
        private val iv: WordArray
    ) {
        private var prevBlock: WordArray? = null

        /** 加密块 */
        fun encryptBlock(words: IntArray, offset: Int) {
            val blockSize = cipher.blockSize
            // XOR与IV/前一块
            xorBlock(words, offset, blockSize)
            // 加密
            cipher.encryptBlock(words, offset)
            // 保存当前块为下一块的前块
            prevBlock = WordArray(words.copyOfRange(offset, offset + blockSize), blockSize * 4)
        }

        /** 解密块 */
        fun decryptBlock(words: IntArray, offset: Int) {
            val blockSize = cipher.blockSize
            // 保存当前块
            val currentBlock = WordArray(words.copyOfRange(offset, offset + blockSize), blockSize * 4)
            // 解密
            cipher.decryptBlock(words, offset)
            // XOR与IV/前一块
            xorBlock(words, offset, blockSize)
            // 保存当前块为下一块的前块
            prevBlock = currentBlock
        }

        private fun xorBlock(words: IntArray, offset: Int, blockSize: Int) {
            val block = prevBlock ?: iv
            for (i in 0 until blockSize) {
                words[offset + i] = words[offset + i] xor block.words[i]
            }
        }
    }
    // endregion

    /** 块加密接口 */
    interface BlockCipher {
        val blockSize: Int // 块大小（32位word数）
        fun encryptBlock(words: IntArray, offset: Int)
        fun decryptBlock(words: IntArray, offset: Int)
    }

    /** 加密参数配置 */
    data class CipherConfig(
        val mode: CBCMode,
        val padding: (WordArray, Int) -> Unit = Pkcs7::pad,
        val unpadding: (WordArray) -> Unit = Pkcs7::unpad
    )
}

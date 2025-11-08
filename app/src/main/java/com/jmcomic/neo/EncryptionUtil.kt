// utils/EncryptionUtil.kt
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log

object EncryptionUtil {
    
    // MD5加密
    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    // AES解密 (ECB模式)
    fun decryptAES(encryptedText: String, key: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("EncryptionUtil", "AES解密失败", e)
            throw e
        }
    }
    
    // 尝试多个密钥解密
    fun tryMultipleKeysDecrypt(encryptedText: String, url: String): String {
        val possibleKeys = listOf(
            listOf(49, 56, 53, 72, 99, 111, 109, 105, 99, 51, 80, 65, 80, 80, 55, 82), // 185Hcomic3PAPP7R
            listOf(49, 56, 99, 111, 109, 105, 99, 65, 80, 80, 67, 111, 110, 116, 101, 110, 116) // 18comicAPPContent
        )
        
        val adKeyUrls = listOf("ad_content_all", "advertise_all")
        val isAdUrl = adKeyUrls.any { url.contains(it) }
        
        for (key in possibleKeys) {
            val content = String(key.map { it.toChar() }.toCharArray())
            val keyToTry = if (isAdUrl) {
                md5(content)
            } else {
                val currentTime = System.currentTimeMillis() / 1000
                md5("$currentTime$content")
            }
            
            try {
                return decryptAES(encryptedText, keyToTry)
            } catch (e: Exception) {
                // 继续尝试下一个密钥
                continue
            }
        }
        
        throw Exception("所有解密尝试都失败")
    }
}
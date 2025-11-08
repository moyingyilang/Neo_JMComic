// ImageScrambleUtil.kt
import android.graphics.*
import android.widget.ImageView
import kotlin.math.floor

object ImageScrambleUtil {
    
    // 打乱图片
    fun scrambleImage(
        imageView: ImageView,
        aid: String,
        scrambleId: String,
        page: String,
        onScrambled: (Bitmap) -> Unit
    ) {
        // 如果是GIF或者aid小于scrambleId，不处理
        if (aid.toIntOrNull() ?: 0 < scrambleId.toIntOrNull() ?: 0) {
            return
        }
        
        // 获取打乱数量
        val num = getNum(aid, page)
        
        // 异步处理图片打乱
        processImageScramble(imageView, num, onScrambled)
    }
    
    private fun processImageScramble(
        imageView: ImageView,
        num: Int,
        onScrambled: (Bitmap) -> Unit
    ) {
        // 获取图片的Bitmap
        val drawable = imageView.drawable
        if (drawable is BitmapDrawable) {
            val originalBitmap = drawable.bitmap
            val scrambledBitmap = scrambleBitmap(originalBitmap, num)
            onScrambled(scrambledBitmap)
        }
    }
    
    private fun scrambleBitmap(bitmap: Bitmap, num: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // 创建新的Bitmap
        val scrambledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(scrambledBitmap)
        
        val remainder = height % num
        val copyW = width
        
        for (i in 0 until num) {
            var copyH = floor(height.toDouble() / num).toInt()
            var py = copyH * i
            var y = height - copyH * (i + 1) - remainder
            
            if (i == 0) {
                copyH += remainder
            } else {
                py += remainder
            }
            
            // 创建源矩形和目标矩形
            val srcRect = Rect(0, y, copyW, y + copyH)
            val destRect = Rect(0, py, copyW, py + copyH)
            
            // 绘制打乱后的部分
            canvas.drawBitmap(bitmap, srcRect, destRect, null)
        }
        
        return scrambledBitmap
    }
    
    private fun getNum(aid: String, page: String): Int {
        val base64Aid = base64Encode(aid)
        val base64Page = base64Encode(page)
        
        val key = aid + page
        val md5Key = EncryptionUtil.md5(key)
        val lastChar = md5Key.last()
        var keyValue = lastChar.toInt()
        
        // 根据aid范围调整keyValue
        val aidInt = aid.toIntOrNull() ?: 0
        val rangeStart = base64DecodeToInt("MjY4ODUw")
        val rangeEnd = base64DecodeToInt("NDIxOTI1")
        val rangeStart2 = base64DecodeToInt("NDIxOTI2")
        
        when {
            aidInt in rangeStart..rangeEnd -> keyValue %= 10
            aidInt >= rangeStart2 -> keyValue %= 8
        }
        
        return when (keyValue) {
            0 -> 2
            1 -> 4
            2 -> 6
            3 -> 8
            4 -> 10
            5 -> 12
            6 -> 14
            7 -> 16
            8 -> 18
            9 -> 20
            else -> 10
        }
    }
    
    private fun base64Encode(input: String): String {
        return Base64.encodeToString(input.toByteArray(), Base64.DEFAULT)
            .replace("\n", "") // 移除换行符
    }
    
    private fun base64DecodeToInt(input: String): Int {
        return try {
            val decoded = Base64.decode(input, Base64.DEFAULT)
            String(decoded).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
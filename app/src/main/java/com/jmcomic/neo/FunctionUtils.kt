// FunctionUtils.kt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

object FunctionUtils {
    
    // 分块数组
    fun <T> chunkArray(arr: List<T>?, size: Int): List<List<T>> {
        if (arr.isNullOrEmpty()) return emptyList()
        
        val result = mutableListOf<List<T>>()
        for (i in arr.indices step size) {
            val end = min(i + size, arr.size)
            result.add(arr.subList(i, end))
        }
        return result
    }
    
    // 随机获取项目
    fun <T> getRandomItems(arr: List<T>?, count: Int = 1): RandomItemsResult<T> {
        if (arr.isNullOrEmpty()) {
            return RandomItemsResult(emptyList(), emptyList())
        }
        
        val maxCount = min(count, arr.size)
        val shuffled = arr.shuffled()
        val result = shuffled.take(maxCount)
        val indexes = result.map { arr.indexOf(it) }
        
        return RandomItemsResult(result, indexes)
    }
    
    data class RandomItemsResult<T>(
        val items: List<T>,
        val indexes: List<Int>
    )
    
    // 获取与当前时间的日期差
    fun getDateDiffFromNow(time: Long): Int {
        val now = System.currentTimeMillis()
        val updated = time * 1000
        val diffMs = now - updated
        return (diffMs / (1000 * 60 * 60 * 24)).toInt()
    }
    
    // 获取台北时间字符串
    fun getTaipeiTimeString(): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.TAIWAN)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Taipei")
        return formatter.format(Date())
    }
    
    // 获取当前时间 (HH:mm)
    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
    
    // 获取今天时间 (HH:mm:ss)
    fun getToday(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
    
    // 获取星期信息
    data class WeekInfo(
        val today: String,
        val todayIndex: Int,
        val allDays: List<String>
    )
    
    fun getWeekInfo(weekDayItems: List<String>): WeekInfo {
        val calendar = Calendar.getInstance()
        var rawIndex = calendar.get(Calendar.DAY_OF_WEEK)
        // 调整：周日=0 -> 6, 周一=1 -> 0, 周二=2 -> 1, 等等
        val todayIndex = if (rawIndex == Calendar.SUNDAY) 6 else rawIndex - 2
        val weekDays = weekDayItems
        
        return WeekInfo(
            today = weekDays[todayIndex],
            todayIndex = todayIndex + 1,
            allDays = weekDays
        )
    }
    
    // 获取标记颜色
    fun getMarkColor(type: String): Int {
        return when (type) {
            "fanbox" -> Color.parseColor("#5abee8")
            "fantia" -> Color.parseColor("#3c7620")
            "gumroad" -> Color.parseColor("#fc92e9")
            "patreon" -> Color.parseColor("#c23f2b")
            "subscribestar" -> Color.parseColor("#56b8ac")
            else -> Color.parseColor("#3c7620")
        }
    }
    
    // 下拉刷新文本渲染
    fun renderText(pullStatus: String, percent: Int): String {
        return when (pullStatus) {
            "pulling" -> "下拉刷新"
            "canRelease" -> "释放立即刷新"
            "refreshing" -> "刷新中..."
            "complete" -> "刷新完成"
            else -> ""
        }
    }
}
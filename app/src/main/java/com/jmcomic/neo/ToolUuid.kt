package com.yourpackage.neojmcomic.utils.tool

import android.os.Build
import java.util.UUID

/** UUID生成工具（生成V4 UUID，适配Android各版本） */
object ToolUuid {
    /** 生成V4 UUID（兼容低版本Android） */
    fun generate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UUID.randomUUID().toString()
        } else {
            // 低版本兼容方案：基于时间戳+随机数
            val time = System.currentTimeMillis()
            val random = java.util.Random(time).nextLong()
            UUID(time shr 32, time and 0xFFFFFFFFL or (random shl 32)).toString()
        }
    }

    /** 生成带前缀的UUID（用于缓存键、唯一标识等场景） */
    fun generateWithPrefix(prefix: String): String {
        return "$prefix:${generate()}"
    }
}

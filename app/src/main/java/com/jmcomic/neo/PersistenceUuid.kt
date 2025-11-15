package com.yourpackage.neojmcomic.utils.persistence

import android.os.Build
import java.util.UUID

/** 唯一标识生成工具（适配Android） */
object PersistenceUuid {
    /** 生成V4 UUID（用于缓存键等场景） */
    fun generate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UUID.randomUUID().toString()
        } else {
            // 兼容低版本：基于时间戳+随机数生成
            val time = System.currentTimeMillis()
            val random = java.util.Random(time).nextLong()
            UUID(time shr 32, time and 0xFFFFFFFFL or (random shl 32)).toString()
        }
    }

    /** 生成带前缀的缓存键 */
    fun generateCacheKey(prefix: String): String {
        return "$prefix:${generate()}"
    }
}

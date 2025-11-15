package com.yourpackage.neojmcomic.utils.persistence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/** 带防抖的内存缓存工具 */
class PersistenceMemo<T, R>(
    private val debounceDelay: Long = 166 // 默认防抖延迟（对应原166ms）
) {
    private val cache = ConcurrentHashMap<T, R>()
    private val debounceJobs = ConcurrentHashMap<T, kotlinx.coroutines.Job?>()

    /** 缓存函数结果（带防抖） */
    fun memoize(key: T, func: () -> R): R {
        // 先查缓存
        cache[key]?.let { return it }
        
        // 防抖处理
        debounceJobs[key]?.cancel()
        val job = GlobalScope.launch(Dispatchers.IO) {
            delay(debounceDelay)
            cache[key] = func()
        }
        debounceJobs[key] = job
        
        // 执行函数并缓存
        val result = func()
        cache[key] = result
        return result
    }

    /** 获取缓存值 */
    fun get(key: T): R? = cache[key]

    /** 清除单个缓存 */
    fun remove(key: T) {
        cache.remove(key)
        debounceJobs[key]?.cancel()
        debounceJobs.remove(key)
    }

    /** 清空缓存 */
    fun clear() {
        cache.clear()
        debounceJobs.forEach { it.value?.cancel() }
        debounceJobs.clear()
    }
}

package com.yourpackage.neojmcomic.utils.tool

import java.util.LinkedHashMap

/** 记忆化缓存工具（基于LRU算法，避免重复计算） */
class ToolMemo<K, V>(private val maxSize: Int = 100) {
    // LinkedHashMap实现LRU：访问顺序排序，移除最久未使用的元素
    private val cache = object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
            return size > maxSize
        }
    }

    /** 缓存函数结果，已存在则直接返回，否则执行函数并缓存 */
    fun memoize(key: K, func: () -> V): V {
        return cache[key] ?: run {
            val result = func()
            cache[key] = result
            result
        }
    }

    /** 获取缓存值 */
    fun get(key: K): V? = cache[key]

    /** 移除指定缓存 */
    fun remove(key: K) = cache.remove(key)

    /** 清空所有缓存 */
    fun clear() = cache.clear()

    /** 获取缓存大小 */
    fun size() = cache.size
}

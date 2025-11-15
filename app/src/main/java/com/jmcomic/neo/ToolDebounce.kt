package com.yourpackage.neojmcomic.utils.tool

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 防抖工具（默认166ms延迟，适配Kotlin协程） */
object ToolDebounce {
    private val jobs = mutableMapOf<String, Job>()

    /**
     * @param key 唯一标识（避免同一事件重复触发）
     * @param wait 防抖延迟时间（默认166ms）
     * @param action 防抖后执行的操作
     */
    fun debounce(
        key: String,
        wait: Long = 166,
        action: suspend CoroutineScope.() -> Unit
    ) {
        jobs[key]?.cancel()
        val job = CoroutineScope(Dispatchers.Main).launch {
            delay(wait)
            action()
            jobs.remove(key)
        }
        jobs[key] = job
    }

    /** 清除指定key的防抖任务 */
    fun clear(key: String) {
        jobs[key]?.cancel()
        jobs.remove(key)
    }

    /** 清除所有防抖任务 */
    fun clearAll() {
        jobs.forEach { it.value.cancel() }
        jobs.clear()
    }
}

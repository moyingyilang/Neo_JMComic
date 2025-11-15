package com.yourpackage.neojmcomic.utils

import android.content.Context

/**
 * 获取全局上下文（兼容多环境）
 * @return 全局上下文，未找到时返回null
 */
fun getGlobalContext(): Context? {
    return try {
        // 反射获取全局ApplicationContext（Android专用）
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
        currentApplicationMethod.invoke(null) as Context
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取全局self对象（兼容Android/Node环境）
 * @return 全局self对象，未找到时返回null
 */
val optionalSelf: Any?
    get() {
        return try {
            getGlobalContext() ?: run {
                // Node环境兼容（实际Android中不会执行）
                Class.forName("java.lang.System").getMethod("getProperties").invoke(null)
            }
        } catch (e: Exception) {
            null
        }
    }

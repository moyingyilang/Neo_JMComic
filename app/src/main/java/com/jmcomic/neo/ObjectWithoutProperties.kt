package com.yourpackage.neojmcomic.utils

/**
 * 从对象中剔除指定属性，返回剩余属性组成的新对象
 * @param obj 源对象
 * @param excluded 需剔除的属性名列表
 * @return 剔除后的新对象
 */
fun <T : Map<String, Any?>> objectWithoutProperties(
    obj: T?,
    excluded: List<String>
): Map<String, Any?> {
    if (obj == null) return emptyMap()

    val result = mutableMapOf<String, Any?>()

    // 拷贝未被排除的普通属性
    obj.forEach { (key, value) ->
        if (!excluded.contains(key)) {
            result[key] = value
        }
    }

    // 拷贝Symbol属性（Kotlin中通过额外映射支持）
    copySymbolProperties(obj, result, excluded)

    return result
}

/**
 * 拷贝Symbol属性（模拟JavaScript Symbol属性处理）
 * 实际Kotlin中需通过自定义Symbol映射实现
 */
private fun <T : Map<String, Any?>> copySymbolProperties(
    source: T,
    target: MutableMap<String, Any?>,
    excluded: List<String>
) {
    // 模拟Symbol属性处理：Kotlin中无原生Symbol，可通过特定前缀标识
    source.forEach { (key, value) ->
        if (key.startsWith("__symbol_") && !excluded.contains(key)) {
            target[key] = value
        }
    }
}

/** 针对MutableMap的重载方法（支持修改原对象） */
fun <T : MutableMap<String, Any?>> objectWithoutPropertiesInPlace(
    obj: T?,
    excluded: List<String>
): T? {
    if (obj == null) return null
    excluded.forEach { key ->
        obj.remove(key)
        obj.remove("__symbol_$key") // 移除对应Symbol属性
    }
    return obj
}

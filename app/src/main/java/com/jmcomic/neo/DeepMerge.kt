package com.yourpackage.neojmcomic.utils

import com.yourpackage.neojmcomic.ui.component.ReactElement

/**
 * 判断是否为纯对象（非null、原型为Object或null、无特殊Symbol属性）
 */
fun isPlainObject(item: Any?): Boolean {
    if (item == null || item !is Map<*, *>) return false
    val prototype = item.javaClass.superclass
    return prototype == null || prototype == Map::class.java
}

/**
 * 深度克隆对象（React元素浅拷贝）
 */
private fun deepClone(source: Any?): Any? {
    if (source is ReactElement) return source
    if (!isPlainObject(source)) return source
    val output = mutableMapOf<Any?, Any?>()
    (source as Map<*, *>).forEach { (key, value) ->
        output[key] = deepClone(value)
    }
    return output
}

/**
 * 深度合并两个对象
 * @param target 目标对象
 * @param source 源对象
 * @param clone 是否克隆目标对象（默认true，避免修改原对象）
 * @return 合并后的对象
 */
fun deepmerge(
    target: Any?,
    source: Any?,
    clone: Boolean = true
): Any? {
    val output = if (clone && target is Map<*, *>) {
        mutableMapOf<Any?, Any?>().apply { putAll(target) }
    } else {
        target
    }

    if (isPlainObject(target) && isPlainObject(source)) {
        (source as Map<*, *>).forEach { (key, value) ->
            when {
                value is ReactElement -> {
                    (output as MutableMap<*, *>)[key] = value
                }
                isPlainObject(value) && isPlainObject(target?.get(key)) -> {
                    (output as MutableMap<*, *>)[key] = deepmerge(target?.get(key), value, clone)
                }
                clone -> {
                    (output as MutableMap<*, *>)[key] = if (isPlainObject(value)) deepClone(value) else value
                }
                else -> {
                    (output as MutableMap<*, *>)[key] = value
                }
            }
        }
    }

    return output
}

/** 针对Theme的重载方法，优化类型安全 */
fun deepmergeTheme(target: Theme, source: Map<String, Any>): Theme {
    val merged = deepmerge(target, source) as Map<*, *>
    return Theme(
        breakpoints = merged["breakpoints"] as BreakpointsConfig,
        direction = merged["direction"] as String ?: target.direction,
        components = merged["components"] as MutableMap<String, Any> ?: target.components,
        palette = merged["palette"] as PaletteConfig,
        spacing = merged["spacing"] as SpacingConfig,
        shape = merged["shape"] as ShapeConfig,
        unstable_sxConfig = merged["unstable_sxConfig"] as Map<String, Any> ?: target.unstable_sxConfig
    )
}

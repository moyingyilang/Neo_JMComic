package com.yourpackage.neojmcomic.utils.theme.emotion

import com.yourpackage.neojmcomic.utils.hash.Md5Hash

/** 无单位属性列表 */
private val unitlessProperties = setOf(
    "animationIterationCount", "aspectRatio", "borderImageOutset", "borderImageSlice",
    "borderImageWidth", "boxFlex", "boxFlexGroup", "boxOrdinalGroup", "columnCount",
    "columns", "flex", "flexGrow", "flexPositive", "flexShrink", "flexNegative",
    "flexOrder", "fontWeight", "lineHeight", "opacity", "order", "orphans", "scale",
    "tabSize", "widows", "zIndex", "zoom"
)

/**
 * 样式序列化工具
 * 将JS样式对象转换为CSS字符串
 */
object EmotionSerialize {
    /**
     * 序列化样式
     * @param args 样式参数（支持模板字符串、对象、函数）
     * @param registered 已注册样式缓存
     * @param mergedProps 组件属性（用于函数式样式）
     * @return 序列化后的样式
     */
    fun serializeStyles(
        args: Array<Any>,
        registered: Map<String, String> = emptyMap(),
        mergedProps: Map<String, Any> = emptyMap()
    ): SerializedStyle {
        val styles = buildString {
            args.forEach { arg ->
                append(handleInterpolation(arg, registered, mergedProps))
            }
        }
        val name = Md5Hash.hash(styles)
        return SerializedStyle(name, styles.trimEnd(';'))
    }

    /** 处理单个插值项 */
    private fun handleInterpolation(
        interpolation: Any?,
        registered: Map<String, String>,
        mergedProps: Map<String, Any>
    ): String {
        return when {
            interpolation == null || interpolation is Boolean -> ""
            interpolation is SerializedStyle -> interpolation.styles
            interpolation is Map<*, *> -> createStringFromObject(interpolation, registered)
            interpolation is List<*> -> interpolation.joinToString(";") {
                handleInterpolation(it, registered, mergedProps)
            }
            interpolation is Function<*> -> {
                // 处理函数式样式
                val result = interpolation.invoke(mergedProps)
                handleInterpolation(result, registered, mergedProps)
            }
            interpolation is String -> {
                registered[interpolation] ?: interpolation
            }
            else -> interpolation.toString()
        }
    }

    /** 将对象转换为CSS字符串 */
    private fun createStringFromObject(
        obj: Map<*, *>,
        registered: Map<String, String>
    ): String {
        return buildString {
            obj.forEach { (key, value) ->
                val prop = key.toString().hyphenate()
                val processedValue = processValue(prop, value)
                if (registered.containsKey(processedValue)) {
                    append("$prop: ${registered[processedValue]};")
                } else {
                    append("$prop: $processedValue;")
                }
            }
        }
    }

    /** 处理样式值（添加单位、转换动画关键帧等） */
    private fun processValue(property: String, value: Any?): String {
        if (value == null) return ""
        val valueStr = value.toString()

        // 处理无单位属性
        if (unitlessProperties.contains(property) || property.startsWith("--")) {
            return valueStr
        }

        // 数字值添加px单位（0除外）
        if (value is Number && value != 0) {
            return "${value}px"
        }

        // 处理动画关键帧标记
        return valueStr.replace(Regex("_EMO_([^_]+?)_([^]*?)_EMO_"), "$1")
    }

    /** 驼峰转连字符（camelCase -> camel-case） */
    private fun String.hyphenate(): String {
        return replace(Regex("[A-Z]"), "-$0").lowercase()
    }
}

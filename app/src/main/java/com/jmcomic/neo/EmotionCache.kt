package com.yourpackage.neojmcomic.utils.theme.emotion

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

/**
 * Emotion样式缓存管理器
 * 处理样式插入、缓存、SSR hydration适配
 */
class EmotionCache(
    private val context: Context,
    private val key: String = "css",
    private val container: ViewGroup = ViewGroup(context),
    private val nonce: String? = null,
    private val speedy: Boolean = true,
    private val prepend: Boolean = false
) {
    private val inserted = mutableMapOf<String, Boolean>()
    private val registered = mutableMapOf<String, String>()
    private val styleSheet = StyleSheet(container, nonce, speedy, prepend)

    init {
        // 处理SSR样式hydration
        hydrateSsrStyles()
    }

    /** 插入样式到缓存并渲染 */
    fun insert(selector: String?, serialized: SerializedStyle, shouldCache: Boolean = true) {
        if (shouldCache && inserted.containsKey(serialized.name)) return

        val style = if (selector.isNullOrEmpty()) {
            serialized.styles
        } else {
            "$selector { ${serialized.styles} }"
        }

        // 编译并插入样式
        val compiledStyle = Stylis.compile(style)
        styleSheet.insert(compiledStyle)

        if (shouldCache) {
            inserted[serialized.name] = true
            registered[serialized.name] = style
        }
    }

    /** 从SSR渲染的样式中恢复缓存 */
    private fun hydrateSsrStyles() {
        // 模拟Web端SSR样式处理，Android中可通过WebView或原生View恢复
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WebView(context).apply {
                // 加载SSR样式并恢复缓存状态
                loadUrl("javascript:(function(){ /* 样式恢复逻辑 */ })()")
            }
        }
    }

    /** 获取已注册的样式 */
    fun getRegisteredStyle(name: String): String? = registered[name]

    /** 清空缓存和样式 */
    fun clear() {
        inserted.clear()
        registered.clear()
        styleSheet.clear()
    }
}

/** 样式表管理 */
private class StyleSheet(
    private val container: ViewGroup,
    private val nonce: String?,
    private val speedy: Boolean,
    private val prepend: Boolean
) {
    private val styleViews = mutableListOf<View>()

    fun insert(style: String) {
        val styleView = View(container.context).apply {
            tag = "emotion-style"
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        }
        // 实际项目中可通过WebView或自定义View渲染CSS
        if (prepend) {
            container.addView(styleView, 0)
        } else {
            container.addView(styleView)
        }
        styleViews.add(styleView)
    }

    fun clear() {
        styleViews.forEach { container.removeView(it) }
        styleViews.clear()
    }
}

/** 样式序列化模型 */
data class SerializedStyle(
    val name: String,
    val styles: String,
    val next: SerializedStyle? = null
)

/** Stylis样式编译工具（模拟） */
object Stylis {
    fun compile(style: String): String {
        // 模拟样式编译：添加浏览器前缀、处理嵌套等
        return prefixStyle(style)
    }

    private fun prefixStyle(style: String): String {
        // 简化前缀处理，实际需根据属性添加-webkit-/-moz-等前缀
        return style.replace("animation", "-webkit-animation")
            .replace("transform", "-webkit-transform")
    }
}

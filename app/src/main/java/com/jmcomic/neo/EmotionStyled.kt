package com.yourpackage.neojmcomic.utils.theme.emotion

import com.yourpackage.neojmcomic.ui.component.View

/**
 * 创建Styled组件工厂
 * 支持HTML/SVG标签及自定义组件
 */
object EmotionStyled {
    // 支持的HTML/SVG标签列表
    private val supportedTags = listOf(
        "a", "div", "span", "button", "input", "img", "svg", "path", "circle", "rect",
        "h1", "h2", "h3", "p", "ul", "li", "form", "label", "table", "tr", "td"
    )

    /** 创建Styled组件 */
    fun <T : View> styled(
        tag: String,
        vararg styles: Any
    ): StyledComponent<T> {
        require(supportedTags.contains(tag)) { "不支持的标签类型: $tag" }
        return StyledComponent(tag, styles)
    }

    /** 预定义常用标签的Styled组件 */
    val div = styled<View>("div")
    val button = styled<View>("button")
    val span = styled<View>("span")
    val input = styled<View>("input")
    val img = styled<View>("img")
    val svg = styled<View>("svg")
}

/** Styled组件封装 */
class StyledComponent<T : View>(
    private val tag: String,
    private val styles: Array<out Any>
) {
    /** 应用额外样式并创建组件实例 */
    fun apply(vararg extraStyles: Any): StyledComponent<T> {
        val mergedStyles = styles + extraStyles
        return StyledComponent(tag, mergedStyles)
    }

    /** 创建组件实例并应用样式 */
    fun create(context: Context, props: Map<String, Any> = emptyMap()): T {
        // 1. 序列化样式
        val serialized = EmotionSerialize.serializeStyles(styles)
        // 2. 获取或创建样式缓存
        val cache = EmotionCacheManager.getCache(context)
        // 3. 插入样式到缓存
        cache.insert(null, serialized)
        // 4. 创建组件并应用样式（实际项目中需绑定样式到View）
        val view = ViewFactory.createView(context, tag) as T
        view.setStyle(serialized.name)
        return view
    }
}

/** 样式缓存管理器（单例） */
object EmotionCacheManager {
    private val caches = mutableMapOf<Context, EmotionCache>()

    fun getCache(context: Context): EmotionCache {
        return caches.getOrPut(context) {
            EmotionCache(context)
        }
    }

    fun clearCache(context: Context) {
        caches.remove(context)?.clear()
    }
}

/** View创建工厂 */
object ViewFactory {
    fun createView(context: Context, tag: String): View {
        return when (tag) {
            "div" -> View(context).apply { layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) }
            "button" -> android.widget.Button(context)
            "input" -> android.widget.EditText(context)
            "img" -> android.widget.ImageView(context)
            else -> View(context)
        }
    }
}

/** View扩展函数：设置Emotion样式类名 */
fun View.setStyle(styleName: String) {
    tag = "emotion-$styleName"
    // 实际项目中需根据样式名应用对应的CSS样式
}

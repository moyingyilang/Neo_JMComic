package com.yourpackage.neojmcomic.ui.components.icons

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.yourpackage.neojmcomic.utils.theme.ThemeProvider
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * SVG图标核心渲染组件，支持颜色、尺寸、无障碍配置
 */
open class SvgIcon @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ImageView(context, attrs) {
    var displayName: String = "SvgIcon"
    var color: IconColor = IconColor.INHERIT
        set(value) {
            field = value
            updateIconColor()
        }
    var fontSize: IconFontSize = IconFontSize.MEDIUM
        set(value) {
            field = value
            updateIconSize()
        }

    // 图标样式类（对应svgIconClasses.txt）
    private val iconClasses = SvgIconClasses.generate()

    init {
        initAttrs(attrs)
        setupDefaultStyle()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.SvgIcon)
            // 解析颜色属性
            val colorInt = ta.getInt(R.styleable.SvgIcon_iconColor, 0)
            color = IconColor.values()[colorInt]
            // 解析尺寸属性
            val fontSizeInt = ta.getInt(R.styleable.SvgIcon_fontSize, 1)
            fontSize = IconFontSize.values()[fontSizeInt]
            // 解析无障碍描述
            val contentDesc = ta.getString(R.styleable.SvgIcon_contentDescription)
            contentDescription = contentDesc ?: displayName
            ta.recycle()
        }
    }

    private fun setupDefaultStyle() {
        scaleType = ScaleType.FIT_CENTER
        adjustViewBounds = true
        // 应用基础样式
        setBackgroundColor(context.getColor(android.R.color.transparent))
        updateIconColor()
        updateIconSize()
    }

    /** 设置SVG图片资源 */
    fun setPictureDrawable(drawable: PictureDrawable) {
        setImageDrawable(drawable)
    }

    /** 更新图标颜色 */
    private fun updateIconColor() {
        val theme = ThemeProvider.getTheme()
        val textColor = when (color) {
            IconColor.PRIMARY -> theme.palette.primary.main
            IconColor.SECONDARY -> theme.palette.secondary.main
            IconColor.ACTION -> theme.palette.action.active
            IconColor.ERROR -> theme.palette.error.main
            IconColor.DISABLED -> theme.palette.action.disabled
            IconColor.INHERIT -> context.getColor(android.R.color.darker_gray)
            IconColor.SUCCESS -> theme.palette.success.main
            IconColor.INFO -> theme.palette.info.main
            IconColor.WARNING -> theme.palette.warning.main
        }
        setColorFilter(textColor)
    }

    /** 更新图标尺寸 */
    private fun updateIconSize() {
        val size = when (fontSize) {
            IconFontSize.SMALL -> 20.dpToPx()
            IconFontSize.MEDIUM -> 24.dpToPx()
            IconFontSize.LARGE -> 35.dpToPx()
            IconFontSize.INHERIT -> layoutParams.width.takeIf { it > 0 } ?: 24.dpToPx()
        }
        layoutParams = LayoutParams(size, size)
    }

    /** 图标颜色枚举 */
    enum class IconColor {
        INHERIT, PRIMARY, SECONDARY, ACTION, ERROR, DISABLED, SUCCESS, INFO, WARNING
    }

    /** 图标尺寸枚举 */
    enum class IconFontSize {
        INHERIT, SMALL, MEDIUM, LARGE
    }

    /** 尺寸转换扩展函数 */
    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

/** SVG图标样式类生成（对应svgIconClasses.txt） */
object SvgIconClasses {
    fun generate(): Map<String, String> {
        return mapOf(
            "root" to StyleUtility.generateUtilityClass("MuiSvgIcon", "root"),
            "colorPrimary" to StyleUtility.generateUtilityClass("MuiSvgIcon", "colorPrimary"),
            "colorSecondary" to StyleUtility.generateUtilityClass("MuiSvgIcon", "colorSecondary"),
            "colorAction" to StyleUtility.generateUtilityClass("MuiSvgIcon", "colorAction"),
            "colorError" to StyleUtility.generateUtilityClass("MuiSvgIcon", "colorError"),
            "colorDisabled" to StyleUtility.generateUtilityClass("MuiSvgIcon", "colorDisabled"),
            "fontSizeInherit" to StyleUtility.generateUtilityClass("MuiSvgIcon", "fontSizeInherit"),
            "fontSizeSmall" to StyleUtility.generateUtilityClass("MuiSvgIcon", "fontSizeSmall"),
            "fontSizeMedium" to StyleUtility.generateUtilityClass("MuiSvgIcon", "fontSizeMedium"),
            "fontSizeLarge" to StyleUtility.generateUtilityClass("MuiSvgIcon", "fontSizeLarge")
        )
    }
}

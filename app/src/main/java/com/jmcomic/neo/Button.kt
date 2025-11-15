package com.yourpackage.neojmcomic.ui.components.button

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.yourpackage.neojmcomic.ui.components.icons.SvgIcon
import com.yourpackage.neojmcomic.utils.theme.ThemeProvider
import com.yourpackage.neojmcomic.utils.theme.StyleUtility
import com.yourpackage.neojmcomic.utils.capitalize

/**
 * 高级按钮组件，支持变体、颜色、尺寸、图标等特性
 */
class Button @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ButtonBase(context, attrs) {
    var variant: ButtonVariant = ButtonVariant.TEXT
        set(value) {
            field = value
            updateButtonStyle()
        }
    var color: ButtonColor = ButtonColor.PRIMARY
        set(value) {
            field = value
            updateButtonStyle()
        }
    var size: ButtonSize = ButtonSize.MEDIUM
        set(value) {
            field = value
            updateButtonSize()
        }
    var fullWidth: Boolean = false
        set(value) {
            field = value
            updateButtonWidth()
        }
    var disableElevation: Boolean = false
        set(value) {
            field = value
            updateButtonElevation()
        }
    private var startIcon: SvgIcon? = null
    private var endIcon: SvgIcon? = null
    private val labelView = TextView(context)
    private val buttonClasses = ButtonClasses.generate()

    init {
        initAttrs(attrs)
        setupButtonStructure()
        updateButtonStyle()
        updateButtonSize()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.Button)
            // 解析变体
            val variantInt = ta.getInt(R.styleable.Button_variant, 2)
            variant = ButtonVariant.values()[variantInt]
            // 解析颜色
            val colorInt = ta.getInt(R.styleable.Button_color, 0)
            color = ButtonColor.values()[colorInt]
            // 解析尺寸
            val sizeInt = ta.getInt(R.styleable.Button_size, 1)
            size = ButtonSize.values()[sizeInt]
            // 其他属性
            fullWidth = ta.getBoolean(R.styleable.Button_fullWidth, false)
            disableElevation = ta.getBoolean(R.styleable.Button_disableElevation, false)
            val text = ta.getString(R.styleable.Button_text) ?: ""
            setText(text)
            ta.recycle()
        }
    }

    private fun setupButtonStructure() {
        // 水平布局容器
        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        // 添加图标和文本
        startIcon?.let { container.addView(it) }
        container.addView(labelView)
        endIcon?.let { container.addView(it) }
        addView(container)

        // 文本样式基础配置
        labelView.gravity = android.view.Gravity.CENTER
        labelView.setTextColor(context.getColor(android.R.color.white))
    }

    /** 设置按钮文本 */
    fun setText(text: String) {
        labelView.text = text
    }

    /** 设置左侧图标 */
    fun setStartIcon(icon: SvgIcon) {
        startIcon = icon
        (getChildAt(0) as FrameLayout).addView(icon, 0)
        updateIconMargin()
    }

    /** 设置右侧图标 */
    fun setEndIcon(icon: SvgIcon) {
        endIcon = icon
        (getChildAt(0) as FrameLayout).addView(icon)
        updateIconMargin()
    }

    private fun updateButtonStyle() {
        val theme = ThemeProvider.getTheme()
        val (bgColor, textColor, elevation) = when (variant) {
            ButtonVariant.CONTAINED -> {
                val paletteColor = when (color) {
                    ButtonColor.PRIMARY -> theme.palette.primary
                    ButtonColor.SECONDARY -> theme.palette.secondary
                    ButtonColor.SUCCESS -> theme.palette.success
                    ButtonColor.ERROR -> theme.palette.error
                    ButtonColor.INFO -> theme.palette.info
                    ButtonColor.WARNING -> theme.palette.warning
                    ButtonColor.INHERIT -> theme.palette.action
                }
                Triple(paletteColor.main, paletteColor.contrastText, if (disableElevation) 0f else 2f)
            }
            ButtonVariant.OUTLINED -> {
                val borderColor = when (color) {
                    ButtonColor.PRIMARY -> theme.palette.primary.main
                    else -> theme.palette.onSurface
                }
                setBorder(borderColor)
                Triple(context.getColor(android.R.color.transparent), theme.palette.onSurface, 0f)
            }
            ButtonVariant.TEXT -> {
                val textColor = when (color) {
                    ButtonColor.PRIMARY -> theme.palette.primary.main
                    else -> theme.palette.onSurface
                }
                Triple(context.getColor(android.R.color.transparent), textColor, 0f)
            }
        }

        // 应用样式
        setBackgroundColor(bgColor)
        labelView.setTextColor(textColor)
        elevation = elevation
        // 应用样式类
        val styleClass = "${buttonClasses["root"]} ${buttonClasses[variant.name.lowercase()]} ${buttonClasses["color${capitalize(color.name)}"]}"
        setTag(R.id.button_class, styleClass)
    }

    private fun updateButtonSize() {
        val (padding, textSize) = when (size) {
            ButtonSize.SMALL -> Triple(4.dpToPx(), 13f)
            ButtonSize.MEDIUM -> Triple(6.dpToPx(), 14f)
            ButtonSize.LARGE -> Triple(8.dpToPx(), 15f)
        }
        setPadding(padding, padding, padding, padding)
        labelView.textSize = textSize
        startIcon?.fontSize = SvgIcon.IconFontSize.SMALL
        endIcon?.fontSize = SvgIcon.IconFontSize.SMALL
    }

    private fun updateButtonWidth() {
        layoutParams.width = if (fullWidth) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
    }

    private fun updateButtonElevation() {
        elevation = if (disableElevation) 0f else 2f
    }

    private fun updateIconMargin() {
        val margin = 8.dpToPx()
        startIcon?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            rightMargin = margin
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        endIcon?.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = margin
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
    }

    private fun setBorder(color: Int) {
        background = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setStroke(1, color)
            cornerRadius = 4.dpToPx().toFloat()
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    /** 按钮变体枚举 */
    enum class ButtonVariant {
        CONTAINED, OUTLINED, TEXT
    }

    /** 按钮颜色枚举 */
    enum class ButtonColor {
        PRIMARY, SECONDARY, SUCCESS, ERROR, INFO, WARNING, INHERIT
    }

    /** 按钮尺寸枚举 */
    enum class ButtonSize {
        SMALL, MEDIUM, LARGE
    }
}

/** Button样式类生成（对应buttonClasses.txt） */
object ButtonClasses {
    fun generate(): Map<String, String> {
        val baseClasses = mutableMapOf(
            "root" to StyleUtility.generateUtilityClass("MuiButton", "root"),
            "text" to StyleUtility.generateUtilityClass("MuiButton", "text"),
            "outlined" to StyleUtility.generateUtilityClass("MuiButton", "outlined"),
            "contained" to StyleUtility.generateUtilityClass("MuiButton", "contained"),
            "disableElevation" to StyleUtility.generateUtilityClass("MuiButton", "disableElevation"),
            "focusVisible" to StyleUtility.generateUtilityClass("MuiButton", "focusVisible"),
            "disabled" to StyleUtility.generateUtilityClass("MuiButton", "disabled"),
            "fullWidth" to StyleUtility.generateUtilityClass("MuiButton", "fullWidth"),
            "startIcon" to StyleUtility.generateUtilityClass("MuiButton", "startIcon"),
            "endIcon" to StyleUtility.generateUtilityClass("MuiButton", "endIcon"),
            "icon" to StyleUtility.generateUtilityClass("MuiButton", "icon")
        )

        // 添加颜色相关样式类
        listOf("Primary", "Secondary", "Success", "Error", "Info", "Warning", "Inherit").forEach { color ->
            baseClasses["color$color"] = StyleUtility.generateUtilityClass("MuiButton", "color$color")
            baseClasses["text$color"] = StyleUtility.generateUtilityClass("MuiButton", "text$color")
            baseClasses["outlined$color"] = StyleUtility.generateUtilityClass("MuiButton", "outlined$color")
            baseClasses["contained$color"] = StyleUtility.generateUtilityClass("MuiButton", "contained$color")
        }

        // 添加尺寸相关样式类
        listOf("Small", "Medium", "Large").forEach { size ->
            baseClasses["size$size"] = StyleUtility.generateUtilityClass("MuiButton", "size$size")
            baseClasses["textSize$size"] = StyleUtility.generateUtilityClass("MuiButton", "textSize$size")
            baseClasses["outlinedSize$size"] = StyleUtility.generateUtilityClass("MuiButton", "outlinedSize$size")
            baseClasses["containedSize$size"] = StyleUtility.generateUtilityClass("MuiButton", "containedSize$size")
            baseClasses["iconSize$size"] = StyleUtility.generateUtilityClass("MuiButton", "iconSize$size")
        }

        return baseClasses
    }
}

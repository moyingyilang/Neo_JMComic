package com.yourpackage.neojmcomic.ui.components.form

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.yourpackage.neojmcomic.utils.theme.ThemeProvider
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * 基础输入框组件，支持下划线、禁用、错误状态等特性
 */
class Input @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : InputBase(context, attrs) {
    var disableUnderline: Boolean = false
        set(value) {
            field = value
            updateUnderlineVisibility()
        }
    private val underlineView = View(context)
    private val inputClasses = InputClasses.generate()

    init {
        initAttrs(attrs)
        setupUnderline()
        updateUnderlineStyle()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.Input)
            disableUnderline = ta.getBoolean(R.styleable.Input_disableUnderline, false)
            ta.recycle()
        }
    }

    private fun setupUnderline() {
        // 下划线布局（底部对齐）
        underlineView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            1.dpToPx()
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }
        (getChildAt(0) as FrameLayout).addView(underlineView)
    }

    private fun updateUnderlineVisibility() {
        underlineView.visibility = if (disableUnderline) View.GONE else View.VISIBLE
    }

    override fun updateErrorStyle() {
        super.updateErrorStyle()
        updateUnderlineStyle()
    }

    /** 更新下划线样式（颜色、状态） */
    private fun updateUnderlineStyle() {
        val theme = ThemeProvider.getTheme()
        val underlineColor = when {
            error -> context.getColor(R.color.error)
            inputView.isFocused -> theme.palette.primary.main
            else -> theme.palette.divider
        }
        underlineView.setBackgroundColor(underlineColor)
    }

    /** 重写焦点变化处理，更新下划线样式 */
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) {
            inputView.requestFocus()
        }
        updateUnderlineStyle()
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

/** Input样式类生成（对应inputClasses.txt） */
object InputClasses {
    fun generate(): Map<String, String> {
        // 继承InputBase样式类
        val baseClasses = InputBaseClasses.generate().toMutableMap()
        // 添加Input专属样式类
        baseClasses["root"] = StyleUtility.generateUtilityClass("MuiInput", "root")
        baseClasses["underline"] = StyleUtility.generateUtilityClass("MuiInput", "underline")
        baseClasses["input"] = StyleUtility.generateUtilityClass("MuiInput", "input")
        return baseClasses
    }
}

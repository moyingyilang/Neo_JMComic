package com.yourpackage.neojmcomic.ui.components.dialog

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.yourpackage.neojmcomic.utils.theme.ThemeProvider
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * 对话框内容区组件，承载核心内容并支持分隔线
 */
class DialogContent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var dividers: Boolean = false
        set(value) {
            field = value
            updateDividersStyle()
        }
    private val contentClasses = DialogContentClasses.generate()

    init {
        initAttrs(attrs)
        setupContentLayout()
        updateDividersStyle()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.DialogContent)
            dividers = ta.getBoolean(R.styleable.DialogContent_dividers, false)
            ta.recycle()
        }
    }

    private fun setupContentLayout() {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        setPadding(20.dpToPx(), 20.dpToPx(), 20.dpToPx(), 20.dpToPx())
        // 应用样式类
        setTag(R.id.dialog_content_class, contentClasses["root"])
    }

    private fun updateDividersStyle() {
        val theme = ThemeProvider.getTheme()
        if (dividers) {
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                setStroke(1, theme.palette.divider)
            }
        } else {
            setPadding(20.dpToPx(), 20.dpToPx(), 20.dpToPx(), 20.dpToPx())
            background = null
        }
    }

    /** 设置内容视图 */
    fun setContent(view: android.view.View) {
        removeAllViews()
        addView(view)
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

/** DialogContent样式类生成 */
object DialogContentClasses {
    fun generate(): Map<String, String> {
        return mapOf(
            "root" to StyleUtility.generateUtilityClass("MuiDialogContent", "root"),
            "dividers" to StyleUtility.generateUtilityClass("MuiDialogContent", "dividers")
        )
    }
}

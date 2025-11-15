package com.yourpackage.neojmcomic.ui.components.dialog

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.yourpackage.neojmcomic.ui.components.button.Button
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * 对话框操作区组件，统一管理底部操作按钮布局
 */
class DialogActions @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var disableSpacing: Boolean = false
        set(value) {
            field = value
            updateButtonSpacing()
        }
    private val actionClasses = DialogActionClasses.generate()

    init {
        initAttrs(attrs)
        setupActionLayout()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.DialogActions)
            disableSpacing = ta.getBoolean(R.styleable.DialogActions_disableSpacing, false)
            ta.recycle()
        }
    }

    private fun setupActionLayout() {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
        // 水平布局，右对齐
        gravity = android.view.Gravity.END
        // 应用样式类
        setTag(R.id.dialog_actions_class, actionClasses["root"])
    }

    /** 添加操作按钮 */
    fun addActionButton(button: Button) {
        addView(button)
        updateButtonSpacing()
    }

    /** 添加多个操作按钮 */
    fun addActionButtons(vararg buttons: Button) {
        buttons.forEach { addView(it) }
        updateButtonSpacing()
    }

    private fun updateButtonSpacing() {
        if (disableSpacing) return

        val spacing = 8.dpToPx()
        for (i in 1 until childCount) {
            val button = getChildAt(i) as Button
            val params = button.layoutParams as FrameLayout.LayoutParams
            params.leftMargin = spacing
            button.layoutParams = params
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

/** DialogActions样式类生成 */
object DialogActionClasses {
    fun generate(): Map<String, String> {
        return mapOf(
            "root" to StyleUtility.generateUtilityClass("MuiDialogActions", "root"),
            "spacing" to StyleUtility.generateUtilityClass("MuiDialogActions", "spacing")
        )
    }
}

package com.yourpackage.neojmcomic.ui.components.button

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.yourpackage.neojmcomic.ui.components.ripple.TouchRipple
import com.yourpackage.neojmcomic.utils.ref.UseForkRef
import com.yourpackage.neojmcomic.utils.event.UseEventCallback
import com.yourpackage.neojmcomic.utils.focus.IsFocusVisible
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * 按钮基础组件，提供统一的点击、波纹、焦点交互逻辑
 */
open class ButtonBase @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var disabled: Boolean = false
        set(value) {
            field = value
            isClickable = !value
            isFocusable = !value
            alpha = if (value) 0.5f else 1f
        }
    var disableRipple: Boolean = false
    var disableTouchRipple: Boolean = false
    var focusRipple: Boolean = false
    var centerRipple: Boolean = false
    var focusVisibleClassName: String? = null
    var onFocusVisible: ((MotionEvent) -> Unit)? = null

    private val buttonRef = View.OnAttachStateChangeListener { isAttached ->
        if (isAttached) setupRipple()
    }
    private val ripple = TouchRipple(context)
    private var focusVisible: Boolean = false
    private val buttonClasses = ButtonBaseClasses.generate()

    init {
        initAttrs(attrs)
        setupBaseStyle()
        addOnAttachStateChangeListener(buttonRef)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.ButtonBase)
            disabled = ta.getBoolean(R.styleable.ButtonBase_disabled, false)
            disableRipple = ta.getBoolean(R.styleable.ButtonBase_disableRipple, false)
            disableTouchRipple = ta.getBoolean(R.styleable.ButtonBase_disableTouchRipple, false)
            focusRipple = ta.getBoolean(R.styleable.ButtonBase_focusRipple, false)
            centerRipple = ta.getBoolean(R.styleable.ButtonBase_centerRipple, false)
            focusVisibleClassName = ta.getString(R.styleable.ButtonBase_focusVisibleClassName)
            ta.recycle()
        }
    }

    private fun setupBaseStyle() {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(context.getColor(android.R.color.transparent))
        // 应用基础样式类
        setTag(R.id.button_base_class, buttonClasses["root"])
    }

    private fun setupRipple() {
        ripple.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        ripple.isClickable = false
        addView(ripple)
    }

    // 触摸事件处理（波纹控制）
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (disabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!disableTouchRipple) ripple.start(event, centerRipple)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!disableTouchRipple) ripple.stop(event)
            }
        }
        return super.onTouchEvent(event)
    }

    // 焦点事件处理
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        focusVisible = gainFocus && IsFocusVisible.isFocusVisible(this)
        if (focusVisible) {
            onFocusVisible?.invoke(MotionEvent.obtain(System.currentTimeMillis(), System.currentTimeMillis(), MotionEvent.ACTION_FOCUS, 0f, 0f, 0))
            if (focusRipple && !disableRipple) ripple.pulsate()
            focusVisibleClassName?.let { setTag(R.id.focus_visible_class, it) }
        } else {
            focusVisibleClassName?.let { removeTag(R.id.focus_visible_class) }
        }
    }

    // 点击事件拦截（禁用状态处理）
    override fun performClick(): Boolean {
        return if (disabled) false else super.performClick()
    }
}

/** ButtonBase样式类生成（对应buttonBaseClasses.txt） */
object ButtonBaseClasses {
    fun generate(): Map<String, String> {
        return mapOf(
            "root" to StyleUtility.generateUtilityClass("MuiButtonBase", "root"),
            "disabled" to StyleUtility.generateUtilityClass("MuiButtonBase", "disabled"),
            "focusVisible" to StyleUtility.generateUtilityClass("MuiButtonBase", "focusVisible")
        )
    }
}

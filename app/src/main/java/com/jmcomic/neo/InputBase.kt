package com.yourpackage.neojmcomic.ui.components.form

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import com.yourpackage.neojmcomic.ui.components.text.TextareaAutosize
import com.yourpackage.neojmcomic.utils.form.FormControlContext
import com.yourpackage.neojmcomic.utils.form.UseFormControl
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * 输入框基础组件，提供输入、焦点、自动填充等核心逻辑
 */
open class InputBase @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var value: String? = null
        set(value) {
            field = value
            inputView.setText(value ?: "")
        }
    var disabled: Boolean = false
        set(value) {
            field = value
            inputView.isEnabled = !value
            alpha = if (value) 0.5f else 1f
        }
    var error: Boolean = false
        set(value) {
            field = value
            updateErrorStyle()
        }
    var multiline: Boolean = false
        set(value) {
            field = value
            updateInputType()
        }
    var fullWidth: Boolean = false
        set(value) {
            field = value
            updateInputWidth()
        }
    var placeholder: String? = null
        set(value) {
            field = value
            inputView.hint = value
        }
    var readOnly: Boolean = false
        set(value) {
            field = value
            inputView.isFocusable = !value
            inputView.isClickable = !value
        }
    var startAdornment: View? = null
    var endAdornment: View? = null
    protected lateinit var inputView: EditText
    private val inputClasses = InputBaseClasses.generate()
    private val formControl = UseFormControl(this)

    init {
        initAttrs(attrs)
        setupInputView()
        setupInputStructure()
        updateInputWidth()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.InputBase)
            disabled = ta.getBoolean(R.styleable.InputBase_disabled, false)
            error = ta.getBoolean(R.styleable.InputBase_error, false)
            multiline = ta.getBoolean(R.styleable.InputBase_multiline, false)
            fullWidth = ta.getBoolean(R.styleable.InputBase_fullWidth, false)
            readOnly = ta.getBoolean(R.styleable.InputBase_readOnly, false)
            placeholder = ta.getString(R.styleable.InputBase_placeholder)
            val text = ta.getString(R.styleable.InputBase_text) ?: ""
            value = text
            ta.recycle()
        }
    }

    private fun setupInputView() {
        // 根据是否多行选择输入组件
        inputView = if (multiline) {
            TextareaAutosize(context).apply {
                isSingleLine = false
                minLines = 3
            }
        } else {
            EditText(context).apply {
                isSingleLine = true
            }
        }

        // 基础样式配置
        inputView.setBackgroundColor(context.getColor(android.R.color.transparent))
        inputView.setTextColor(context.getColor(android.R.color.darker_gray))
        inputView.hintTextColor = context.getColor(android.R.color.darker_gray).let {
            android.graphics.Color.argb(107, android.graphics.Color.red(it), android.graphics.Color.green(it), android.graphics.Color.blue(it))
        }
    }

    private fun setupInputStructure() {
        val container = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        // 添加前缀、输入框、后缀
        startAdornment?.let { container.addView(it) }
        container.addView(inputView)
        endAdornment?.let { container.addView(it) }
        addView(container)

        // 应用样式类
        setTag(R.id.input_base_class, inputClasses["root"])
    }

    private fun updateInputType() {
        if (multiline) {
            val textarea = TextareaAutosize(context).apply {
                isSingleLine = false
                minLines = 3
                setText(value ?: "")
                hint = placeholder
                isEnabled = !disabled
            }
            (getChildAt(0) as FrameLayout).removeViewAt(if (startAdornment != null) 1 else 0)
            (getChildAt(0) as FrameLayout).addView(textarea, if (startAdornment != null) 1 else 0)
            inputView = textarea
        } else {
            val editText = EditText(context).apply {
                isSingleLine = true
                setText(value ?: "")
                hint = placeholder
                isEnabled = !disabled
            }
            (getChildAt(0) as FrameLayout).removeViewAt(if (startAdornment != null) 1 else 0)
            (getChildAt(0) as FrameLayout).addView(editText, if (startAdornment != null) 1 else 0)
            inputView = editText
        }
    }

    private fun updateInputWidth() {
        layoutParams.width = if (fullWidth) LayoutParams.MATCH_PARENT else LayoutParams.WRAP_CONTENT
    }

    private fun updateErrorStyle() {
        inputView.setTextColor(if (error) {
            context.getColor(R.color.error)
        } else {
            context.getColor(android.R.color.darker_gray)
        })
    }

    /** 获取输入值 */
    fun getValue(): String = inputView.text.toString()

    /** 设置输入变化监听 */
    fun setOnValueChangeListener(listener: (String) -> Unit) {
        inputView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                listener(s?.toString() ?: "")
            }
        })
    }
}

/** InputBase样式类生成（对应inputBaseClasses.txt） */
object InputBaseClasses {
    fun generate(): Map<String, String> {
        return mapOf(
            "root" to StyleUtility.generateUtilityClass("MuiInputBase", "root"),
            "formControl" to StyleUtility.generateUtilityClass("MuiInputBase", "formControl"),
            "focused" to StyleUtility.generateUtilityClass("MuiInputBase", "focused"),
            "disabled" to StyleUtility.generateUtilityClass("MuiInputBase", "disabled"),
            "adornedStart" to StyleUtility.generateUtilityClass("MuiInputBase", "adornedStart"),
            "adornedEnd" to StyleUtility.generateUtilityClass("MuiInputBase", "adornedEnd"),
            "error" to StyleUtility.generateUtilityClass("MuiInputBase", "error"),
            "sizeSmall" to StyleUtility.generateUtilityClass("MuiInputBase", "sizeSmall"),
            "multiline" to StyleUtility.generateUtilityClass("MuiInputBase", "multiline"),
            "colorSecondary" to StyleUtility.generateUtilityClass("MuiInputBase", "colorSecondary"),
            "fullWidth" to StyleUtility.generateUtilityClass("MuiInputBase", "fullWidth"),
            "hiddenLabel" to StyleUtility.generateUtilityClass("MuiInputBase", "hiddenLabel"),
            "readOnly" to StyleUtility.generateUtilityClass("MuiInputBase", "readOnly"),
            "input" to StyleUtility.generateUtilityClass("MuiInputBase", "input"),
            "inputSizeSmall" to StyleUtility.generateUtilityClass("MuiInputBase", "inputSizeSmall"),
            "inputMultiline" to StyleUtility.generateUtilityClass("MuiInputBase", "inputMultiline"),
            "inputTypeSearch" to StyleUtility.generateUtilityClass("MuiInputBase", "inputTypeSearch"),
            "inputAdornedStart" to StyleUtility.generateUtilityClass("MuiInputBase", "inputAdornedStart"),
            "inputAdornedEnd" to StyleUtility.generateUtilityClass("MuiInputBase", "inputAdornedEnd"),
            "inputHiddenLabel" to StyleUtility.generateUtilityClass("MuiInputBase", "inputHiddenLabel")
        )
    }
}

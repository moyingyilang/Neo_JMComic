package com.yourpackage.neojmcomic.ui.components.dialog

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.FadeAnimation
import android.widget.FrameLayout
import com.yourpackage.neojmcomic.ui.components.paper.Paper
import com.yourpackage.neojmcomic.ui.components.backdrop.Backdrop
import com.yourpackage.neojmcomic.utils.theme.StyleUtility

/**
 * 对话框容器组件，支持模态、过渡、全屏、滚动等特性
 */
class Dialog @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var open: Boolean = false
        set(value) {
            field = value
            updateDialogVisibility()
        }
    var fullScreen: Boolean = false
        set(value) {
            field = value
            updateDialogSize()
        }
    var fullWidth: Boolean = false
        set(value) {
            field = value
            updateDialogSize()
        }
    var maxWidth: DialogMaxWidth = DialogMaxWidth.SM
        set(value) {
            field = value
            updateDialogSize()
        }
    var scroll: DialogScroll = DialogScroll.PAPER
        set(value) {
            field = value
            updateScrollMode()
        }
    var onClose: ((reason: String) -> Unit)? = null
    private val backdrop = Backdrop(context)
    private val dialogContainer = FrameLayout(context)
    private val dialogPaper = Paper(context)
    private val dialogClasses = DialogClasses.generate()
    private val fadeAnimation = FadeAnimation(0f, 1f).apply {
        duration = 300
    }

    init {
        initAttrs(attrs)
        setupDialogStructure()
        setupBackdropListener()
        updateDialogSize()
        updateScrollMode()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.Dialog)
            open = ta.getBoolean(R.styleable.Dialog_open, false)
            fullScreen = ta.getBoolean(R.styleable.Dialog_fullScreen, false)
            fullWidth = ta.getBoolean(R.styleable.Dialog_fullWidth, false)
            val maxWidthInt = ta.getInt(R.styleable.Dialog_maxWidth, 1)
            maxWidth = DialogMaxWidth.values()[maxWidthInt]
            val scrollInt = ta.getInt(R.styleable.Dialog_scroll, 0)
            scroll = DialogScroll.values()[scrollInt]
            ta.recycle()
        }
    }

    private fun setupDialogStructure() {
        // 层级：背景遮罩 -> 容器 -> 面板
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(backdrop)

        // 对话框容器（居中对齐）
        dialogContainer.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        addView(dialogContainer)

        // 面板（承载内容）
        dialogPaper.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        dialogContainer.addView(dialogPaper)

        // 应用样式类
        setTag(R.id.dialog_class, dialogClasses["root"])
        dialogContainer.tag = dialogClasses["container"]
        dialogPaper.tag = dialogClasses["paper"]
    }

    private fun setupBackdropListener() {
        backdrop.setOnClickListener {
            onClose?.invoke("backdropClick")
            open = false
        }
    }

    private fun updateDialogVisibility() {
        visibility = if (open) View.VISIBLE else View.GONE
        if (open) {
            dialogContainer.startAnimation(fadeAnimation)
        } else {
            val fadeOut = FadeAnimation(1f, 0f).apply {
                duration = 300
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        visibility = View.GONE
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                })
            }
            dialogContainer.startAnimation(fadeOut)
        }
    }

    private fun updateDialogSize() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        val margin = 32.dpToPx()

        if (fullScreen) {
            dialogPaper.layoutParams = FrameLayout.LayoutParams(
                screenWidth,
                screenHeight
            )
            dialogPaper.setPadding(0, 0, 0, 0)
            return
        }

        // 计算最大宽度
        val maxWidthPx = when (maxWidth) {
            DialogMaxWidth.XS -> Math.max(320.dpToPx(), screenWidth * 0.7f.toInt())
            DialogMaxWidth.SM -> Math.max(600.dpToPx(), screenWidth * 0.8f.toInt())
            DialogMaxWidth.MD -> Math.max(960.dpToPx(), screenWidth * 0.85f.toInt())
            DialogMaxWidth.LG -> Math.max(1280.dpToPx(), screenWidth * 0.9f.toInt())
            DialogMaxWidth.XL -> Math.max(1920.dpToPx(), screenWidth * 0.95f.toInt())
            DialogMaxWidth.FALSE -> screenWidth - 2 * margin
        }

        val width = if (fullWidth) screenWidth - 2 * margin else maxWidthPx
        val height = FrameLayout.LayoutParams.WRAP_CONTENT

        dialogPaper.layoutParams = FrameLayout.LayoutParams(width, height)
        dialogPaper.setPadding(margin, margin, margin, margin)
    }

    private fun updateScrollMode() {
        when (scroll) {
            DialogScroll.PAPER -> {
                dialogPaper.isVerticalScrollBarEnabled = true
                dialogPaper.verticalScrollbarPosition = View.SCROLLBAR_POSITION_RIGHT
            }
            DialogScroll.BODY -> {
                dialogContainer.isVerticalScrollBarEnabled = true
                dialogContainer.verticalScrollbarPosition = View.SCROLLBAR_POSITION_RIGHT
            }
        }
    }

    /** 设置对话框内容 */
    fun setContent(view: View) {
        dialogPaper.removeAllViews()
        dialogPaper.addView(view)
    }

    /** 设置对话框标题 */
    fun setTitle(title: String) {
        // 可扩展：添加标题TextView
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    /** 对话框最大宽度枚举 */
    enum class DialogMaxWidth {
        XS, SM, MD, LG, XL, FALSE
    }

    /** 对话框滚动模式枚举 */
    enum class DialogScroll {
        PAPER, BODY
    }
}

/** Dialog样式类生成（对应dialogClasses.txt） */
object DialogClasses {
    fun generate(): Map<String, String> {
        return mapOf(
            "root" to StyleUtility.generateUtilityClass("MuiDialog", "root"),
            "scrollPaper" to StyleUtility.generateUtilityClass("MuiDialog", "scrollPaper"),
            "scrollBody" to StyleUtility.generateUtilityClass("MuiDialog", "scrollBody"),
            "container" to StyleUtility.generateUtilityClass("MuiDialog", "container"),
            "paper" to StyleUtility.generateUtilityClass("MuiDialog", "paper"),
            "paperScrollPaper" to StyleUtility.generateUtilityClass("MuiDialog", "paperScrollPaper"),
            "paperScrollBody" to StyleUtility.generateUtilityClass("MuiDialog", "paperScrollBody"),
            "paperWidthFalse" to StyleUtility.generateUtilityClass("MuiDialog", "paperWidthFalse"),
            "paperWidthXs" to StyleUtility.generateUtilityClass("MuiDialog", "paperWidthXs"),
            "paperWidthSm" to StyleUtility.generateUtilityClass("MuiDialog", "paperWidthSm"),
            "paperWidthMd" to StyleUtility.generateUtilityClass("MuiDialog", "paperWidthMd"),
            "paperWidthLg" to StyleUtility.generateUtilityClass("MuiDialog", "paperWidthLg"),
            "paperWidthXl" to StyleUtility.generateUtilityClass("MuiDialog", "paperWidthXl"),
            "paperFullWidth" to StyleUtility.generateUtilityClass("MuiDialog", "paperFullWidth"),
            "paperFullScreen" to StyleUtility.generateUtilityClass("MuiDialog", "paperFullScreen")
        )
    }
}

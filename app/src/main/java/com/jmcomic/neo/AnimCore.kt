package com.yourpackage.neojmcomic.utils.anim

import android.animation.ValueAnimator
import android.view.View
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 动画值管理（适配motion-value） */
class AnimValue(initialValue: Float = 0f) {
    private val _value = MutableStateFlow(initialValue)
    val value: StateFlow<Float> = _value

    var current: Float
        get() = _value.value
        set(value) {
            _value.value = value
        }

    /** 绑定动画到值 */
    fun bindToAnimator(animator: ValueAnimator) {
        animator.addUpdateListener { anim ->
            current = anim.animatedValue as Float
        }
    }
}

/** 动画核心工具（适配animations） */
object AnimCore {
    /** 创建基础值动画 */
    fun createAnimator(
        from: Float,
        to: Float,
        duration: Long = 300,
        interpolator: android.view.animation.Interpolator = android.view.animation.DecelerateInterpolator()
    ): ValueAnimator {
        return ValueAnimator.ofFloat(from, to).apply {
            this.duration = duration
            this.interpolator = interpolator
        }
    }

    /** 执行View属性动画 */
    fun animateView(
        view: View,
        property: String,
        from: Float,
        to: Float,
        duration: Long = 300,
        onEnd: (() -> Unit)? = null
    ) {
        createAnimator(from, to, duration).apply {
            addUpdateListener { anim ->
                when (property) {
                    "alpha" -> view.alpha = anim.animatedValue as Float
                    "scaleX" -> view.scaleX = anim.animatedValue as Float
                    "scaleY" -> view.scaleY = anim.animatedValue as Float
                    "translationX" -> view.translationX = anim.animatedValue as Float
                    "translationY" -> view.translationY = anim.animatedValue as Float
                }
            }
            onEnd?.let {
                addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationEnd(anim: android.animation.Animator) = it()
                    override fun onAnimationStart(anim: android.animation.Animator) {}
                    override fun onAnimationCancel(anim: android.animation.Animator) {}
                    override fun onAnimationRepeat(anim: android.animation.Animator) {}
                })
            }
            start()
        }
    }
}

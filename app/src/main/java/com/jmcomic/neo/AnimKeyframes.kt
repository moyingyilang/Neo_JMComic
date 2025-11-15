package com.yourpackage.neojmcomic.utils.anim

import android.animation.Keyframe
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.View

/** 关键帧动画工具 */
object AnimKeyframes {
    /** 创建关键帧动画 */
    fun createKeyframeAnimator(
        view: View,
        property: String,
        keyframes: List<Pair<Float, Float>>, // (进度[0-1], 值)
        duration: Long = 500,
        interpolator: Interpolator = AnimEasing.EaseInOut
    ): ValueAnimator {
        val keyframeList = keyframes.mapIndexed { index, (fraction, value) ->
            Keyframe.ofFloat(fraction, value)
        }.toTypedArray()

        val holder = PropertyValuesHolder.ofKeyframe(property, *keyframeList)
        return ValueAnimator.ofPropertyValuesHolder(holder).apply {
            this.duration = duration
            this.interpolator = interpolator
            addUpdateListener { anim ->
                when (property) {
                    "alpha" -> view.alpha = anim.animatedValue as Float
                    "scaleX" -> view.scaleX = anim.animatedValue as Float
                    "scaleY" -> view.scaleY = anim.animatedValue as Float
                }
            }
        }
    }

    /** 示例：执行缩放+淡入关键帧动画 */
    fun animateScaleFadeIn(view: View, duration: Long = 500) {
        val scaleX = createKeyframeAnimator(
            view, "scaleX",
            listOf(0f to 0.8f, 0.5f to 1.1f, 1f to 1.0f),
            duration
        )
        val scaleY = createKeyframeAnimator(
            view, "scaleY",
            listOf(0f to 0.8f, 0.5f to 1.1f, 1f to 1.0f),
            duration
        )
        val alpha = createKeyframeAnimator(
            view, "alpha",
            listOf(0f to 0f, 1f to 1f),
            duration
        )

        ValueAnimator.ofPropertyValuesHolder().apply {
            playTogether(scaleX, scaleY, alpha)
            start()
        }
    }
}

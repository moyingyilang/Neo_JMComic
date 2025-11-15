package com.yourpackage.neojmcomic.utils.anim

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator

/** 缓动曲线工具（适配easing+anticipate） */
object AnimEasing {
    // 基础缓动
    val Linear: Interpolator = LinearInterpolator()
    val EaseInOut: Interpolator = AccelerateDecelerateInterpolator()
    val Bounce: Interpolator = BounceInterpolator()

    // 预加载缓动（anticipate）
    val Anticipate: Interpolator = object : Interpolator {
        override fun getInterpolation(p: Float): Float {
            return if (p * 2 < 1) 0.5f * backIn(p * 2) else 0.5f * (2 - Math.pow(2.0, -10 * (p - 1))).toFloat()
        }
        private fun backIn(p: Float): Float = p * p * (3 * p - 2)
    }

    // 贝塞尔缓动（cubic-bezier）
    fun cubicBezier(x1: Float, y1: Float, x2: Float, y2: Float): Interpolator {
        return android.view.animation.PathInterpolator(x1, y1, x2, y2)
    }
}

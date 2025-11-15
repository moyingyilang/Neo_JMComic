package com.yourpackage.neojmcomic.utils.anim

import android.view.View
import android.view.animation.ValueAnimator
import androidx.core.widget.NestedScrollView

/** 滚动联动动画工具 */
object AnimScroll {
    /** 滚动时联动View动画 */
    fun bindScrollAnimation(
        scrollView: NestedScrollView,
        targetView: View,
        property: String,
        startValue: Float,
        endValue: Float,
        scrollRange: Int // 触发动画的滚动距离
    ) {
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val progress = Math.min(scrollY.toFloat() / scrollRange, 1f)
            val currentValue = startValue + (endValue - startValue) * progress
            when (property) {
                "alpha" -> targetView.alpha = currentValue
                "translationY" -> targetView.translationY = currentValue
                "scaleX" -> targetView.scaleX = currentValue
                "scaleY" -> targetView.scaleY = currentValue
            }
        }
    }

    /** 平滑滚动到指定位置 */
    fun smoothScrollTo(scrollView: NestedScrollView, y: Int, duration: Long = 300) {
        val from = scrollView.scrollY.toFloat()
        ValueAnimator.ofFloat(from, y.toFloat()).apply {
            this.duration = duration
            interpolator = AnimEasing.EaseInOut
            addUpdateListener { anim ->
                scrollView.scrollTo(0, anim.animatedValue as Int)
            }
            start()
        }
    }
}

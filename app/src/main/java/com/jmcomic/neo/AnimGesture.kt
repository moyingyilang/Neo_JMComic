package com.yourpackage.neojmcomic.utils.anim

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

/** 手势回调接口 */
interface AnimGestureCallback {
    fun onTap(view: View, x: Float, y: Float) {}
    fun onHoverEnter(view: View, x: Float, y: Float) {}
    fun onHoverExit(view: View, x: Float, y: Float) {}
    fun onDragStart(view: View, x: Float, y: Float) {}
    fun onDrag(view: View, dx: Float, dy: Float) {}
    fun onDragEnd(view: View, x: Float, y: Float) {}
}

/** 手势管理工具 */
class AnimGesture(
    context: Context,
    private val view: View,
    private val callback: AnimGestureCallback
) : OnTouchListener {
    private val gestureDetector = GestureDetector(context, GestureListener())
    private var startX = 0f
    private var startY = 0f
    private var isDragging = false

    init {
        view.setOnTouchListener(this)
        view.setOnHoverListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> callback.onHoverEnter(v, event.x, event.y)
                MotionEvent.ACTION_HOVER_EXIT -> callback.onHoverExit(v, event.x, event.y)
            }
            true
        }
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            callback.onTap(view, e.x, e.y)
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val currentX = e2.x
            val currentY = e2.y
            if (!isDragging) {
                startX = currentX
                startY = currentY
                callback.onDragStart(view, currentX, currentY)
                isDragging = true
            }
            val dx = currentX - startX
            val dy = currentY - startY
            callback.onDrag(view, dx, dy)
            startX = currentX
            startY = currentY
            return true
        }

        override fun onScrollEnd(e: MotionEvent) {
            callback.onDragEnd(view, e.x, e.y)
            isDragging = false
        }
    }
}

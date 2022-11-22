package com.android.text

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import java.lang.IndexOutOfBoundsException

/**
 * Create by weishl
 * 2022/11/9
 */
object DrawableBoundsHelper {

    fun getDrawableBounds(
        view: EasyTextView,
        drawable: Drawable,
        mode: EasyTextView.DrawableMode,
        tag: Int,
        drawableWidth: Int,
        drawableHeight: Int
    ): IntArray {
        val originRect = drawable.bounds
        val originBounds =
            intArrayOf(originRect.left, originRect.top, originRect.right, originRect.bottom)
        var bounds = IntArray(4)
        val finalWidth = if (drawableWidth == 0) drawable.intrinsicWidth else drawableWidth
        val finalHeight = if (drawableWidth == 0) drawable.intrinsicHeight else drawableHeight

        if (mode.code == EasyTextView.DrawableMode.NORMAL.code && originRect.width() == drawableWidth && drawableHeight == originRect.height()) {
            return originBounds
        }

        if (tag == 0 || tag == 2) {
            val left = 0
            val right = finalWidth
            when (mode.code) {
                EasyTextView.DrawableMode.TOP.code -> {
                    val top = -view.lineCount * view.lineHeight / 2 + view.lineHeight / 2
                    val bottom = top + finalHeight
                    fillBounds(bounds, left, top, right, bottom)
                }
                EasyTextView.DrawableMode.BOTTOM.code -> {
                    val top = view.lineCount * view.lineHeight / 2 - view.lineHeight / 2
                    val bottom = top + finalHeight
                    fillBounds(bounds, left, top, right, bottom)
                }
                else -> {
                    if (finalWidth > originRect.width() && finalHeight > originRect.height()) {
                        val halfHeight = (finalHeight - originRect.height()) / 2
                        val left = originRect.left
                        val top = originRect.top - halfHeight
                        fillBounds(bounds, left, top, left + finalWidth, top + finalHeight)
                    } else {
                        bounds = originBounds
                    }
                }
            }
        } else if (tag == 1 || tag == 3) {
            val top = 0
            val bottom = top + finalHeight
            val lineBounds = Rect()
            try {
                view.getLineBounds(0, lineBounds)
            } catch (e: IndexOutOfBoundsException) {
            }

            if (mode.code == EasyTextView.DrawableMode.LEFT.code) {
                val left = if (view.compoundDrawables[0] != null) {
                    -view.width / 2 + view.paddingLeft + view.compoundDrawables[0].bounds.width()
                } else {
                    -view.width / 2 + view.paddingLeft + lineBounds.left
                }

                val right = left + finalWidth
                fillBounds(bounds, left, top, right, bottom)
            } else if (mode.code == EasyTextView.DrawableMode.RIGHT.code) {
                val right = if (view.compoundDrawables[3] != null) {
                    view.width / 2 - view.paddingRight- view.compoundDrawablePadding - view.compoundDrawables[3].bounds.width()
                } else {
                    view.width / 2 - view.paddingRight- view.compoundDrawablePadding
                }
                val left = right - finalWidth
                fillBounds(bounds, left, top, right, bottom)
            } else {
                if (finalWidth > originRect.width() && finalHeight > originRect.height()) {
                    val halfWidth = (finalWidth - originRect.width()) / 2
                    val left = originRect.left - halfWidth
                    val top = originRect.top
                    fillBounds(bounds, left, top, left + finalWidth, top + finalHeight)
                } else {
                    bounds = originBounds
                }
            }
        }
        return bounds
    }

    private fun fillBounds(bounds: IntArray, left: Int, top: Int, right: Int, bottom: Int) {
        bounds[0] = left
        bounds[1] = top
        bounds[2] = right
        bounds[3] = bottom
    }
}
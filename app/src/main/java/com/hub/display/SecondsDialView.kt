package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 60 Precision Segment Blocks encircling the clock:
 * Dynamically scales its dimensions to fit any screen size (from 5.0" Galaxy J5 to large A21s):
 * - Margin & dimensions computed from width & height.
 * - Single-line text safe area inside.
 */
class SecondsDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentSecond: Int = 0
    private var activeColor: Int = Color.WHITE
    private var inactiveOutlineColor: Int = Color.parseColor("#444444")

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    fun setSecond(second: Int) {
        if (currentSecond != second) {
            currentSecond = second
            invalidate()
        }
    }

    fun setDialColors(active: Int, inactive: Int) {
        activeColor = active
        inactiveOutlineColor = inactive
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val margin = 8f
        val left = margin
        val top = margin
        val right = w - margin
        val bottom = h - margin

        // Dynamic responsive segment size based on canvas width
        val segLength = (w / 16f) * 0.72f
        val segThick = 10f
        val corner = 2.5f

        fillPaint.color = activeColor
        strokePaint.color = inactiveOutlineColor

        val numTop = 16
        val numRight = 14
        val numBottom = 16
        val numLeft = 14
        val halfTop = numTop / 2 // 8

        for (i in 0 until 60) {
            val rect: RectF

            when {
                // Top Right: indices 0..7
                i < halfTop -> {
                    val step = (right - (left + right) / 2f) / halfTop
                    val cx = ((left + right) / 2f) + (i * step) + (step / 2f)
                    rect = RectF(cx - (segLength / 2f), top, cx + (segLength / 2f), top + segThick)
                }
                // Right Edge: indices 8..21
                i < halfTop + numRight -> {
                    val rIdx = i - halfTop
                    val step = (bottom - top) / numRight
                    val cy = top + (rIdx * step) + (step / 2f)
                    rect = RectF(right - segThick, cy - (segLength / 2f), right, cy + (segLength / 2f))
                }
                // Bottom Edge: indices 22..37
                i < halfTop + numRight + numBottom -> {
                    val bIdx = i - (halfTop + numRight)
                    val step = (right - left) / numBottom
                    val cx = right - (bIdx * step) - (step / 2f)
                    rect = RectF(cx - (segLength / 2f), bottom - segThick, cx + (segLength / 2f), bottom)
                }
                // Left Edge: indices 38..51
                i < halfTop + numRight + numBottom + numLeft -> {
                    val lIdx = i - (halfTop + numRight + numBottom)
                    val step = (bottom - top) / numLeft
                    val cy = bottom - (lIdx * step) - (step / 2f)
                    rect = RectF(left, cy - (segLength / 2f), left + segThick, cy + (segLength / 2f))
                }
                // Top Left: indices 52..59
                else -> {
                    val tlIdx = i - (halfTop + numRight + numBottom + numLeft)
                    val step = (((left + right) / 2f) - left) / halfTop
                    val cx = left + (tlIdx * step) + (step / 2f)
                    rect = RectF(cx - (segLength / 2f), top, cx + (segLength / 2f), top + segThick)
                }
            }

            if (i <= currentSecond) {
                canvas.drawRoundRect(rect, corner, corner, fillPaint)
            } else {
                canvas.drawRoundRect(rect, corner, corner, strokePaint)
            }
        }
    }
}

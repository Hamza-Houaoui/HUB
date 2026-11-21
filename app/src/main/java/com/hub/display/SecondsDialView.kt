package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 60 Elegant Precision Segment Blocks encircling the clock:
 * - Start at exact Top-Center (12 o'clock, tick index 0)
 * - Proceeds clockwise: Top-Center -> Top-Right -> Right -> Bottom -> Left -> Top-Left
 * - INACTIVE: Sleek hollow outline rectangle with subtle rounded corners
 * - ACTIVE: Solid filled luminous rectangle
 */
class SecondsDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentSecond: Int = 0
    private var activeColor: Int = Color.WHITE
    private var inactiveOutlineColor: Int = Color.parseColor("#333333")

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.2f
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

        val margin = 12f
        val left = margin
        val top = margin
        val right = w - margin
        val bottom = h - margin

        val segLength = 18f
        val segThick = 9f
        val corner = 2.5f

        fillPaint.color = activeColor
        strokePaint.color = inactiveOutlineColor

        // 60 segments:
        // Top edge: 16 segments (starts at center: index 0 is at middle top, runs to right, then indices 53..59 come from left)
        // Right edge: 14 segments
        // Bottom edge: 16 segments
        // Left edge: 14 segments
        // Total = 16 + 14 + 16 + 14 = 60 segments!

        val numTop = 16
        val numRight = 14
        val numBottom = 16
        val numLeft = 14

        val halfTop = numTop / 2 // 8

        for (i in 0 until 60) {
            val rect: RectF

            when {
                // Top Right (from center x to right): indices 0..7 (8 segments)
                i < halfTop -> {
                    val step = (right - (left + right) / 2f) / halfTop
                    val cx = ((left + right) / 2f) + (i * step) + (step / 2f)
                    rect = RectF(cx - (segLength / 2f), top, cx + (segLength / 2f), top + segThick)
                }
                // Right Edge: indices 8..21 (14 segments, top to bottom)
                i < halfTop + numRight -> {
                    val rIdx = i - halfTop
                    val step = (bottom - top) / numRight
                    val cy = top + (rIdx * step) + (step / 2f)
                    rect = RectF(right - segThick, cy - (segLength / 2f), right, cy + (segLength / 2f))
                }
                // Bottom Edge: indices 22..37 (16 segments, right to left)
                i < halfTop + numRight + numBottom -> {
                    val bIdx = i - (halfTop + numRight)
                    val step = (right - left) / numBottom
                    val cx = right - (bIdx * step) - (step / 2f)
                    rect = RectF(cx - (segLength / 2f), bottom - segThick, cx + (segLength / 2f), bottom)
                }
                // Left Edge: indices 38..51 (14 segments, bottom to top)
                i < halfTop + numRight + numBottom + numLeft -> {
                    val lIdx = i - (halfTop + numRight + numBottom)
                    val step = (bottom - top) / numLeft
                    val cy = bottom - (lIdx * step) - (step / 2f)
                    rect = RectF(left, cy - (segLength / 2f), left + segThick, cy + (segLength / 2f))
                }
                // Top Left: indices 52..59 (8 segments, left to center)
                else -> {
                    val tlIdx = i - (halfTop + numRight + numBottom + numLeft)
                    val step = (((left + right) / 2f) - left) / halfTop
                    val cx = left + (tlIdx * step) + (step / 2f)
                    rect = RectF(cx - (segLength / 2f), top, cx + (segLength / 2f), top + segThick)
                }
            }

            if (i <= currentSecond) {
                // ACTIVE: Solid filled with subtle glow
                canvas.drawRoundRect(rect, corner, corner, fillPaint)
            } else {
                // INACTIVE: Sleek hollow precision border
                canvas.drawRoundRect(rect, corner, corner, strokePaint)
            }
        }
    }
}

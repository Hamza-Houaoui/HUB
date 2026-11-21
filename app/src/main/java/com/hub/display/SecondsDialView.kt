package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 60 rectangular tick marks arranged along the rectangular border
 * of the left half of the screen.
 * - Thickness: Bold (8dp)
 * - Major ticks: Extra bold (12dp)
 * - Light up tick-by-tick with each second, reset when 60s completes.
 */
class SecondsDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var currentSecond: Int = 0
    private var activeColor: Int = Color.WHITE
    private var inactiveColor: Int = Color.parseColor("#222222")

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
    }

    fun setSecond(second: Int) {
        if (currentSecond != second) {
            currentSecond = second
            invalidate()
        }
    }

    fun setDialColors(active: Int, inactive: Int) {
        activeColor = active
        inactiveColor = inactive
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val margin = 16f
        val left = margin
        val top = margin
        val right = w - margin
        val bottom = h - margin

        val innerMargin = 38f
        val innerLeft = innerMargin
        val innerTop = innerMargin
        val innerRight = w - innerMargin
        val innerBottom = h - innerMargin

        activePaint.color = activeColor
        inactivePaint.color = inactiveColor

        // 60 ticks distributed along rectangle perimeter:
        // Top: 15 ticks (left to right)
        // Right: 15 ticks (top to bottom)
        // Bottom: 15 ticks (right to left)
        // Left: 15 ticks (bottom to top)
        for (i in 0 until 60) {
            var x1 = 0f
            var y1 = 0f
            var x2 = 0f
            var y2 = 0f

            when {
                i < 15 -> { // Top edge
                    val fraction = i / 15f
                    val x = left + fraction * (right - left)
                    x1 = x; y1 = top
                    x2 = x; y2 = innerTop
                }
                i < 30 -> { // Right edge
                    val fraction = (i - 15) / 15f
                    val y = top + fraction * (bottom - top)
                    x1 = right; y1 = y
                    x2 = innerRight; y2 = y
                }
                i < 45 -> { // Bottom edge
                    val fraction = (i - 30) / 15f
                    val x = right - fraction * (right - left)
                    x1 = x; y1 = bottom
                    x2 = x; y2 = innerBottom
                }
                else -> { // Left edge
                    val fraction = (i - 45) / 15f
                    val y = bottom - fraction * (bottom - top)
                    x1 = left; y1 = y
                    x2 = innerLeft; y2 = y
                }
            }

            val isMajor = (i % 5 == 0)
            val strokeW = if (isMajor) 10f else 6.5f

            val paint = if (i <= currentSecond) {
                activePaint.strokeWidth = strokeW
                activePaint
            } else {
                inactivePaint.strokeWidth = strokeW
                inactivePaint
            }

            canvas.drawLine(x1, y1, x2, y2, paint)
        }
    }
}

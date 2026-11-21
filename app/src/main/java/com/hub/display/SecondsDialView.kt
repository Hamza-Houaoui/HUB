package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 60 radial ticks around the clock face representing seconds.
 * Fills up tick-by-tick each second, resets smoothly when 60s completes.
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
        strokeCap = Paint.Cap.ROUND
    }

    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
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

        val cx = w / 2f
        val cy = h / 2f

        // Use rounded rectangle / oval bounds that closely trace the clock text area
        val outerRadiusX = (w / 2f) - 16f
        val outerRadiusY = (h / 2f) - 16f
        val tickLength = 14f

        activePaint.color = activeColor
        inactivePaint.color = inactiveColor

        for (i in 0 until 60) {
            // Angle in radians: start at 12 o'clock (-90 degrees)
            val angleDeg = (i * 6f) - 90f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val cos = Math.cos(angleRad).toFloat()
            val sin = Math.sin(angleRad).toFloat()

            val isMajor = (i % 5 == 0)
            val len = if (isMajor) tickLength * 1.5f else tickLength
            val strokeW = if (isMajor) 4f else 2.5f

            val xOuter = cx + outerRadiusX * cos
            val yOuter = cy + outerRadiusY * sin

            val xInner = cx + (outerRadiusX - len) * cos
            val yInner = cy + (outerRadiusY - len) * sin

            val paint = if (i <= currentSecond) {
                activePaint.strokeWidth = strokeW
                activePaint
            } else {
                inactivePaint.strokeWidth = strokeW
                inactivePaint
            }

            canvas.drawLine(xInner, yInner, xOuter, yOuter, paint)
        }
    }
}

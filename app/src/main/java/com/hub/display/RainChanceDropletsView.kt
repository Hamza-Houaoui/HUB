package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * 10 Rain Droplet Scale representing Rain Chance (0% to 100%):
 * 10 droplets = 100%, 1 droplet per 10%
 * Active droplets are filled (Cyan or Night-Red), inactive droplets are hollow outlines.
 */
class RainChanceDropletsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rainProbability: Int = 0 // 0..100
    private var isNightMode: Boolean = false

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
    }

    private val path = Path()

    fun setProbability(prob: Int, night: Boolean) {
        rainProbability = prob.coerceIn(0, 100)
        isNightMode = night
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val totalDrops = 10
        val activeCount = Math.round(rainProbability / 10f)

        val activeColor = if (isNightMode) Color.parseColor("#E53935") else Color.parseColor("#29B6F6")
        val inactiveColor = if (isNightMode) Color.parseColor("#441111") else Color.parseColor("#2C2C2C")

        fillPaint.color = activeColor
        strokePaint.color = inactiveColor

        val dropSpacing = w / totalDrops
        val dropW = dropSpacing * 0.65f
        val dropH = h * 0.85f

        for (i in 0 until totalDrops) {
            val cx = (i * dropSpacing) + (dropSpacing / 2f)
            val topY = (h - dropH) / 2f
            val botY = topY + dropH

            // Draw a teardrop shape
            path.reset()
            path.moveTo(cx, topY) // Top sharp point
            // Curve down to right belly
            path.cubicTo(
                cx + (dropW / 1.5f), topY + (dropH * 0.55f),
                cx + (dropW / 1.5f), botY,
                cx, botY
            )
            // Curve up from left belly to top
            path.cubicTo(
                cx - (dropW / 1.5f), botY,
                cx - (dropW / 1.5f), topY + (dropH * 0.55f),
                cx, topY
            )
            path.close()

            if (i < activeCount) {
                canvas.drawPath(path, fillPaint)
            } else {
                canvas.drawPath(path, strokePaint)
            }
        }
    }
}

package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Custom Vector Weather Icon renderer matching clean outline icon designs:
 * - Bold curved Cloud outline
 * - Animated teardrop rain drops falling smoothly underneath
 * - Night Mode: fully tinted #E53935 (Red) like the clock!
 */
class AnimatedWeatherIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var weatherCode = 0
    private var isDay = true
    private var isNightMode = false
    private var animPhase = 0f

    private val cloudPath = Path()
    private val dropPath = Path()

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setWeather(emojiStr: String, code: Int, day: Boolean, nightMode: Boolean) {
        weatherCode = code
        isDay = day
        isNightMode = nightMode
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val primaryColor = if (isNightMode) Color.parseColor("#E53935") else Color.WHITE
        strokePaint.color = primaryColor
        strokePaint.strokeWidth = w * 0.07f // thick bold stroke

        fillPaint.color = primaryColor

        animPhase += 0.05f
        if (animPhase > Math.PI * 2) animPhase = 0f

        val cx = w / 2f
        val cy = h / 2f

        canvas.save()

        // Float breathing effect
        val floatY = Math.sin(animPhase.toDouble()).toFloat() * 4f
        canvas.translate(0f, floatY)

        val isRain = weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)
        val isCloudy = weatherCode in listOf(2, 3, 45, 48)

        if (isRain || isCloudy) {
            // Draw Bold Cloud Outline
            val cloudLeft = w * 0.12f
            val cloudRight = w * 0.88f
            val cloudBottom = h * 0.58f

            cloudPath.reset()
            cloudPath.moveTo(cloudLeft + (w * 0.15f), cloudBottom)
            cloudPath.lineTo(cloudRight - (w * 0.15f), cloudBottom)
            // Right lobe
            cloudPath.cubicTo(
                cloudRight + (w * 0.03f), cloudBottom,
                cloudRight + (w * 0.03f), cloudBottom - (h * 0.28f),
                cloudRight - (w * 0.15f), cloudBottom - (h * 0.28f)
            )
            // Top main lobe
            cloudPath.cubicTo(
                cloudRight - (w * 0.15f), h * 0.16f,
                cloudLeft + (w * 0.28f), h * 0.12f,
                cloudLeft + (w * 0.24f), cloudBottom - (h * 0.24f)
            )
            // Left lobe
            cloudPath.cubicTo(
                cloudLeft - (w * 0.05f), cloudBottom - (h * 0.24f),
                cloudLeft - (w * 0.05f), cloudBottom,
                cloudLeft + (w * 0.15f), cloudBottom
            )
            cloudPath.close()

            canvas.drawPath(cloudPath, strokePaint)

            // If rain: draw 2 falling vector raindrops
            if (isRain) {
                val dropPhase1 = (animPhase * 1.5f) % (Math.PI * 2).toFloat()
                val dropPhase2 = ((animPhase * 1.5f) + 1.8f) % (Math.PI * 2).toFloat()

                drawTeardrop(canvas, w * 0.44f, cloudBottom + (h * 0.08f) + (dropPhase1 * 4f), w * 0.07f, h * 0.13f)
                drawTeardrop(canvas, w * 0.62f, cloudBottom + (h * 0.05f) + (dropPhase2 * 4f), w * 0.06f, h * 0.11f)
            }
        } else if (!isDay) {
            // Night Clear / Moon: Bold Crescent Moon Outline
            strokePaint.style = Paint.Style.STROKE
            val moonRadius = w * 0.32f
            canvas.drawCircle(cx, cy, moonRadius, strokePaint)
            // Cutout inner curve
            val cutoutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx + (moonRadius * 0.52f), cy - (moonRadius * 0.22f), moonRadius * 0.9f, cutoutPaint)
        } else {
            // Day Clear: Sun with Pulsing Rays
            val sunRadius = w * 0.24f
            canvas.drawCircle(cx, cy, sunRadius, strokePaint)

            val rayLen = w * 0.11f
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45f + (animPhase * 10f)).toDouble())
                val cos = Math.cos(angle).toFloat()
                val sin = Math.sin(angle).toFloat()
                val r1 = sunRadius + (w * 0.05f)
                val r2 = r1 + rayLen
                canvas.drawLine(cx + cos * r1, cy + sin * r1, cx + cos * r2, cy + sin * r2, strokePaint)
            }
        }

        canvas.restore()
        postInvalidateDelayed(33)
    }

    private fun drawTeardrop(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        dropPath.reset()
        dropPath.moveTo(x, y)
        dropPath.cubicTo(x + width, y + (height * 0.6f), x + width, y + height, x, y + height)
        dropPath.cubicTo(x - width, y + height, x - width, y + (height * 0.6f), x, y)
        dropPath.close()
        canvas.drawPath(dropPath, fillPaint)
    }
}

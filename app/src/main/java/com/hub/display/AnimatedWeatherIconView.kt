package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Shared, cohesive Vector Weather Icon renderer used for BOTH:
 * 1. The Main Weather Display (Large 110dp)
 * 2. The 24h Hourly Forecast items (Compact 28dp)
 *
 * Features:
 * - Clear visibility against pure OLED black background:
 *   - Clouds: Filled with dark charcoal (#1A1A1A or night #2E0B0B) + bold primary outline (#FFFFFF / #E53935).
 *   - When Cloud is over Moon: Moon is drawn behind, and Cloud is opaque with glowing outline!
 * - Night Mode: 100% harmonized red (#E53935)
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
    private var isCompact = false

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

    private val cloudBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setWeather(code: Int, day: Boolean, nightMode: Boolean, compact: Boolean = false) {
        weatherCode = code
        isDay = day
        isNightMode = nightMode
        isCompact = compact
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val primaryColor = if (isNightMode) Color.parseColor("#E53935") else Color.WHITE
        val strokeW = if (isCompact) w * 0.09f else w * 0.065f
        strokePaint.color = primaryColor
        strokePaint.strokeWidth = strokeW
        fillPaint.color = primaryColor

        // Cloud body fill: solid dark so moon/background behind it is occluded, but outline pops!
        cloudBodyPaint.color = if (isNightMode) Color.parseColor("#250505") else Color.parseColor("#151515")

        if (!isCompact) {
            animPhase += 0.05f
            if (animPhase > Math.PI * 2) animPhase = 0f
        }

        val cx = w / 2f
        val cy = h / 2f

        canvas.save()

        if (!isCompact) {
            // Floating breathing animation
            val floatY = Math.sin(animPhase.toDouble()).toFloat() * 3.5f
            canvas.translate(0f, floatY)
        }

        val isRain = weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99)
        val isSnow = weatherCode in listOf(71, 73, 75, 77, 85, 86)
        val isCloudy = weatherCode in listOf(1, 2, 3, 45, 48)

        when {
            isRain || isSnow -> {
                // Cloud with Rain or Snow
                drawCloud(canvas, w, h, cx, cy * 0.9f)
                if (isRain) {
                    val p1 = if (isCompact) 0f else (animPhase * 1.5f) % (Math.PI * 2).toFloat()
                    val p2 = if (isCompact) 0f else ((animPhase * 1.5f) + 1.8f) % (Math.PI * 2).toFloat()
                    val dropY = h * 0.68f
                    drawTeardrop(canvas, w * 0.42f, dropY + (p1 * 3.5f), w * 0.07f, h * 0.12f)
                    drawTeardrop(canvas, w * 0.62f, dropY - (h * 0.04f) + (p2 * 3.5f), w * 0.06f, h * 0.11f)
                } else {
                    // Snow: 3 delicate flakes
                    canvas.drawCircle(w * 0.38f, h * 0.76f, w * 0.045f, fillPaint)
                    canvas.drawCircle(w * 0.54f, h * 0.82f, w * 0.045f, fillPaint)
                    canvas.drawCircle(w * 0.70f, h * 0.76f, w * 0.045f, fillPaint)
                }
            }
            isCloudy -> {
                // Partly Cloudy: Moon or Sun behind Cloud
                if (!isDay) {
                    // Crescent Moon behind top-left
                    drawCrescentMoon(canvas, w * 0.38f, h * 0.35f, w * 0.22f)
                } else if (weatherCode == 1 || weatherCode == 2) {
                    // Sun rays behind top-right
                    drawMiniSun(canvas, w * 0.64f, h * 0.34f, w * 0.18f)
                }
                // Opaque Cloud with bright outline on top!
                drawCloud(canvas, w, h, cx, cy * 1.05f)
            }
            !isDay -> {
                // Pure Clear Moon
                drawCrescentMoon(canvas, cx, cy, w * 0.32f)
            }
            else -> {
                // Pure Clear Sun
                drawFullSun(canvas, cx, cy, w * 0.24f)
            }
        }

        canvas.restore()

        if (!isCompact) {
            postInvalidateDelayed(33)
        }
    }

    private fun drawCloud(canvas: Canvas, w: Float, h: Float, cx: Float, cy: Float) {
        val cloudLeft = cx - (w * 0.36f)
        val cloudRight = cx + (w * 0.36f)
        val cloudBottom = cy + (h * 0.16f)

        cloudPath.reset()
        cloudPath.moveTo(cloudLeft + (w * 0.14f), cloudBottom)
        cloudPath.lineTo(cloudRight - (w * 0.14f), cloudBottom)
        // Right lobe
        cloudPath.cubicTo(
            cloudRight + (w * 0.04f), cloudBottom,
            cloudRight + (w * 0.04f), cloudBottom - (h * 0.25f),
            cloudRight - (w * 0.12f), cloudBottom - (h * 0.25f)
        )
        // Top main lobe
        cloudPath.cubicTo(
            cloudRight - (w * 0.12f), cloudBottom - (h * 0.44f),
            cloudLeft + (w * 0.22f), cloudBottom - (h * 0.46f),
            cloudLeft + (w * 0.18f), cloudBottom - (h * 0.22f)
        )
        // Left lobe
        cloudPath.cubicTo(
            cloudLeft - (w * 0.04f), cloudBottom - (h * 0.22f),
            cloudLeft - (w * 0.04f), cloudBottom,
            cloudLeft + (w * 0.14f), cloudBottom
        )
        cloudPath.close()

        // 1. Fill solid body first (masks whatever is behind it)
        canvas.drawPath(cloudPath, cloudBodyPaint)
        // 2. Draw crisp luminous outline
        canvas.drawPath(cloudPath, strokePaint)
    }

    private fun drawCrescentMoon(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val moonPath = Path()
        moonPath.moveTo(cx, cy - radius)
        moonPath.cubicTo(
            cx + radius * 0.9f, cy - radius,
            cx + radius * 0.9f, cy + radius,
            cx, cy + radius
        )
        moonPath.cubicTo(
            cx + radius * 0.35f, cy + radius * 0.6f,
            cx + radius * 0.35f, cy - radius * 0.6f,
            cx, cy - radius
        )
        moonPath.close()

        canvas.drawPath(moonPath, fillPaint)
    }

    private fun drawMiniSun(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.drawCircle(cx, cy, radius, strokePaint)
        val rayLen = radius * 0.45f
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60f + (animPhase * 8f)).toDouble())
            val cos = Math.cos(angle).toFloat()
            val sin = Math.sin(angle).toFloat()
            canvas.drawLine(
                cx + cos * (radius + 3f),
                cy + sin * (radius + 3f),
                cx + cos * (radius + 3f + rayLen),
                cy + sin * (radius + 3f + rayLen),
                strokePaint
            )
        }
    }

    private fun drawFullSun(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        canvas.drawCircle(cx, cy, radius, strokePaint)
        val rayLen = radius * 0.5f
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45f + (animPhase * 8f)).toDouble())
            val cos = Math.cos(angle).toFloat()
            val sin = Math.sin(angle).toFloat()
            canvas.drawLine(
                cx + cos * (radius + 4f),
                cy + sin * (radius + 4f),
                cx + cos * (radius + 4f + rayLen),
                cy + sin * (radius + 4f + rayLen),
                strokePaint
            )
        }
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

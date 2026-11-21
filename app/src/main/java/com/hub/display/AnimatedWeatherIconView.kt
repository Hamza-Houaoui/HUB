package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Animated Weather Icon View:
 * - Sun: Gentle glowing rays expanding & contracting
 * - Moon: Soft orbital rotation & smooth float
 * - Cloud: Smooth breathing & drifting
 * - Rain: Droplets gliding down from the cloud
 */
class AnimatedWeatherIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var emoji = "☀️"
    private var weatherCode = 0
    private var isDay = true
    private var isNightMode = false

    private var animPhase = 0f
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    fun setWeather(emojiStr: String, code: Int, day: Boolean, nightMode: Boolean) {
        emoji = emojiStr
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

        val cx = w / 2f
        val cy = h / 2f

        animPhase += 0.05f
        if (animPhase > Math.PI * 2) animPhase = 0f

        canvas.save()

        // Float effect (subtle up & down floating)
        val offsetY = Math.sin(animPhase.toDouble()).toFloat() * 6f
        canvas.translate(0f, offsetY)

        // Rotation effect (subtle tilt for moon/sun)
        val rotationDeg = Math.sin((animPhase * 0.8).toDouble()).toFloat() * 6f
        canvas.rotate(rotationDeg, cx, cy)

        // Draw animated background glow for sun/moon
        val glowColor = if (isNightMode) Color.parseColor("#E53935") else Color.parseColor("#FFA000")
        glowPaint.color = glowColor
        val pulse = (Math.sin(animPhase * 2.toDouble()).toFloat() + 1f) / 2f // 0..1

        if (weatherCode == 0 || weatherCode == 1) {
            glowPaint.strokeWidth = 3f + (pulse * 3f)
            glowPaint.alpha = (40 + (pulse * 80)).toInt()
            val radius = (w * 0.35f) + (pulse * 8f)
            canvas.drawCircle(cx, cy, radius, glowPaint)
        }

        // Draw Emoji Icon
        textPaint.textSize = w * 0.72f
        val baseline = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(emoji, cx, baseline, textPaint)

        canvas.restore()

        // 30fps refresh
        postInvalidateDelayed(33)
    }
}

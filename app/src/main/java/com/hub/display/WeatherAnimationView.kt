package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * Prominent Dynamic Weather Particle Animation:
 * - Rain: Bold glowing slanted raindrops falling with speed
 * - Snow: Soft luminous drifting snow discs
 * - Thunder: Periodic subtle sky lightning flashes + rain
 * - Wind: Fast horizontal wind gusts
 * - Fog / Mist: Layered horizontal fog bands drifting across
 * - Clear Night: Sparkling bright twinkling stars (diamonds and circles)
 * - Clear Day: Golden sunshine rays / dust particles
 */
class WeatherAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class WeatherType {
        CLEAR_DAY, CLEAR_NIGHT, CLOUDY, RAIN, SNOW, THUNDER, FOG, WIND
    }

    private var weatherType = WeatherType.CLEAR_DAY
    private var isNightMode = false

    private data class Particle(
        var x: Float,
        var y: Float,
        var speedX: Float,
        var speedY: Float,
        var size: Float,
        var alpha: Int,
        var phase: Float = 0f
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fogPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun setWeatherState(weatherCode: Int, isDay: Boolean, nightMode: Boolean) {
        isNightMode = nightMode
        weatherType = when (weatherCode) {
            0 -> if (isDay) WeatherType.CLEAR_DAY else WeatherType.CLEAR_NIGHT
            1, 2, 3 -> WeatherType.CLOUDY
            45, 48 -> WeatherType.FOG
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherType.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherType.SNOW
            95, 96, 99 -> WeatherType.THUNDER
            else -> if (isDay) WeatherType.CLEAR_DAY else WeatherType.CLEAR_NIGHT
        }
        resetParticles()
        invalidate()
    }

    private fun resetParticles() {
        particles.clear()
        val count = when (weatherType) {
            WeatherType.RAIN, WeatherType.THUNDER -> 55
            WeatherType.SNOW -> 40
            WeatherType.FOG -> 10
            WeatherType.CLOUDY -> 18
            WeatherType.WIND -> 30
            WeatherType.CLEAR_NIGHT -> 35
            WeatherType.CLEAR_DAY -> 16
        }

        val w = if (width > 0) width.toFloat() else 800f
        val h = if (height > 0) height.toFloat() else 600f

        for (i in 0 until count) {
            particles.add(createParticle(w, h))
        }
    }

    private fun createParticle(w: Float, h: Float): Particle {
        return when (weatherType) {
            WeatherType.RAIN, WeatherType.THUNDER -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = -3f + Random.nextFloat() * 1.5f,
                speedY = 22f + Random.nextFloat() * 14f,
                size = 20f + Random.nextFloat() * 20f,
                alpha = 140 + Random.nextInt(100)
            )
            WeatherType.SNOW -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = -1.2f + Random.nextFloat() * 2.4f,
                speedY = 2.5f + Random.nextFloat() * 3.5f,
                size = 4f + Random.nextFloat() * 6f,
                alpha = 160 + Random.nextInt(90)
            )
            WeatherType.FOG -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0.8f + Random.nextFloat() * 1.2f,
                speedY = 0f,
                size = 60f + Random.nextFloat() * 80f,
                alpha = 40 + Random.nextInt(40)
            )
            WeatherType.CLOUDY -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0.8f + Random.nextFloat() * 1.0f,
                speedY = 0f,
                size = 45f + Random.nextFloat() * 55f,
                alpha = 35 + Random.nextInt(45)
            )
            WeatherType.CLEAR_NIGHT -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0f,
                speedY = 0f,
                size = 2.5f + Random.nextFloat() * 4.5f,
                alpha = 90 + Random.nextInt(150),
                phase = Random.nextFloat() * 6.28f
            )
            else -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0.4f,
                speedY = -0.8f,
                size = 3.5f + Random.nextFloat() * 3.5f,
                alpha = 80 + Random.nextInt(100)
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetParticles()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val primaryColor = if (isNightMode) Color.parseColor("#E53935") else Color.WHITE

        when (weatherType) {
            WeatherType.RAIN, WeatherType.THUNDER -> {
                paint.color = primaryColor
                paint.strokeWidth = 3f
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawLine(p.x, p.y, p.x + p.speedX * 2, p.y + p.size, paint)
                    p.x += p.speedX
                    p.y += p.speedY
                    if (p.y > h) { p.y = -p.size; p.x = Random.nextFloat() * w }
                    if (p.x < 0) { p.x = w }
                }
            }
            WeatherType.SNOW -> {
                paint.color = primaryColor
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                    p.x += p.speedX
                    p.y += p.speedY
                    if (p.y > h) { p.y = -p.size; p.x = Random.nextFloat() * w }
                }
            }
            WeatherType.FOG -> {
                fogPaint.color = if (isNightMode) Color.parseColor("#3B0909") else Color.parseColor("#333333")
                for (p in particles) {
                    fogPaint.alpha = p.alpha
                    val rect = RectF(p.x - p.size, p.y - (p.size * 0.35f), p.x + p.size, p.y + (p.size * 0.35f))
                    canvas.drawRoundRect(rect, 20f, 20f, fogPaint)
                    p.x += p.speedX
                    if (p.x - p.size > w) { p.x = -p.size; p.y = Random.nextFloat() * h }
                }
            }
            WeatherType.CLOUDY -> {
                paint.color = if (isNightMode) Color.parseColor("#330808") else Color.parseColor("#262626")
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                    p.x += p.speedX
                    if (p.x - p.size > w) { p.x = -p.size; p.y = Random.nextFloat() * h }
                }
            }
            WeatherType.CLEAR_NIGHT -> {
                // Sparkling, clearly visible diamond & round stars
                paint.color = primaryColor
                for (p in particles) {
                    p.phase += 0.08f
                    val twinkle = (Math.sin(p.phase.toDouble()).toFloat() + 1f) / 2f // 0..1
                    paint.alpha = (90 + (twinkle * 165)).toInt().coerceIn(40, 255)

                    // Draw 4-point cross star for larger particles
                    if (p.size > 4f) {
                        val arm = p.size * (0.8f + (twinkle * 0.5f))
                        paint.strokeWidth = 1.8f
                        canvas.drawLine(p.x - arm, p.y, p.x + arm, p.y, paint)
                        canvas.drawLine(p.x, p.y - arm, p.x, p.y + arm, paint)
                    } else {
                        canvas.drawCircle(p.x, p.y, p.size * (0.8f + (twinkle * 0.4f)), paint)
                    }
                }
            }
            else -> {
                // Clear Day: Warm luminous sun particles
                paint.color = primaryColor
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                    p.x += p.speedX
                    p.y += p.speedY
                    if (p.y < 0) { p.y = h; p.x = Random.nextFloat() * w }
                }
            }
        }

        postInvalidateDelayed(33)
    }
}

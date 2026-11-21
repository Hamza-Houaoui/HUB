package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * Lightweight particle animation matching current weather:
 * - Rain (streaks falling fast)
 * - Snow (gentle falling flakes)
 * - Thunder (flashes + rain)
 * - Wind (horizontal streaks)
 * - Clouds (drifting soft clouds)
 * - Clear (night stars twinkling or subtle sunshine dust)
 */
class WeatherAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class WeatherType {
        CLEAR_DAY, CLEAR_NIGHT, CLOUDY, RAIN, SNOW, THUNDER, WIND
    }

    private var weatherType = WeatherType.CLEAR_DAY
    private var isNightMode = false

    private data class Particle(
        var x: Float,
        var y: Float,
        var speedX: Float,
        var speedY: Float,
        var size: Float,
        var alpha: Int
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setWeatherState(weatherCode: Int, isDay: Boolean, nightMode: Boolean) {
        isNightMode = nightMode
        weatherType = when (weatherCode) {
            0, 1 -> if (isDay) WeatherType.CLEAR_DAY else WeatherType.CLEAR_NIGHT
            2, 3, 45, 48 -> WeatherType.CLOUDY
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
            WeatherType.RAIN, WeatherType.THUNDER -> 45
            WeatherType.SNOW -> 30
            WeatherType.CLOUDY -> 12
            WeatherType.WIND -> 25
            WeatherType.CLEAR_NIGHT -> 20
            WeatherType.CLEAR_DAY -> 10
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
                speedX = -2f + Random.nextFloat() * 1f,
                speedY = 18f + Random.nextFloat() * 12f,
                size = 15f + Random.nextFloat() * 15f,
                alpha = 90 + Random.nextInt(100)
            )
            WeatherType.SNOW -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = -1f + Random.nextFloat() * 2f,
                speedY = 2f + Random.nextFloat() * 3f,
                size = 3f + Random.nextFloat() * 5f,
                alpha = 100 + Random.nextInt(100)
            )
            WeatherType.CLOUDY -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0.5f + Random.nextFloat() * 0.8f,
                speedY = 0f,
                size = 30f + Random.nextFloat() * 40f,
                alpha = 30 + Random.nextInt(40)
            )
            WeatherType.CLEAR_NIGHT -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0f,
                speedY = 0f,
                size = 2f + Random.nextFloat() * 3f,
                alpha = 40 + Random.nextInt(160)
            )
            else -> Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                speedX = 0.2f,
                speedY = -0.5f,
                size = 2f + Random.nextFloat() * 2f,
                alpha = 40 + Random.nextInt(60)
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

        val baseColor = if (isNightMode) Color.parseColor("#E53935") else Color.WHITE

        when (weatherType) {
            WeatherType.RAIN, WeatherType.THUNDER -> {
                paint.color = baseColor
                paint.strokeWidth = 2.5f
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
                paint.color = baseColor
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                    p.x += p.speedX
                    p.y += p.speedY
                    if (p.y > h) { p.y = -p.size; p.x = Random.nextFloat() * w }
                }
            }
            WeatherType.CLOUDY -> {
                paint.color = if (isNightMode) Color.parseColor("#441111") else Color.parseColor("#222222")
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                    p.x += p.speedX
                    if (p.x - p.size > w) { p.x = -p.size; p.y = Random.nextFloat() * h }
                }
            }
            WeatherType.CLEAR_NIGHT -> {
                paint.color = baseColor
                for (p in particles) {
                    paint.alpha = (p.alpha + Random.nextInt(-15, 16)).coerceIn(30, 240)
                    p.alpha = paint.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                }
            }
            else -> {
                paint.color = baseColor
                for (p in particles) {
                    paint.alpha = p.alpha
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                    p.x += p.speedX
                    p.y += p.speedY
                    if (p.y < 0) { p.y = h; p.x = Random.nextFloat() * w }
                }
            }
        }

        // Post next frame (~30 fps) for smooth and battery-friendly animation
        postInvalidateDelayed(33)
    }
}

package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * Responsive Clock TextView that auto-measures the canvas and scales
 * its font size to fill the exact full width from border to border!
 */
class AutoScaleClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var timeString: String = "00:00"
    private var textColor: Int = Color.WHITE

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val textBounds = Rect()

    fun setTime(time: String) {
        if (timeString != time) {
            timeString = time
            invalidate()
        }
    }

    fun setClockColor(color: Int) {
        textColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        paint.color = textColor

        // Binary search / exact measurement to find the font size that fills 92% of width
        val targetWidth = w * 0.94f
        val maxHeight = h * 0.85f

        // Initial guess based on character count (5 chars: "00:00")
        var low = 20f
        var high = 300f
        var bestSize = 80f

        for (i in 0 until 12) {
            val mid = (low + high) / 2f
            paint.textSize = mid
            val measuredW = paint.measureText(timeString)
            if (measuredW < targetWidth) {
                bestSize = mid
                low = mid
            } else {
                high = mid
            }
        }

        // Ensure it doesn't exceed vertical height
        paint.textSize = bestSize
        paint.getTextBounds(timeString, 0, timeString.length, textBounds)
        if (textBounds.height() > maxHeight) {
            paint.textSize = bestSize * (maxHeight / textBounds.height())
        }

        // Center vertically & horizontally
        paint.getTextBounds(timeString, 0, timeString.length, textBounds)
        val yOffset = (h / 2f) - textBounds.exactCenterY()
        canvas.drawText(timeString, w / 2f, yOffset, paint)
    }
}

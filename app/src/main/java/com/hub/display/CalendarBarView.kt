package com.hub.display

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

/**
 * Vertical center divider with 8 boxes:
 * - Box 0 (Top): Month number (e.g. "11" for November) + Day number (e.g. "21")
 * - Box 1..7: Days of the week (MON, TUE, WED, THU, FRI, SAT, SUN)
 * The current day box lights up with bold highlight!
 */
class CalendarBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val days = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    private var currentDayIndex = 0 // 0 = MON ... 6 = SUN
    private var monthNumberStr = "01"
    private var dayNumberStr = "01"
    private var isNightMode = false

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    fun updateDate(cal: Calendar, night: Boolean) {
        isNightMode = night

        // Month is 0-indexed in Calendar
        val month = cal.get(Calendar.MONTH) + 1
        monthNumberStr = String.format("%02d", month)
        dayNumberStr = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))

        // DAY_OF_WEEK: Sunday = 1, Monday = 2, Tuesday = 3 ...
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        currentDayIndex = when (dow) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val paddingV = 16f
        val paddingH = 6f
        val totalAvailableH = h - (paddingV * 2)
        val boxH = totalAvailableH / 8f
        val boxW = w - (paddingH * 2)

        val activeColor = if (isNightMode) Color.parseColor("#E53935") else Color.WHITE
        val inactiveBorder = if (isNightMode) Color.parseColor("#330A0A") else Color.parseColor("#222222")
        val inactiveText = if (isNightMode) Color.parseColor("#661111") else Color.parseColor("#555555")

        for (i in 0 until 8) {
            val top = paddingV + (i * boxH) + 2f
            val bottom = top + boxH - 4f
            val left = paddingH
            val right = left + boxW

            val rect = RectF(left, top, right, bottom)
            val corner = 8f

            if (i == 0) {
                // Top box: Month number / Day number
                fillPaint.color = if (isNightMode) Color.parseColor("#441111") else Color.parseColor("#222222")
                canvas.drawRoundRect(rect, corner, corner, fillPaint)

                boxPaint.color = activeColor
                canvas.drawRoundRect(rect, corner, corner, boxPaint)

                textPaint.color = activeColor
                textPaint.textSize = boxW * 0.28f
                val cy = (top + bottom) / 2f
                val text = "$dayNumberStr/$monthNumberStr"
                canvas.drawText(text, rect.centerX(), cy + (textPaint.textSize / 3f), textPaint)
            } else {
                val dayIdx = i - 1
                val isToday = (dayIdx == currentDayIndex)

                if (isToday) {
                    fillPaint.color = activeColor
                    canvas.drawRoundRect(rect, corner, corner, fillPaint)

                    textPaint.color = Color.BLACK
                    textPaint.textSize = boxW * 0.32f
                } else {
                    boxPaint.color = inactiveBorder
                    canvas.drawRoundRect(rect, corner, corner, boxPaint)

                    textPaint.color = inactiveText
                    textPaint.textSize = boxW * 0.26f
                }

                val dayName = days[dayIdx]
                val cy = (top + bottom) / 2f
                canvas.drawText(dayName, rect.centerX(), cy + (textPaint.textSize / 3f), textPaint)
            }
        }
    }
}

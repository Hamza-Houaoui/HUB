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
 * Modern Slim Edge-to-Edge Calendar Divider:
 * - Extends continuously from very top edge (0px) to bottom divider (0px)
 * - Sleek minimal width (36dp)
 * - 8 micro-cards:
 *   - Box 0: Compact Date/Month ("21/11")
 *   - Boxes 1..7: "M", "T", "W", "T", "F", "S", "S" (or "MO", "TU"..)
 * - Highlighted day lights up with solid background card
 */
class CalendarBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val days = arrayOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")
    private var currentDayIndex = 0
    private var monthStr = "11"
    private var dayStr = "21"
    private var isNightMode = false

    private val boxBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
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

        val month = cal.get(Calendar.MONTH) + 1
        monthStr = String.format("%02d", month)
        dayStr = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))

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

        val sidePadding = 4f
        val boxW = w - (sidePadding * 2)
        val gap = 3f
        val totalGaps = 7 * gap
        val boxH = (h - totalGaps) / 8f
        val corner = 5f

        val activeColor = if (isNightMode) Color.parseColor("#E53935") else Color.WHITE
        val inactiveBorder = if (isNightMode) Color.parseColor("#2A0808") else Color.parseColor("#1C1C1C")
        val inactiveText = if (isNightMode) Color.parseColor("#661515") else Color.parseColor("#555555")

        for (i in 0 until 8) {
            val top = i * (boxH + gap)
            val bottom = top + boxH
            val left = sidePadding
            val right = left + boxW
            val rect = RectF(left, top, right, bottom)

            if (i == 0) {
                // Top Date Box (Month / Day)
                fillPaint.color = if (isNightMode) Color.parseColor("#380C0C") else Color.parseColor("#181818")
                canvas.drawRoundRect(rect, corner, corner, fillPaint)

                boxBorderPaint.color = activeColor
                canvas.drawRoundRect(rect, corner, corner, boxBorderPaint)

                textPaint.color = activeColor
                textPaint.textSize = boxW * 0.32f
                val cy = (top + bottom) / 2f
                val text = "$dayStr/$monthStr"
                canvas.drawText(text, rect.centerX(), cy + (textPaint.textSize / 3f), textPaint)
            } else {
                val dayIdx = i - 1
                val isToday = (dayIdx == currentDayIndex)

                if (isToday) {
                    fillPaint.color = activeColor
                    canvas.drawRoundRect(rect, corner, corner, fillPaint)

                    textPaint.color = Color.BLACK
                    textPaint.textSize = boxW * 0.38f
                } else {
                    boxBorderPaint.color = inactiveBorder
                    canvas.drawRoundRect(rect, corner, corner, boxBorderPaint)

                    textPaint.color = inactiveText
                    textPaint.textSize = boxW * 0.32f
                }

                val dayName = days[dayIdx]
                val cy = (top + bottom) / 2f
                canvas.drawText(dayName, rect.centerX(), cy + (textPaint.textSize / 3f), textPaint)
            }
        }
    }
}

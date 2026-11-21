package com.hub.display

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Transparent overlay that draws a 2×2 pixel checkerboard pattern.
 *
 * Half the pixels are black (OLED-off) at any moment. Every [flipPattern]
 * call inverts the pattern so each physical pixel is ON only 50 % of the
 * time, dramatically reducing AMOLED burn-in while keeping the content
 * readable underneath.
 */
class BurnInOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var offset = false

    // Pattern A: top-left & bottom-right pixels are black
    private val patternA: Bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).apply {
        setPixel(0, 0, Color.BLACK)
        setPixel(1, 1, Color.BLACK)
        setPixel(1, 0, Color.TRANSPARENT)
        setPixel(0, 1, Color.TRANSPARENT)
    }

    // Pattern B: inverted
    private val patternB: Bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).apply {
        setPixel(0, 0, Color.TRANSPARENT)
        setPixel(1, 1, Color.TRANSPARENT)
        setPixel(1, 0, Color.BLACK)
        setPixel(0, 1, Color.BLACK)
    }

    private val shaderA = BitmapShader(patternA, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val shaderB = BitmapShader(patternB, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

    private val paint = Paint().apply {
        shader = shaderA
        isAntiAlias = false
        isFilterBitmap = false
    }

    init {
        // Never intercept touch events
        isClickable = false
        isFocusable = false
    }

    /** Swap the checkerboard pattern (call every ~5 s). */
    fun flipPattern() {
        offset = !offset
        paint.shader = if (offset) shaderB else shaderA
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean = false
}

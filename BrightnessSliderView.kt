package com.bkz.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import kotlin.math.abs

val Context.screenBrightness get() = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 125)

fun Context.setScreenBrightness(brightness: Int) {
    runCatching {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
    }
}

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

class BrightnessSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var viewRectF = RectF()
    private var progressRectF = RectF()

    @ColorInt
    var backColor: Int = color(R.color.color_46)

    @ColorInt
    var progressColor: Int = color(R.color.white)

    @ColorInt
    var sunColor: Int = color(R.color.color_green)

    var max: Int = 255
    private var progress: Int = 0

    var onProgressChange: ((Int) -> Unit)? = null

    private var sunRadius: Float = 0f
    private var sunMargin: Float = 0f
    private val shineWidth: Float = 2f.dp
    private val shineLength: Float = 4f.dp
    private val corners: Float = 10f
    private var downY = 0f
    private var moving: Boolean = false

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = shineWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        viewRectF.set(0f, 0f, width.toFloat(), height.toFloat())
        sunRadius = width / 6f
        sunMargin = width / 3f
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val restoreToCount = c.save()
        paint.color = backColor
        c.drawRoundRect(viewRectF, corners, corners, paint)
        paint.color = progressColor
        val progressHeight = progress.toFloat() / max * viewRectF.bottom
        progressRectF.set(0f, viewRectF.bottom - progressHeight, viewRectF.right, viewRectF.bottom)
        c.drawRoundRect(progressRectF, corners, corners, paint)
        paint.color = sunColor
        val cx = viewRectF.right / 2
        val cy = viewRectF.bottom - sunRadius - sunMargin
        c.drawCircle(cx, cy, sunRadius, paint)
        c.translate(cx, cy)
        for (i in 0..10) {
            c.drawLine(sunRadius, sunRadius, sunRadius + shineLength, sunRadius + shineLength, paint)
            c.rotate(36f)
        }
        c.restoreToCount(restoreToCount)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                moving = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (moving || abs((downY) - event.y) > ViewConfiguration.getTouchSlop()) {
                    moving = true
                    onProgressChange(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (moving || (abs(downY - event.y) < ViewConfiguration.getTouchSlop())) {
                    onProgressChange(event)
                }
            }
            else -> {}
        }
        return true
    }

    private fun onProgressChange(event: MotionEvent) {
        val newProgress = ((viewRectF.bottom - event.y) / viewRectF.bottom * max).toInt()
        val progress = if (newProgress > max) max else if (newProgress < 0) 0 else newProgress
        onProgressChange?.invoke(progress)
    }

    fun refreshProgress(_progress: Int) {
        val progress = if (_progress > max) max else _progress
        if (this.progress == progress) {
            return
        }
        this.progress = progress
        invalidate()
    }
}

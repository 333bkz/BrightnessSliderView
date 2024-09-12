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
    var bgColor: Int = Color.GRAY

    @ColorInt
    var progressColor: Int = Color.WHITE

    @ColorInt
    var sunColor: Int = Color.BLACK

    private val max: Int = 254
    private var progress: Int = 0
    private var brightness: Int = 0

    var onProgressChange: ((Int) -> Unit)? = null

    private var sunRadius: Float = 0f
    private var sunMargin: Float = 0f
    private val shineWidth: Float = 2f.dp
    private val shineLength: Float = 3f.dp
    private val corners: Float = 16f.dp
    private var downY = 0f
    private var moving: Boolean = false
    private val mode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

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
        sunRadius = width / 10f
        sunMargin = width / 3f
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        //background
        val sc = c.saveLayer(viewRectF, paint)
        paint.color = bgColor
        paint.xfermode = null
        c.drawRoundRect(viewRectF, corners, corners, paint)
        //progress
        var progressHeight = progress.toFloat() / max * viewRectF.bottom
        if (progressHeight < 5) {
            progressHeight = 0f
        }
        val top = viewRectF.bottom - progressHeight
        progressRectF.set(0f, top, viewRectF.right, viewRectF.bottom)
        paint.color = progressColor
        paint.xfermode = mode
        c.drawRoundRect(progressRectF, corners, corners, paint)
        //sun
        paint.color = sunColor
        val cx = viewRectF.right / 2
        val cy = viewRectF.bottom - sunRadius - sunMargin
        paint.xfermode = null
        c.drawCircle(cx, cy, sunRadius, paint)
        c.translate(cx, cy)
        for (i in 0..10) {
            c.drawLine(sunRadius, sunRadius, sunRadius + shineLength, sunRadius + shineLength, paint)
            c.rotate(36f)
        }
        c.restoreToCount(sc)
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
                if (moving || abs((downY) - event.y) > ViewConfiguration.get(context).scaledTouchSlop) {
                    moving = true
                    onProgressChange(event)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (moving || (abs(downY - event.y) < ViewConfiguration.get(context).scaledTouchSlop)) {
                    onProgressChange(event)
                }
            }

            else -> {}
        }
        return true
    }

    private fun onProgressChange(event: MotionEvent) {
        val newProgress = ((viewRectF.bottom - event.y) / viewRectF.bottom * max).toInt()
        val progress = if (newProgress < 1) 1 else if (newProgress > max) max else newProgress
        if (brightness != progress) {
            brightness = progress
            onProgressChange?.invoke(progress)
        }
    }

    fun setProgress(value: Int) {
        val progress = if (value < 1) 1 else if (value > max) max else value
        if (this.progress != progress) {
            this.progress = progress
            invalidate()
        }
    }
}

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
    var backColor: Int = Color.GRAY

    @ColorInt
    var progressColor: Int = Color.WHITE

    @ColorInt
    var sunColor: Int = Color.GREEN

    @FloatRange(from = 0.0, to = Double.MAX_VALUE)
    var max: Float = 255f

    @FloatRange(from = 0.0, to = Double.MAX_VALUE)
    var progress: Float = context.screenBrightness.toFloat()

    private var sunRadius: Float = 0f
    private var sunMargin: Float = 0f
    private val shineWidth: Float = 2f.dp
    private val shineLength: Float = 4f.dp
    private val corners: Float = 10f

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
        val progressHeight = progress / max * viewRectF.bottom
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

    private var downY = 0f
    private var isMove = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = event.y
                isMove = false
            }
            MotionEvent.ACTION_MOVE -> {
                val diff = abs(downY - event.y)
                if (diff > 10) {
                    isMove = true
                    val percentage = (downY - event.y) / viewRectF.bottom
                    downY = event.y
                    val offset = percentage * max
                    val newProgress = progress + offset
                    progress = if (newProgress > max) max else if (newProgress < 0) 0f else newProgress
                    context?.setScreenBrightness(progress.toInt())
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isMove) {
                    val diff = abs(downY - event.y)
                    if (diff < 10) {
                        val percentage = (viewRectF.bottom - event.y) / viewRectF.bottom
                        val newProgress = percentage * max
                        progress = if (newProgress > max) max else if (newProgress < 0) 0f else newProgress
                        context?.setScreenBrightness(progress.toInt())
                    }
                }
            }
        }
        invalidate()
        return true
    }
}

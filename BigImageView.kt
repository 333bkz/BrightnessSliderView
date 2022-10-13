package com.bkz.demo

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import java.io.InputStream

class BigImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr),
    GestureDetector.OnGestureListener,
    View.OnTouchListener {

    private var _imageWidth = 0
    private var _imageHeight = 0
    private var _width = 0
    private var _height = 0
    private val _rect = Rect()
    private val _option = BitmapFactory.Options()
    private val _detector = GestureDetector(context, this)
    private val _scroller = Scroller(context)
    private var _decoder: BitmapRegionDecoder? = null
    private var _bitmap: Bitmap? = null

    init {
        setOnTouchListener(this)
    }

    fun setImage(input: InputStream) {
        _option.inJustDecodeBounds = true
        _option.inMutable = true
        _option.inBitmap = _bitmap
        _option.inPreferredConfig = Bitmap.Config.RGB_565
        BitmapFactory.decodeStream(input, null, _option)
        _imageWidth = _option.outWidth
        _imageHeight = _option.outHeight
        _option.inJustDecodeBounds = false
        runCatching {
            _decoder = BitmapRegionDecoder.newInstance(input, false)
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureWidth(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)
        setMeasuredDimension(width, height)
        _width = _imageWidth.coerceAtMost(width)
        _height = _imageHeight.coerceAtMost(height)
        _rect.top = 0
        _rect.left = 0
        _rect.right = _width
        _rect.bottom = _height
    }

    private fun measureWidth(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        var width = suggestedMinimumWidth
        when (specMode) {
            MeasureSpec.AT_MOST -> {
                width = _imageWidth.coerceAtMost(specSize)
            }
            MeasureSpec.EXACTLY -> {
                width = specSize
            }
            MeasureSpec.UNSPECIFIED -> {
                width = width.coerceAtLeast(specSize)
            }
        }
        return width
    }

    private fun measureHeight(measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        var height = suggestedMinimumHeight
        when (specMode) {
            MeasureSpec.AT_MOST -> {
                height = _imageHeight.coerceAtMost(specSize)
            }
            MeasureSpec.EXACTLY -> {
                height = specSize
            }
            MeasureSpec.UNSPECIFIED -> {
                height = height.coerceAtLeast(specSize)
            }
        }
        return height
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.YELLOW)
        _decoder?.decodeRegion(_rect, _option)?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, x: Float, y: Float): Boolean {
        _rect.offset(x.toInt(), y.toInt())
        if (_rect.top < 0) {
            _rect.top = 0
            _rect.bottom = _height
        }
        if (_rect.bottom > _imageHeight) {
            _rect.bottom = _imageHeight
            _rect.top = _imageHeight - _height
        }
        if (_rect.left < 0) {
            _rect.left = 0
            _rect.right = _width
        }
        if (_rect.right > _imageWidth) {
            _rect.right = _imageWidth
            _rect.left = _imageWidth - _width
        }
        invalidate()
        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        if (!_scroller.isFinished) {
            _scroller.forceFinished(true)
        }
        return true
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, x: Float, y: Float): Boolean {
        _scroller.fling(
            _rect.left, _rect.top, -x.toInt(), -y.toInt(),
            0, _imageWidth - _width,
            0, _imageHeight - _height
        )
        return true
    }

    override fun computeScroll() {
        if (_scroller.isFinished) {
            return
        }
        if (_scroller.computeScrollOffset()) {
            _rect.left = _scroller.currX
            _rect.right = _scroller.currX + _width
            _rect.top = _scroller.currY
            _rect.bottom = _scroller.currY + _height
            invalidate()
        }
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }


    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onTouch(v: View, e: MotionEvent): Boolean {
        return _detector.onTouchEvent(e)
    }
}
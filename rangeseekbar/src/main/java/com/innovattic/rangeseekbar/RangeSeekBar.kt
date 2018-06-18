package com.innovattic.rangeseekbar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class RangeSeekBar : View {
    private val trackPaint: Paint
    private val selectedTrackPaint: Paint
    private val bitmapPaint = Paint()

    private var selectedThumb: Int = THUMB_NONE
    private var offset: Int = 0

    var touchRadius: Int
    var minThumbDrawable: Drawable
    set(value) {
        field = value
        minThumbBitmap = getBitmapFromDrawable(minThumbDrawable)
    }
    var maxThumbDrawable: Drawable
    set(value) {
        field = value
        maxThumbBitmap = getBitmapFromDrawable(maxThumbDrawable)
    }
    private lateinit var minThumbBitmap: Bitmap
    private lateinit var maxThumbBitmap: Bitmap

    var sidePadding: Int
    var minWindow: Int

    var max = 100
        set(value) {
            field = value
            minThumb = 0
            maxThumb = field
        }
    var minThumb: Int = 0
        private set
    var maxThumb: Int = max
        private set
    var seekBarChangeListener: SeekBarChangeListener? = null

    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        val res = context.resources
        val defaultTrackWidth = res.getDimensionPixelSize(R.dimen.rsb_trackDefaultWidth)
        val defaultSidePadding = res.getDimensionPixelSize(R.dimen.rsb_defaultSidePadding)
        val defaultTouchRadius = res.getDimensionPixelSize(R.dimen.rsb_touchRadius)
        val defaultTrackColor = ContextCompat.getColor(context, R.color.rsb_trackDefaultColor)
        val defaultSelectedTrackColor = ContextCompat.getColor(context, R.color.rsb_trackSelectedDefaultColor)

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0)
        try {
            max = a.getInteger(R.styleable.RangeSeekBar_rsb_max, 100)
            minWindow = max(a.getInteger(R.styleable.RangeSeekBar_rsb_minWindow, 1), 1)
            sidePadding = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_sidePadding, defaultSidePadding)
            touchRadius = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_touchRadius, defaultTouchRadius)
            trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_trackWidth, defaultTrackWidth).toFloat()
                color = a.getColor(R.styleable.RangeSeekBar_rsb_trackColor, defaultTrackColor)
            }
            selectedTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_trackSelectedWidth, defaultTrackWidth).toFloat()
                color = a.getColor(R.styleable.RangeSeekBar_rsb_trackSelectedColor, defaultSelectedTrackColor)
            }
            minThumbDrawable = if(a.hasValue(R.styleable.RangeSeekBar_rsb_minThumbDrawable)) {
                a.getDrawable(R.styleable.RangeSeekBar_rsb_minThumbDrawable)
            } else {
                ContextCompat.getDrawable(context, R.drawable.rsb_bracket_min)!!
            }
            maxThumbDrawable = if(a.hasValue(R.styleable.RangeSeekBar_rsb_maxThumbDrawable)) {
                a.getDrawable(R.styleable.RangeSeekBar_rsb_maxThumbDrawable)
            } else {
                ContextCompat.getDrawable(context, R.drawable.rsb_bracket_max)!!
            }
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (width <= 0 || height <= 0) {
            return
        }
        val newHeight = max(minThumbBitmap.height, maxThumbBitmap.height)
        if (newHeight > height) {
            layoutParams.height = newHeight
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paddingLeft = this.paddingLeft + sidePadding
        val paddingRight = this.paddingRight + sidePadding
        val width = width - paddingLeft - paddingRight
        val verticalCenter = height / 2f
        val x1 = paddingLeft + (minThumb / max.toFloat()) * width
        val x2 = paddingLeft + (maxThumb / max.toFloat()) * width
        canvas.drawLine(paddingLeft + 0f, verticalCenter, paddingLeft + width.toFloat(), verticalCenter, trackPaint)
        canvas.drawLine(x1, verticalCenter, x2, verticalCenter, selectedTrackPaint)
        canvas.drawBitmap(minThumbBitmap, x1, (height - minThumbBitmap.height) / 2f, bitmapPaint)
        canvas.drawBitmap(maxThumbBitmap, x2 - maxThumbBitmap.width.toFloat(), (height - maxThumbBitmap.height) / 2f, bitmapPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var changed = false
        val paddingLeft = this.paddingLeft + sidePadding
        val paddingRight = this.paddingRight + sidePadding
        val width = width - paddingLeft - paddingRight
        val mx = when {
            event.x < paddingLeft -> 0
            event.x in paddingLeft..this.width - paddingRight -> ((event.x - paddingLeft) / width * max).toInt()
            else -> max
        }
        val leftThumbX = (paddingLeft + (minThumb / max.toFloat() * width)).toInt()
        val rightThumbX = (paddingLeft + (maxThumb / max.toFloat() * width)).toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if(isInsideRadius(event, leftThumbX, height / 2)) {
                    selectedThumb = THUMB_MIN
                    offset = mx - minThumb
                    changed = true
                    seekBarChangeListener?.onStartedSeeking()
                } else if(isInsideRadius(event, rightThumbX, height / 2)) {
                    selectedThumb = THUMB_MAX
                    offset = maxThumb - mx
                    changed = true
                    seekBarChangeListener?.onStartedSeeking()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedThumb == THUMB_MIN) {
                    minThumb = max(min(mx - offset, max - minWindow), 0)
                    changed = true
                } else if (selectedThumb == THUMB_MAX) {
                    maxThumb = min(max(mx + offset, minWindow), max)
                    changed = true
                }
            }
            MotionEvent.ACTION_UP -> {
                selectedThumb = THUMB_NONE
                seekBarChangeListener?.onStoppedSeeking()
            }
        }
        if (selectedThumb == THUMB_MAX) {
            if (maxThumb <= minThumb + minWindow) {
                minThumb = maxThumb - minWindow
            }
        } else if (selectedThumb == THUMB_MIN) {
            if (minThumb > maxThumb - minWindow) {
                maxThumb = minThumb + minWindow
            }
        }
        if (changed) {
            invalidate()
            seekBarChangeListener?.onValueChanged(minThumb, maxThumb)
            return true
        }
        return false
    }

    private fun isInsideRadius(event: MotionEvent, cx: Int, cy: Int): Boolean {
        val dx = event.x - cx
        val dy = event.y - cy
        return (dx * dx) + (dy * dy) < (touchRadius * touchRadius)
    }

    companion object {
        private const val THUMB_NONE = 0
        private const val THUMB_MIN = 1
        private const val THUMB_MAX = 2
    }

    interface SeekBarChangeListener {
        fun onStartedSeeking()
        fun onStoppedSeeking()
        fun onValueChanged(leftThumbValue: Int, rightThumbValue: Int)
    }
}
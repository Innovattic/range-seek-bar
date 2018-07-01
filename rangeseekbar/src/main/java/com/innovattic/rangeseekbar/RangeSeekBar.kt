package com.innovattic.rangeseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class RangeSeekBar : View {
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private var selectedThumb: Int = THUMB_NONE
    private var offset: Int = 0

    var trackThickness: Int
    var trackSelectedThickness: Int
    var trackColor: Int
    var trackSelectedColor: Int

    var touchRadius: Int

    var minThumbDrawable: Drawable
    var maxThumbDrawable: Drawable

    var sidePadding: Int
    var minWindow: Int

    var max: Int
        set(value) {
            field = value
            minThumbValue = 0
            maxThumbValue = field
        }
    var minThumbValue: Int = 0
        private set
    var maxThumbValue: Int = 0
        private set
    var seekBarChangeListener: SeekBarChangeListener? = null

    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        val res = context.resources
        val defaultTrackThickness = res.getDimensionPixelSize(R.dimen.rsb_trackDefaultThickness)
        val defaultSidePadding = res.getDimensionPixelSize(R.dimen.rsb_defaultSidePadding)
        val defaultTouchRadius = res.getDimensionPixelSize(R.dimen.rsb_touchRadius)
        val defaultTrackColor = ContextCompat.getColor(context, R.color.rsb_trackDefaultColor)
        val defaultSelectedTrackColor = ContextCompat.getColor(context,
                R.color.rsb_trackSelectedDefaultColor)
        val defaultMinThumb = ContextCompat.getDrawable(context, R.drawable.rsb_bracket_min)!!
        val defaultMaxThumb = ContextCompat.getDrawable(context, R.drawable.rsb_bracket_max)!!

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0)
        try {
            max = extractMaxValue(a)
            minWindow = extractMinWindow(a)
            sidePadding = extractSidePadding(a, defaultSidePadding)
            touchRadius = extractTouchRadius(a, defaultTouchRadius)
            trackThickness = extractTrackThickness(a, defaultTrackThickness)
            trackSelectedThickness = extractTrackSelectedThickness(a, defaultTrackThickness)
            trackColor = extractTrackColor(a, defaultTrackColor)
            trackSelectedColor = extractTrackSelectedColor(a, defaultSelectedTrackColor)
            minThumbDrawable = extractMinThumbDrawable(a, defaultMinThumb)
            maxThumbDrawable = extractMaxThumbDrawable(a, defaultMaxThumb)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
                measureHeight(heightMeasureSpec))
    }

    @SuppressLint("SwitchIntDef")
    private fun measureHeight(measureSpec: Int): Int {
        val maxHeight = max(minThumbDrawable.intrinsicHeight, maxThumbDrawable.intrinsicHeight)
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            else -> maxHeight + sidePadding
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val paddingLeft = this.paddingLeft + sidePadding
        val paddingRight = this.paddingRight + sidePadding
        val width = width - paddingLeft - paddingRight
        val verticalCenter = height / 2f
        val minimumX = paddingLeft + (minThumbValue / max.toFloat()) * width
        val maximumX = paddingLeft + (maxThumbValue / max.toFloat()) * width

        // Draw full track
        updatePaint(trackThickness, trackColor)
        canvas.drawLine(paddingLeft + 0f, verticalCenter, paddingLeft + width.toFloat(),
                verticalCenter, trackPaint)

        // Draw selected range of the track
        updatePaint(trackSelectedThickness, trackSelectedColor)
        canvas.drawLine(minimumX, verticalCenter, maximumX, verticalCenter, trackPaint)

        // Draw thumb at minimumX position
        minThumbDrawable.drawAtPosition(canvas, minimumX.toInt())

        // Draw thumb at maximumX position
        maxThumbDrawable.drawAtPosition(canvas, maximumX.toInt() - maxThumbDrawable.intrinsicWidth)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var changed = false
        val paddingLeft = this.paddingLeft + sidePadding
        val paddingRight = this.paddingRight + sidePadding
        val width = width - paddingLeft - paddingRight
        val mx = when {
            event.x < paddingLeft -> 0
            event.x in paddingLeft..this.width - paddingRight ->
                ((event.x - paddingLeft) / width * max).toInt()
            else -> max
        }
        val leftThumbX = (paddingLeft + (minThumbValue / max.toFloat() * width)).toInt()
        val rightThumbX = (paddingLeft + (maxThumbValue / max.toFloat() * width)).toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isInsideRadius(event, leftThumbX, height / 2)) {
                    selectedThumb = THUMB_MIN
                    offset = mx - minThumbValue
                    changed = true
                    seekBarChangeListener?.onStartedSeeking()
                } else if (isInsideRadius(event, rightThumbX, height / 2)) {
                    selectedThumb = THUMB_MAX
                    offset = maxThumbValue - mx
                    changed = true
                    seekBarChangeListener?.onStartedSeeking()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedThumb == THUMB_MIN) {
                    minThumbValue = max(min(mx - offset, max - minWindow), 0)
                    changed = true
                } else if (selectedThumb == THUMB_MAX) {
                    maxThumbValue = min(max(mx + offset, minWindow), max)
                    changed = true
                }
            }
            MotionEvent.ACTION_UP -> {
                selectedThumb = THUMB_NONE
                seekBarChangeListener?.onStoppedSeeking()
            }
        }
        if (selectedThumb == THUMB_MAX) {
            if (maxThumbValue <= minThumbValue + minWindow) {
                minThumbValue = maxThumbValue - minWindow
            }
        } else if (selectedThumb == THUMB_MIN) {
            if (minThumbValue > maxThumbValue - minWindow) {
                maxThumbValue = minThumbValue + minWindow
            }
        }
        if (changed) {
            invalidate()
            seekBarChangeListener?.onValueChanged(minThumbValue, maxThumbValue)
            return true
        }
        return false
    }

    private fun isInsideRadius(event: MotionEvent, cx: Int, cy: Int): Boolean {
        val dx = event.x - cx
        val dy = event.y - cy
        return (dx * dx) + (dy * dy) < (touchRadius * touchRadius)
    }

    private fun Drawable.drawAtPosition(canvas: Canvas, position: Int) {
        val top = (height - intrinsicHeight) / 2
        setBounds(position, top, position + intrinsicWidth, top + intrinsicHeight)
        draw(canvas)
    }

    private fun updatePaint(strokeWidth: Int, color: Int) {
        trackPaint.strokeWidth = strokeWidth.toFloat()
        trackPaint.color = color
    }

    private fun extractMaxThumbDrawable(a: TypedArray, defaultValue: Drawable): Drawable {
        if (a.hasValue(R.styleable.RangeSeekBar_rsb_maxThumbDrawable)) {
            return a.getDrawable(R.styleable.RangeSeekBar_rsb_maxThumbDrawable)
        }
        return defaultValue
    }

    private fun extractMinThumbDrawable(a: TypedArray, defaultValue: Drawable): Drawable {
        if (a.hasValue(R.styleable.RangeSeekBar_rsb_minThumbDrawable)) {
            return a.getDrawable(R.styleable.RangeSeekBar_rsb_minThumbDrawable)
        }
        return defaultValue
    }

    private fun extractTrackSelectedColor(a: TypedArray, defaultValue: Int): Int {
        return a.getColor(R.styleable.RangeSeekBar_rsb_trackSelectedColor, defaultValue)
    }

    private fun extractTrackColor(a: TypedArray, defaultValue: Int): Int {
        return a.getColor(R.styleable.RangeSeekBar_rsb_trackColor, defaultValue)
    }

    private fun extractTrackSelectedThickness(a: TypedArray, defaultValue: Int): Int {
        return a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_trackSelectedThickness, defaultValue)
    }

    private fun extractTrackThickness(a: TypedArray, defaultValue: Int): Int {
        return a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_trackThickness, defaultValue)
    }

    private fun extractTouchRadius(a: TypedArray, defaultValue: Int): Int {
        return a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_touchRadius, defaultValue)
    }

    private fun extractSidePadding(a: TypedArray, defaultValue: Int): Int {
        return a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_sidePadding, defaultValue)
    }

    private fun extractMinWindow(a: TypedArray): Int {
        return max(a.getInteger(R.styleable.RangeSeekBar_rsb_minWindow, 1), 1)
    }

    private fun extractMaxValue(a: TypedArray): Int {
        return a.getInteger(R.styleable.RangeSeekBar_rsb_max, 100)
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
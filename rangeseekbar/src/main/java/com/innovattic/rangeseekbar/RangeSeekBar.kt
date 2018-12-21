package com.innovattic.rangeseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min

/**
 * RangeSeekBar is a flexible custom view that holds a minimum and maximum range. The user can touch
 * either one of minimum and maximum thumbs and drag them to change their value. It is also possible
 * to change the range with code using [setMinThumbValue] and [setMaxThumbValue] functions.
 *
 * @author Mohammad Mirrajabi
 */
open class RangeSeekBar : View {
	// region Properties
	
	/**
	 * The paint to draw the horizontal tracks with.
	 */
	private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		style = Paint.Style.STROKE
	}
	
	/**
	 * Holds the value of selected thumb while dragging it.
	 */
	private var selectedThumb: Int = THUMB_NONE
	
	/**
	 * Holds the amount of changed value of a thumb while dragging it.
	 */
	private var offset: Int = 0
	
	/**
	 * The thickness of the horizontal track.
	 */
	var trackThickness: Int
	
	/**
	 * The thickness of the selected range of horizontal track.
	 */
	var trackSelectedThickness: Int
	
	/**
	 * Color of horizontal track.
	 */
	var trackColor: Int
	
	/**
	 * Color of the selected range of horizontal track.
	 */
	var trackSelectedColor: Int
	
	/**
	 * The acceptable touch radius around thumbs in pixels.
	 */
	var touchRadius: Int
	
	/**
	 * The drawable to draw min thumb with.
	 */
	var minThumbDrawable: Drawable
	
	/**
	 * The drawable to draw max thumb with.
	 */
	var maxThumbDrawable: Drawable
	
	/**
	 * Side padding for view, by default 16dp on the left and right.
	 */
	var sidePadding: Int
	
	/**
	 * If the track should have rounded caps.
	 */
	var trackRoundedCaps: Boolean = false
	
	/**
	 * If the selected range track should have rounded caps.
	 */
	var trackSelectedRoundedCaps: Boolean = false
	
	/**
	 * Pixel offset of the min thumb
	 */
	var minThumbOffset: Point
	
	/**
	 * Pixel offset of the max thumb
	 */
	var maxThumbOffset: Point
	
	/**
	 * The minimum range to be selected. It should at least be 1.
	 */
	var minRange: Int
		set(value) {
			field = max(value, 1)
		}
	
	/**
	 * The maximum value of thumbs which can also be considered as the maximum possible range.
	 */
	var max: Int
		set(value) {
			field = value
			minThumbValue = 0
			maxThumbValue = field
		}
	/**
	 * Holds the value of min thumb.
	 */
	private var minThumbValue: Int = 0
	
	/**
	 * Holds the value of max thumb.
	 */
	private var maxThumbValue: Int = 0
	
	/**
	 * Holds the last value of [minThumbValue] in order to send the callback updates
	 * only if it is necessary.
	 */
	private var lastMinThumbValue = minThumbValue
	
	/**
	 * Holds the last value of [maxThumbValue] in order to send the callback updates
	 * only if it is necessary.
	 */
	private var lastMaxThumbValue = maxThumbValue
	/**
	 * A callback receiver for view changes.
	 */
	var seekBarChangeListener: SeekBarChangeListener? = null
	// endregion
	
	@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
		val res = context.resources
		val defaultTrackThickness = res.getDimensionPixelSize(R.dimen.rsb_trackDefaultThickness)
		val defaultSidePadding = res.getDimensionPixelSize(R.dimen.rsb_defaultSidePadding)
		val defaultTouchRadius = res.getDimensionPixelSize(R.dimen.rsb_touchRadius)
		val defaultTrackColor = ContextCompat.getColor(context, R.color.rsb_trackDefaultColor)
		val defaultSelectedTrackColor = ContextCompat.getColor(context, R.color.rsb_trackSelectedDefaultColor)
		val defaultMinThumb = ContextCompat.getDrawable(context, R.drawable.rsb_bracket_min)!!
		val defaultMaxThumb = ContextCompat.getDrawable(context, R.drawable.rsb_bracket_max)!!
		
		val a = context.theme.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0)
		try {
			max = extractMaxValue(a)
			minRange = extractMinRange(a)
			sidePadding = extractSidePadding(a, defaultSidePadding)
			touchRadius = extractTouchRadius(a, defaultTouchRadius)
			trackThickness = extractTrackThickness(a, defaultTrackThickness)
			trackSelectedThickness = extractTrackSelectedThickness(a, defaultTrackThickness)
			trackColor = extractTrackColor(a, defaultTrackColor)
			trackSelectedColor = extractTrackSelectedColor(a, defaultSelectedTrackColor)
			minThumbDrawable = extractMinThumbDrawable(a, defaultMinThumb)
			maxThumbDrawable = extractMaxThumbDrawable(a, defaultMaxThumb)
			minThumbOffset = extractMinThumbOffset(a)
			maxThumbOffset = extractMaxThumbOffset(a)
			trackRoundedCaps = extractTrackRoundedCaps(a)
			trackSelectedRoundedCaps = extractTrackSelectedRoundedCaps(a)
		} finally {
			a.recycle()
		}
	}
	
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), measureHeight(heightMeasureSpec))
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
		updatePaint(trackThickness, trackColor, trackRoundedCaps)
		canvas.drawLine(paddingLeft + 0f, verticalCenter, paddingLeft + width.toFloat(), verticalCenter, trackPaint)
		
		// Draw selected range of the track
		updatePaint(trackSelectedThickness, trackSelectedColor, trackSelectedRoundedCaps)
		canvas.drawLine(minimumX, verticalCenter, maximumX, verticalCenter, trackPaint)
		
		// Draw thumb at minimumX position
		minThumbDrawable.drawAtPosition(canvas, minimumX.toInt(), minThumbOffset)
		
		// Draw thumb at maximumX position
		maxThumbDrawable.drawAtPosition(canvas, maximumX.toInt() - maxThumbDrawable.intrinsicWidth, maxThumbOffset)
	}
	
	override fun onTouchEvent(event: MotionEvent): Boolean {
		var changed = false
		val paddingLeft = this.paddingLeft + sidePadding
		val paddingRight = this.paddingRight + sidePadding
		val width = width - paddingLeft - paddingRight
		val mx = when {
			event.x < paddingLeft -> 0
			paddingLeft <= event.x && event.x <=(this.width - paddingRight) -> ((event.x - paddingLeft) / width * max).toInt()
			else -> max
		}
		val leftThumbX = (paddingLeft + (minThumbValue / max.toFloat() * width)).toInt()
		val rightThumbX = (paddingLeft + (maxThumbValue / max.toFloat() * width)).toInt()
		when (event.action) {
			MotionEvent.ACTION_DOWN -> {
				if (isInsideRadius(event, leftThumbX, height / 2, touchRadius)) {
					selectedThumb = THUMB_MIN
					offset = mx - minThumbValue
					changed = true
					parent.requestDisallowInterceptTouchEvent(true)
					seekBarChangeListener?.onStartedSeeking()
					isPressed = true
				} else if (isInsideRadius(event, rightThumbX, height / 2, touchRadius)) {
					selectedThumb = THUMB_MAX
					offset = maxThumbValue - mx
					changed = true
					parent.requestDisallowInterceptTouchEvent(true)
					seekBarChangeListener?.onStartedSeeking()
					isPressed = true
				}
			}
			MotionEvent.ACTION_MOVE -> {
				if (selectedThumb == THUMB_MIN) {
					minThumbValue = max(min(mx - offset, max - minRange), 0)
					changed = true
				} else if (selectedThumb == THUMB_MAX) {
					maxThumbValue = min(max(mx + offset, minRange), max)
					changed = true
				}
			}
			MotionEvent.ACTION_UP -> {
				selectedThumb = THUMB_NONE
				seekBarChangeListener?.onStoppedSeeking()
				isPressed = false
			}
		}
		if (selectedThumb == THUMB_MAX) {
			if (maxThumbValue <= minThumbValue + minRange) {
				minThumbValue = maxThumbValue - minRange
			}
		} else if (selectedThumb == THUMB_MIN) {
			if (minThumbValue > maxThumbValue - minRange) {
				maxThumbValue = minThumbValue + minRange
			}
		}
		keepMinWindow(selectedThumb)
		
		if (!changed) {
			return false
		}
		
		invalidate()
		if (lastMinThumbValue != minThumbValue || lastMaxThumbValue != maxThumbValue) {
			lastMinThumbValue = minThumbValue
			lastMaxThumbValue = maxThumbValue
			seekBarChangeListener?.onValueChanged(minThumbValue, maxThumbValue)
		}
		return true
	}
	
	// region Public functions
	
	/**
	 * Updates the value of minimum thumb and redraws the view.
	 */
	fun setMinThumbValue(value: Int) {
		minThumbValue = value
		keepMinWindow(THUMB_MIN)
		invalidate()
	}
	
	/**
	 * @return the current minimum value of selected range.
	 */
	fun getMinThumbValue() = minThumbValue
	
	/**
	 * Updates the value of maximum thumb and redraws the view.
	 */
	fun setMaxThumbValue(value: Int) {
		maxThumbValue = value
		keepMinWindow(THUMB_MAX)
		invalidate()
	}
	
	/**
	 * @return the current maximum value of selected range.
	 */
	fun getMaxThumbValue() = maxThumbValue
	// endregion
	
	// region Private functions
	
	/**
	 * This function will make sure that while changing the value of a thumb, the other thumb's
	 * value will also be changed if necessary to keep the min window for range.
	 *
	 * @param base the thumb that should be the base for keeping min window.
	 */
	private fun keepMinWindow(base: Int) {
		if (base == THUMB_MAX) {
			if (maxThumbValue <= minThumbValue + minRange) {
				minThumbValue = maxThumbValue - minRange
			}
		} else if (base == THUMB_MIN) {
			if (minThumbValue > maxThumbValue - minRange) {
				maxThumbValue = minThumbValue + minRange
			}
		}
	}
	
	/**
	 * Checks if the given motion event is inside the circle with a radius of [radius] and
	 * a center point of {[cx],[cy]}.
	 */
	private fun isInsideRadius(event: MotionEvent, cx: Int, cy: Int, radius: Int): Boolean {
		val dx = event.x - cx
		val dy = event.y - cy
		return (dx * dx) + (dy * dy) < (radius * radius)
	}
	
	/**
	 * Updates the stroke width and color of the paint which is used for drawing tracks.
	 */
	private fun updatePaint(strokeWidth: Int, color: Int, roundedCaps: Boolean) {
		trackPaint.strokeWidth = strokeWidth.toFloat()
		trackPaint.color = color
		trackPaint.strokeCap = if (roundedCaps) Paint.Cap.ROUND else Paint.Cap.SQUARE
	}
	
	/**
	 * Calculates the height of the view based on the view parameters.
	 * If the height is set to []
	 */
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
	
	// region Extension functions
	/**
	 * Calculates and sets the drawing bounds for drawable and draws it on canvas.
	 *
	 * @param canvas the canvas to draw on
	 * @param position position of the drawable's left edge in horizontal axis (in pixels)
	 * @param offset the pixel offset of the drawable
	 */
	private fun Drawable.drawAtPosition(canvas: Canvas, position: Int, offset: Point = Point(0, 0)) {
		val left = position + offset.x
		val top = ((height - intrinsicHeight) / 2) + offset.y
		setBounds(left, top, left + intrinsicWidth, top + intrinsicHeight)
		draw(canvas)
	}
	// endregion
	
	// region Attribute extractor functions
	// These functions will extract the view attributes
	
	private fun extractMaxThumbDrawable(a: TypedArray, defaultValue: Drawable): Drawable {
		return a.getDrawable(R.styleable.RangeSeekBar_rsb_maxThumbDrawable) ?: defaultValue
	}
	
	private fun extractMinThumbDrawable(a: TypedArray, defaultValue: Drawable): Drawable {
		return a.getDrawable(R.styleable.RangeSeekBar_rsb_minThumbDrawable) ?: defaultValue
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
	
	private fun extractTrackRoundedCaps(a: TypedArray): Boolean {
		return a.getBoolean(R.styleable.RangeSeekBar_rsb_trackRoundedCaps, false)
	}
	
	private fun extractTrackSelectedRoundedCaps(a: TypedArray): Boolean {
		return a.getBoolean(R.styleable.RangeSeekBar_rsb_trackSelectedRoundedCaps, false)
	}
	
	private fun extractMinRange(a: TypedArray): Int {
		return a.getInteger(R.styleable.RangeSeekBar_rsb_minRange, 1)
	}
	
	private fun extractMaxValue(a: TypedArray): Int {
		return a.getInteger(R.styleable.RangeSeekBar_rsb_max, 100)
	}
	
	private fun extractMinThumbOffset(a: TypedArray): Point {
		val x = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_minThumbOffsetHorizontal, 0)
		val y = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_minThumbOffsetVertical, 0)
		return Point(x, y)
	}
	
	private fun extractMaxThumbOffset(a: TypedArray): Point {
		val x = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_maxThumbOffsetHorizontal, 0)
		val y = a.getDimensionPixelSize(R.styleable.RangeSeekBar_rsb_maxThumbOffsetVertical, 0)
		return Point(x, y)
	}
	// endregion
	// endregion
	
	companion object {
		private const val THUMB_NONE = 0
		private const val THUMB_MIN = 1
		private const val THUMB_MAX = 2
	}
	
	/**
	 * This interface is used to set callbacks for actions in [RangeSeekBar]
	 */
	interface SeekBarChangeListener {
		/**
		 * Gets called when the user has started dragging min or max thumbs
		 */
		fun onStartedSeeking()
		
		/**
		 * Gets called when the user has stopped dragging min or max thumb
		 */
		fun onStoppedSeeking()
		
		/**
		 * Gets called during the dragging of min or max value
		 *
		 * @param minThumbValue the current minimum value of selected range
		 * @param maxThumbValue the current maximum value of selected range
		 */
		fun onValueChanged(minThumbValue: Int, maxThumbValue: Int)
	}
}
package com.innovattic.rangeseekbar.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.innovattic.rangeseekbar.RangeSeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RangeSeekBar.SeekBarChangeListener {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRangeSeekBar()
    }

    override fun onStartedSeeking() {
        Log.i(TAG, "Started seeking.")
    }

    override fun onStoppedSeeking() {
        Log.i(TAG, "Stopped seeking.")
    }

    override fun onValueChanged(minThumbValue: Int, maxThumbValue: Int) {
        Log.i(TAG, "Selected range is from $minThumbValue to $maxThumbValue")
        tvRangeStart.text = minThumbValue.toString()
        tvRangeEnd.text = maxThumbValue.toString()
    }

    private fun setupRangeSeekBar() {
        val minValue = 0
        val maxValue = 500

        rangeSeekBar.max = maxValue // Max value for seekbar
        rangeSeekBar.minRange = 10 // Minimum space Range between maximum and minimum values.
        rangeSeekBar.seekBarChangeListener = this

        tvRangeStart.text = minValue.toString()
        tvRangeEnd.text = maxValue.toString()
    }
}
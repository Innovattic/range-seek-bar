package com.innovattic.rangeseekbar.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
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
    }

    private fun setupRangeSeekBar() {
        rangeSeekBar.max = 20
        rangeSeekBar.seekBarChangeListener = this
    }
}

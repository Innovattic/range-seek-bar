package com.innovattic.rangeseekbar

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.support.graphics.drawable.VectorDrawableCompat

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun getBitmapFromVectorDrawable(vectorDrawable: VectorDrawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return bitmap
}

fun getBitmapFromVectorDrawable(vectorDrawable: VectorDrawableCompat): Bitmap {
    val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return bitmap
}

fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
    return if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else if (drawable is VectorDrawableCompat) {
        getBitmapFromVectorDrawable(drawable)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && drawable is VectorDrawable) {
        getBitmapFromVectorDrawable(drawable)
    } else {
        throw IllegalArgumentException("Unsupported drawable type")
    }
}
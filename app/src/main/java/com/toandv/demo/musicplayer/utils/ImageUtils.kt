package com.toandv.demo.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import kotlin.math.roundToInt

object ImageUtils {

    fun bitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                context.contentResolver, uri
            )
            Build.VERSION.SDK_INT >= 28 -> {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, false)
            }
            else -> null
        }
    }

    private fun paletteFromUri(context: Context, uri: Uri): Palette? {
        val bitmap = bitmapFromUri(context, uri)
        return if (bitmap != null && !bitmap.isRecycled) {
            Palette.from(bitmap).generate()
        } else null
    }

    fun getMostPopulousSwatch(context: Context, uri: Uri): Palette.Swatch? {
        val palette = paletteFromUri(context, uri)
        var mostPopulous: Palette.Swatch? = null
        if (palette != null) {
            for (swatch in palette.swatches) {
                if (mostPopulous == null || swatch.population > mostPopulous.population) {
                    mostPopulous = swatch
                }
            }
        }
        return mostPopulous
    }

    @ColorInt

    fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
        return Color.argb(
            (Color.alpha(color) * factor).roundToInt(),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}

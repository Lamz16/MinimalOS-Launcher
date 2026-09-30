package com.lamz.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object IconCache {
    // Cache up to 150 app icons in memory
    private val memoryCache = LruCache<String, ImageBitmap>(150)

    suspend fun getAppIcon(context: Context, packageName: String): ImageBitmap? {
        memoryCache.get(packageName)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val drawable = pm.getApplicationIcon(appInfo)
                val bitmap = drawableToBitmap(drawable)
                val imageBitmap = bitmap.asImageBitmap()
                memoryCache.put(packageName, imageBitmap)
                imageBitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    fun clearCache() {
        memoryCache.evictAll()
    }

    fun remove(packageName: String) {
        memoryCache.remove(packageName)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            // Ensure bitmap is software config if needed
            if (bmp.config != Bitmap.Config.HARDWARE) {
                return bmp
            }
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

        // Normalize max size to 96x96 to save RAM and keep rendering instant
        val targetSize = 96
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

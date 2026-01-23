package org.vinaygopinath.launchchat.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class ImageStorageHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val ICONS_DIRECTORY = "chat_app_icons"
        private const val MAX_ICON_SIZE = 192
        private const val ICON_COMPRESSION_QUALITY = 100
    }

    fun saveIcon(sourceUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val resizedBitmap = resizeBitmap(originalBitmap, MAX_ICON_SIZE)
            val iconFile = createIconFile()

            FileOutputStream(iconFile).use { outputStream ->
                resizedBitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    ICON_COMPRESSION_QUALITY,
                    outputStream
                )
            }

            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()

            iconFile.absolutePath
        } catch (ignoredException: Exception) {
            null
        }
    }

    fun deleteIcon(iconPath: String) {
        try {
            val file = File(iconPath)
            if (file.exists()) {
                file.delete()
            }
        } catch (ignoredException: Exception) {
            // Ignore deletion errors
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return bitmap.scale(newWidth, newHeight)
    }

    private fun createIconFile(): File {
        val iconsDir = File(context.filesDir, ICONS_DIRECTORY)
        if (!iconsDir.exists()) {
            iconsDir.mkdirs()
        }
        val fileName = "${UUID.randomUUID()}.png"
        return File(iconsDir, fileName)
    }
}

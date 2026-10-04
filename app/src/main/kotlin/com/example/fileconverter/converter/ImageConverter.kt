package com.example.fileconverter.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.fileconverter.util.MediaStoreHelper

object ImageConverter {

    fun convert(
        context: Context,
        inputUri: Uri,
        targetFormat: Bitmap.CompressFormat,
        quality: Int,
        resizeWidth: Int?,
        resizeHeight: Int?,
        outputFileName: String
    ): Uri? {
        val bitmap = context.contentResolver.openInputStream(inputUri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return null

        try {
            val toSave = if (resizeWidth != null && resizeHeight != null &&
                resizeWidth > 0 && resizeHeight > 0) {
                Bitmap.createScaledBitmap(bitmap, resizeWidth, resizeHeight, true)
                    .also { if (it !== bitmap) bitmap.recycle() }
            } else {
                bitmap
            }

            val mimeType = when (targetFormat) {
                Bitmap.CompressFormat.JPEG -> "image/jpeg"
                Bitmap.CompressFormat.PNG -> "image/png"
                else -> "image/webp"
            }

            val pair = MediaStoreHelper.openOutputStream(context, outputFileName, mimeType)
                ?: run { toSave.recycle(); return null }
            val (uri, stream) = pair
            stream.use { toSave.compress(targetFormat, quality, it) }
            if (toSave !== bitmap) toSave.recycle() else bitmap.recycle()
            MediaStoreHelper.finishPending(context, uri)
            return uri
        } catch (e: Exception) {
            bitmap.recycle()
            return null
        }
    }
}

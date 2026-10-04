package com.example.fileconverter.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.OutputStream

object MediaStoreHelper {

    private const val SUBDIR = "FileBodlai"

    /**
     * Opens an OutputStream to write a file into Downloads/FileBodlai/.
     * Returns Pair(uri, outputStream). Caller must close the stream.
     */
    fun openOutputStream(
        context: Context,
        fileName: String,
        mimeType: String
    ): Pair<Uri, OutputStream>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$SUBDIR")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return null
            val stream = resolver.openOutputStream(uri) ?: return null
            Pair(uri, stream)
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDir = File(downloadsDir, SUBDIR).also { it.mkdirs() }
            val file = File(subDir, fileName)
            val stream = file.outputStream()
            val uri = Uri.fromFile(file)
            Pair(uri, stream)
        }
    }

    /** Must be called after writing is done (API 29+) to make file visible. */
    fun finishPending(context: Context, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    /** Opens an output stream for writing an image into Downloads/FileConverter/. */
    fun openImageOutputStream(
        context: Context,
        fileName: String,
        mimeType: String
    ): Pair<Uri, OutputStream>? = openOutputStream(context, fileName, mimeType)
}

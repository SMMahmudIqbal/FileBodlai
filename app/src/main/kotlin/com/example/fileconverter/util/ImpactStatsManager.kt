package com.example.fileconverter.util

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import java.util.Locale

/**
 * Bodlai Impact Telemetry Engine
 * Tracks tangible device storage saved & total files processed 100% on-device.
 */
object ImpactStatsManager {
    private const val PREFS_NAME = "bodlai_impact_stats"
    private const val KEY_BYTES_SAVED = "total_bytes_saved"
    private const val KEY_FILES_PROCESSED = "total_files_processed"

    private val _bytesSavedState = mutableLongStateOf(0L)
    val bytesSavedState: State<Long> = _bytesSavedState

    private val _filesProcessedState = mutableIntStateOf(0)
    val filesProcessedState: State<Int> = _filesProcessedState

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _bytesSavedState.longValue = prefs.getLong(KEY_BYTES_SAVED, 0L)
        _filesProcessedState.intValue = prefs.getInt(KEY_FILES_PROCESSED, 0)
    }

    fun recordOperation(context: Context, bytesOriginal: Long = 0L, bytesResult: Long = 0L) {
        val diff = if (bytesOriginal > bytesResult && bytesResult > 0L) {
            bytesOriginal - bytesResult
        } else {
            0L
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentBytes = prefs.getLong(KEY_BYTES_SAVED, 0L) + diff
        val currentFiles = prefs.getInt(KEY_FILES_PROCESSED, 0) + 1

        prefs.edit()
            .putLong(KEY_BYTES_SAVED, currentBytes)
            .putInt(KEY_FILES_PROCESSED, currentFiles)
            .apply()

        _bytesSavedState.longValue = currentBytes
        _filesProcessedState.intValue = currentFiles
    }

    fun getFormattedStorageSaved(): String {
        return formatBytes(_bytesSavedState.longValue)
    }

    fun getFilesProcessedCount(): Int {
        return _filesProcessedState.intValue
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0.0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            else -> String.format(Locale.US, "%.0f KB", kb)
        }
    }
}

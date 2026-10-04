package com.example.fileconverter.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.util.HistoryManager
import com.example.fileconverter.util.ImpactStatsManager
import com.example.fileconverter.util.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class BatchCompressViewModel(app: Application) : AndroidViewModel(app) {

    data class ImageItem(val uri: Uri, val name: String, val size: String, val rawBytes: Long)

    enum class ResizeMode {
        ORIGINAL, PERCENT_75, PERCENT_50, PERCENT_25
    }

    data class UiState(
        val images: List<ImageItem> = emptyList(),
        val resizeMode: ResizeMode = ResizeMode.ORIGINAL,
        val quality: Int = 75,
        val targetMaxKb: Int? = null, // e.g. 200 for government portals
        val isProcessing: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultCount: Int = 0,
        val firstResultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun addImages(uris: List<Uri>) {
        val ctx = getApplication<Application>()
        val newItems = uris.map { uri ->
            val size = FileUtils.getFileSize(ctx, uri)
            ImageItem(
                uri = uri,
                name = FileUtils.getFileName(ctx, uri),
                size = FileUtils.formatSize(size),
                rawBytes = size
            )
        }
        _state.value = _state.value.copy(images = _state.value.images + newItems)
    }

    fun removeImage(index: Int) {
        val list = _state.value.images.toMutableList()
        if (index in list.indices) list.removeAt(index)
        _state.value = _state.value.copy(images = list)
    }

    fun setResizeMode(mode: ResizeMode) {
        _state.value = _state.value.copy(resizeMode = mode)
    }

    fun setQuality(q: Int) {
        _state.value = _state.value.copy(quality = q)
    }

    fun setTargetMaxKb(kb: Int?) {
        _state.value = _state.value.copy(targetMaxKb = kb)
    }

    fun processBatch() {
        val images = _state.value.images
        if (images.isEmpty()) {
            _state.value = _state.value.copy(error = "Select at least 1 image to compress")
            return
        }

        val ctx = getApplication<Application>()
        val resizeMode = _state.value.resizeMode
        val baseQuality = _state.value.quality
        val targetMaxKb = _state.value.targetMaxKb

        _state.value = _state.value.copy(isProcessing = true, error = null, resultCount = 0, firstResultUri = null, progress = Pair(0, images.size))

        compressJob = viewModelScope.launch(Dispatchers.IO) {
            var savedCount = 0
            var firstUri: Uri? = null
            var totalOriginalBytes = 0L
            var totalCompressedBytes = 0L

            for ((idx, item) in images.withIndex()) {
                _state.value = _state.value.copy(progress = Pair(idx + 1, images.size))

                try {
                    val stream = ctx.contentResolver.openInputStream(item.uri) ?: continue
                    val originalBitmap = BitmapFactory.decodeStream(stream)
                    stream.close()

                    if (originalBitmap == null) continue

                    totalOriginalBytes += item.rawBytes

                    // Calculate resized dimensions
                    val scaleFactor = when (resizeMode) {
                        ResizeMode.PERCENT_75 -> 0.75f
                        ResizeMode.PERCENT_50 -> 0.50f
                        ResizeMode.PERCENT_25 -> 0.25f
                        ResizeMode.ORIGINAL -> 1.0f
                    }

                    val targetBmp = if (scaleFactor < 1.0f) {
                        val targetW = (originalBitmap.width * scaleFactor).toInt().coerceAtLeast(1)
                        val targetH = (originalBitmap.height * scaleFactor).toInt().coerceAtLeast(1)
                        val scaled = Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
                        originalBitmap.recycle()
                        scaled
                    } else {
                        originalBitmap
                    }

                    // Compress to JPEG with target KB constraint or quality slider
                    var q = baseQuality
                    var bytes: ByteArray
                    if (targetMaxKb != null && targetMaxKb > 0) {
                        // Binary search / stepped reduction to achieve max target size
                        val maxBytes = targetMaxKb * 1024L
                        var currentQ = 90
                        val baos = ByteArrayOutputStream()
                        targetBmp.compress(Bitmap.CompressFormat.JPEG, currentQ, baos)
                        bytes = baos.toByteArray()
                        while (bytes.size > maxBytes && currentQ > 15) {
                            currentQ -= 15
                            baos.reset()
                            targetBmp.compress(Bitmap.CompressFormat.JPEG, currentQ, baos)
                            bytes = baos.toByteArray()
                        }
                    } else {
                        val baos = ByteArrayOutputStream()
                        targetBmp.compress(Bitmap.CompressFormat.JPEG, q, baos)
                        bytes = baos.toByteArray()
                    }
                    targetBmp.recycle()
                    totalCompressedBytes += bytes.size

                    val outName = "compressed_${System.currentTimeMillis()}_${idx + 1}.jpg"
                    val pair = MediaStoreHelper.openOutputStream(ctx, outName, "image/jpeg")
                    if (pair != null) {
                        val (uri, outStream) = pair
                        outStream.use { it.write(bytes) }
                        MediaStoreHelper.finishPending(ctx, uri)

                        if (firstUri == null) {
                            firstUri = uri
                            HistoryManager.addRecent(
                                context = ctx,
                                fileName = outName,
                                uri = uri,
                                mimeType = "image/jpeg",
                                typeLabel = "Batch Compressed Images"
                            )
                        }
                        savedCount++
                    }
                } catch (_: Exception) {}
            }

            if (savedCount > 0) {
                ImpactStatsManager.recordOperation(ctx, bytesOriginal = totalOriginalBytes, bytesResult = totalCompressedBytes)
            }

            _state.value = _state.value.copy(
                isProcessing = false,
                resultCount = savedCount,
                firstResultUri = firstUri
            )
        }
    }

    private var compressJob: Job? = null

    fun cancel() {
        compressJob?.cancel()
        _state.value = _state.value.copy(
            isProcessing = false,
            error = "Batch compression cancelled"
        )
    }

    fun clearResult() {
        _state.value = _state.value.copy(resultCount = 0, firstResultUri = null, error = null)
    }
}

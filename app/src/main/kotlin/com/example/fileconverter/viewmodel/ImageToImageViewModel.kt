package com.example.fileconverter.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.converter.ImageConverter
import com.example.fileconverter.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageToImageViewModel(app: Application) : AndroidViewModel(app) {

    data class UiState(
        val imageUri: Uri? = null,
        val imageName: String = "",
        val imageSize: String = "",
        val targetFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        val quality: Int = 85,
        val resizeWidth: String = "",
        val resizeHeight: String = "",
        val isConverting: Boolean = false,
        val resultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun pickImage(uri: Uri) {
        val ctx = getApplication<Application>()
        _state.value = _state.value.copy(
            imageUri = uri,
            imageName = FileUtils.getFileName(ctx, uri),
            imageSize = FileUtils.formatSize(FileUtils.getFileSize(ctx, uri)),
            resultUri = null, error = null
        )
    }

    fun setFormat(f: Bitmap.CompressFormat) { _state.value = _state.value.copy(targetFormat = f) }
    fun setQuality(q: Int) { _state.value = _state.value.copy(quality = q) }
    fun setResizeWidth(w: String) { _state.value = _state.value.copy(resizeWidth = w) }
    fun setResizeHeight(h: String) { _state.value = _state.value.copy(resizeHeight = h) }

    fun convert() {
        val uri = _state.value.imageUri ?: return
        val ctx = getApplication<Application>()
        val format = _state.value.targetFormat
        val ext = when (format) {
            Bitmap.CompressFormat.JPEG -> "jpg"
            Bitmap.CompressFormat.PNG -> "png"
            else -> "webp"
        }
        val baseName = FileUtils.baseName(FileUtils.getFileName(ctx, uri))
        val outName = "${baseName}_converted.$ext"
        val resW = _state.value.resizeWidth.toIntOrNull()
        val resH = _state.value.resizeHeight.toIntOrNull()

        _state.value = _state.value.copy(isConverting = true, error = null, resultUri = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = ImageConverter.convert(
                    context = ctx,
                    inputUri = uri,
                    targetFormat = format,
                    quality = _state.value.quality,
                    resizeWidth = resW,
                    resizeHeight = resH,
                    outputFileName = outName
                )
                if (result != null) {
                    val mime = when (format) {
                        Bitmap.CompressFormat.PNG -> "image/png"
                        Bitmap.CompressFormat.WEBP -> "image/webp"
                        else -> "image/jpeg"
                    }
                    com.example.fileconverter.util.HistoryManager.addRecent(
                        context = ctx,
                        fileName = outName,
                        uri = result,
                        mimeType = mime,
                        typeLabel = "Image Convert"
                    )
                    com.example.fileconverter.util.ImpactStatsManager.recordOperation(ctx)
                    _state.value = _state.value.copy(isConverting = false, resultUri = result)
                } else {
                    _state.value = _state.value.copy(isConverting = false, error = "Conversion failed. Is the image valid?")
                }
            } catch (e: OutOfMemoryError) {
                _state.value = _state.value.copy(isConverting = false, error = "Out of memory. Try a smaller image.")
            } catch (e: Exception) {
                _state.value = _state.value.copy(isConverting = false, error = "Error: ${e.localizedMessage}")
            }
        }
    }

    fun clearResult() { _state.value = _state.value.copy(resultUri = null, error = null) }
}

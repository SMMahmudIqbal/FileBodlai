package com.example.fileconverter.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.converter.PdfConverter
import com.example.fileconverter.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PdfToImagesViewModel(app: Application) : AndroidViewModel(app) {

    data class UiState(
        val pdfUri: Uri? = null,
        val pdfName: String = "",
        val pageCount: Int = 0,
        val pageRangeStart: Int = 1,
        val pageRangeEnd: Int = 1,
        val dpi: Int = 150,
        val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        val isConverting: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultCount: Int = 0,
        val firstResultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun pickPdf(uri: Uri) {
        val ctx = getApplication<Application>()
        val name = FileUtils.getFileName(ctx, uri)
        var count = 0
        try {
            ctx.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer -> count = renderer.pageCount }
            }
        } catch (_: Exception) {}
        _state.value = _state.value.copy(
            pdfUri = uri, pdfName = name,
            pageCount = count,
            pageRangeStart = 1, pageRangeEnd = count,
            resultCount = 0, firstResultUri = null, error = null
        )
    }

    fun setDpi(dpi: Int) { _state.value = _state.value.copy(dpi = dpi) }
    fun setFormat(f: Bitmap.CompressFormat) { _state.value = _state.value.copy(format = f) }
    fun setRangeStart(v: Int) { _state.value = _state.value.copy(pageRangeStart = v) }
    fun setRangeEnd(v: Int) { _state.value = _state.value.copy(pageRangeEnd = v) }

    fun convert() {
        val uri = _state.value.pdfUri ?: return
        val ctx = getApplication<Application>()
        val baseName = FileUtils.baseName(FileUtils.getFileName(ctx, uri))
        val rangeStart = (_state.value.pageRangeStart - 1).coerceAtLeast(0)
        val rangeEnd = (_state.value.pageRangeEnd - 1).coerceAtLeast(rangeStart)

        _state.value = _state.value.copy(isConverting = true, error = null, resultCount = 0, firstResultUri = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = PdfConverter.pdfToImages(
                    context = ctx,
                    pdfUri = uri,
                    dpi = _state.value.dpi,
                    format = _state.value.format,
                    pageRange = rangeStart..rangeEnd,
                    baseName = baseName,
                    onProgress = { cur, total ->
                        _state.value = _state.value.copy(progress = Pair(cur, total))
                    }
                )
                if (results.isNotEmpty()) {
                    com.example.fileconverter.util.ImpactStatsManager.recordOperation(ctx)
                }
                _state.value = _state.value.copy(
                    isConverting = false,
                    resultCount = results.size,
                    firstResultUri = results.firstOrNull()
                )
            } catch (e: OutOfMemoryError) {
                _state.value = _state.value.copy(isConverting = false, error = "Out of memory. Try a lower DPI.")
            } catch (e: Exception) {
                _state.value = _state.value.copy(isConverting = false, error = "Error: ${e.localizedMessage}")
            }
        }
    }

    fun clearResult() { _state.value = _state.value.copy(resultCount = 0, firstResultUri = null, error = null) }
}

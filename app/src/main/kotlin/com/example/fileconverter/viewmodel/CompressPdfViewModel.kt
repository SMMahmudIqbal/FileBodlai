package com.example.fileconverter.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.converter.PdfConverter
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.util.HistoryManager
import com.example.fileconverter.util.ImpactStatsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CompressPdfViewModel(app: Application) : AndroidViewModel(app) {

    enum class CompressionLevel(val label: String, val dpi: Int, val quality: Int, val estReduction: String) {
        LOW("Extreme (< 96 DPI)", 96, 50, "~70% smaller"),
        MEDIUM("Recommended (150 DPI)", 150, 70, "~45% smaller"),
        HIGH("High Quality (200 DPI)", 200, 85, "~25% smaller")
    }

    data class UiState(
        val selectedUri: Uri? = null,
        val fileName: String = "",
        val fileSize: String = "",
        val rawBytes: Long = 0,
        val pageCount: Int = 0,
        val customName: String = "",
        val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
        val isProcessing: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultUri: Uri? = null,
        val resultBytes: Long = 0,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun setPdf(uri: Uri) {
        val ctx = getApplication<Application>()
        val name = FileUtils.getFileName(ctx, uri)
        val size = FileUtils.getFileSize(ctx, uri)
        val baseName = name.substringBeforeLast(".")

        var pages = 0
        try {
            val pfd = ctx.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val renderer = android.graphics.pdf.PdfRenderer(pfd)
                pages = renderer.pageCount
                renderer.close()
                pfd.close()
            }
        } catch (_: Exception) {}

        _state.value = UiState(
            selectedUri = uri,
            fileName = name,
            fileSize = FileUtils.formatSize(size),
            rawBytes = size,
            pageCount = pages,
            customName = "${baseName}_compressed"
        )
    }

    fun setCompressionLevel(level: CompressionLevel) {
        _state.value = _state.value.copy(compressionLevel = level)
    }

    fun setCustomName(name: String) {
        _state.value = _state.value.copy(customName = name)
    }

    private var compressJob: Job? = null

    fun clearResult() {
        _state.value = _state.value.copy(resultUri = null)
    }

    fun cancel() {
        compressJob?.cancel()
        _state.value = _state.value.copy(
            isProcessing = false,
            error = "Compression cancelled"
        )
    }

    fun compressPdf() {
        val uri = _state.value.selectedUri ?: return
        val ctx = getApplication<Application>()
        val level = _state.value.compressionLevel
        val outName = if (_state.value.customName.isNotBlank()) {
            if (_state.value.customName.endsWith(".pdf", ignoreCase = true)) _state.value.customName
            else "${_state.value.customName}.pdf"
        } else "compressed_${System.currentTimeMillis()}.pdf"

        _state.value = _state.value.copy(isProcessing = true, error = null, resultUri = null)

        compressJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val resultUri = PdfConverter.compressPdf(
                    context = ctx,
                    inputUri = uri,
                    targetDpi = level.dpi,
                    jpegQuality = level.quality,
                    outputFileName = outName,
                    onProgress = { cur, total ->
                        _state.value = _state.value.copy(progress = Pair(cur, total))
                    }
                )

                if (resultUri != null) {
                    val resultSize = FileUtils.getFileSize(ctx, resultUri)
                    HistoryManager.addRecent(ctx, outName, resultUri, "application/pdf", "PDF Compress")
                    ImpactStatsManager.recordOperation(ctx, bytesOriginal = _state.value.rawBytes, bytesResult = resultSize)
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        resultUri = resultUri,
                        resultBytes = resultSize
                    )
                } else {
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        error = "Failed to compress PDF. Please check file permissions."
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    error = "Compression cancelled"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    error = e.localizedMessage ?: "Error during PDF compression"
                )
            }
        }
    }
}

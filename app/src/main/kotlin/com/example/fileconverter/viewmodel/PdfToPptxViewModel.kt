package com.example.fileconverter.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.converter.PptxBuilder
import com.example.fileconverter.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PdfToPptxViewModel(app: Application) : AndroidViewModel(app) {

    data class UiState(
        val pdfUri: Uri? = null,
        val pdfName: String = "",
        val pdfSize: String = "",
        val isConverting: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun pickPdf(uri: Uri) {
        val ctx = getApplication<Application>()
        _state.value = _state.value.copy(
            pdfUri = uri,
            pdfName = FileUtils.getFileName(ctx, uri),
            pdfSize = FileUtils.formatSize(FileUtils.getFileSize(ctx, uri)),
            resultUri = null,
            error = null
        )
    }

    private var convertJob: kotlinx.coroutines.Job? = null

    fun cancel() {
        convertJob?.cancel()
        _state.value = _state.value.copy(
            isConverting = false,
            error = "Conversion cancelled"
        )
    }

    fun convert() {
        val uri = _state.value.pdfUri ?: return
        val ctx = getApplication<Application>()
        val baseName = FileUtils.baseName(FileUtils.getFileName(ctx, uri))
        val outName = "${baseName}.pptx"

        _state.value = _state.value.copy(isConverting = true, error = null, resultUri = null)
        convertJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PptxBuilder.build(
                    context = ctx,
                    pdfUri = uri,
                    outputFileName = outName,
                    dpi = 150,
                    onProgress = { cur, total ->
                        _state.value = _state.value.copy(progress = Pair(cur, total))
                    }
                )
                if (result != null) {
                    com.example.fileconverter.util.HistoryManager.addRecent(
                        context = ctx,
                        fileName = outName,
                        uri = result,
                        mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        typeLabel = "PDF → PPTX"
                    )
                    com.example.fileconverter.util.ImpactStatsManager.recordOperation(ctx)
                    _state.value = _state.value.copy(isConverting = false, resultUri = result)
                } else {
                    _state.value = _state.value.copy(isConverting = false, error = "Conversion failed. Is the PDF valid?")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                _state.value = _state.value.copy(
                    isConverting = false,
                    error = "Conversion cancelled"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isConverting = false,
                    error = "Error: ${e.localizedMessage ?: e.javaClass.simpleName}"
                )
            }
        }
    }

    fun clearResult() { _state.value = _state.value.copy(resultUri = null, error = null) }
}

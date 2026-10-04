package com.example.fileconverter.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.converter.DocxToPdfConverter
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.util.HistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DocxToPdfViewModel(app: Application) : AndroidViewModel(app) {

    data class UiState(
        val docxUri: Uri? = null,
        val docxName: String = "",
        val docxSize: String = "",
        val isConverting: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun pickDocx(uri: Uri) {
        val ctx = getApplication<Application>()
        _state.value = _state.value.copy(
            docxUri = uri,
            docxName = FileUtils.getFileName(ctx, uri),
            docxSize = FileUtils.formatSize(FileUtils.getFileSize(ctx, uri)),
            resultUri = null,
            error = null
        )
    }

    fun convert() {
        val uri = _state.value.docxUri ?: return
        val ctx = getApplication<Application>()
        val baseName = FileUtils.baseName(FileUtils.getFileName(ctx, uri))
        val outName = "${baseName}.pdf"

        _state.value = _state.value.copy(isConverting = true, error = null, resultUri = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = DocxToPdfConverter.convert(
                    context = ctx,
                    docxUri = uri,
                    outputFileName = outName,
                    onProgress = { cur, total ->
                        _state.value = _state.value.copy(progress = Pair(cur, total))
                    }
                )
                if (result != null) {
                    HistoryManager.addRecent(
                        context = ctx,
                        fileName = outName,
                        uri = result,
                        mimeType = "application/pdf",
                        typeLabel = "Word → PDF"
                    )
                    com.example.fileconverter.util.ImpactStatsManager.recordOperation(ctx)
                    _state.value = _state.value.copy(isConverting = false, resultUri = result)
                } else {
                    _state.value = _state.value.copy(
                        isConverting = false,
                        error = "Conversion failed. Please verify that this is a valid Word (.docx) document."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isConverting = false,
                    error = "Error: ${e.localizedMessage ?: e.javaClass.simpleName}"
                )
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(resultUri = null, error = null)
    }
}

package com.example.fileconverter.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.converter.PageSizeMode
import com.example.fileconverter.converter.PdfConverter
import com.example.fileconverter.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageToPdfViewModel(app: Application) : AndroidViewModel(app) {

    data class ImageItem(val uri: Uri, val name: String, val size: String)

    data class UiState(
        val images: List<ImageItem> = emptyList(),
        val pageSizeMode: PageSizeMode = PageSizeMode.A4,
        val marginPts: Int = 20,
        val landscape: Boolean = false,
        val isConverting: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun addImages(uris: List<Uri>) {
        val ctx = getApplication<Application>()
        val newItems = uris.map { uri ->
            ImageItem(uri, FileUtils.getFileName(ctx, uri), FileUtils.formatSize(FileUtils.getFileSize(ctx, uri)))
        }
        _state.value = _state.value.copy(images = _state.value.images + newItems)
    }

    fun removeImage(index: Int) {
        val list = _state.value.images.toMutableList()
        if (index in list.indices) list.removeAt(index)
        _state.value = _state.value.copy(images = list)
    }

    fun moveUp(index: Int) {
        if (index <= 0) return
        val list = _state.value.images.toMutableList()
        val tmp = list[index - 1]; list[index - 1] = list[index]; list[index] = tmp
        _state.value = _state.value.copy(images = list)
    }

    fun moveDown(index: Int) {
        val list = _state.value.images.toMutableList()
        if (index >= list.size - 1) return
        val tmp = list[index + 1]; list[index + 1] = list[index]; list[index] = tmp
        _state.value = _state.value.copy(images = list)
    }

    fun setPageSizeMode(mode: PageSizeMode) { _state.value = _state.value.copy(pageSizeMode = mode) }
    fun setMargin(pts: Int) { _state.value = _state.value.copy(marginPts = pts) }
    fun setLandscape(v: Boolean) { _state.value = _state.value.copy(landscape = v) }

    fun convert() {
        val images = _state.value.images
        if (images.isEmpty()) {
            _state.value = _state.value.copy(error = "Please add at least one image.")
            return
        }
        val ctx = getApplication<Application>()
        val outName = "images_${System.currentTimeMillis()}.pdf"
        _state.value = _state.value.copy(isConverting = true, error = null, resultUri = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PdfConverter.imagesToPdf(
                    context = ctx,
                    imageUris = images.map { it.uri },
                    pageSizeMode = _state.value.pageSizeMode,
                    marginPts = _state.value.marginPts,
                    landscape = _state.value.landscape,
                    outputFileName = outName,
                    onProgress = { cur, total ->
                        _state.value = _state.value.copy(progress = Pair(cur, total))
                    }
                )
                if (result != null) {
                    com.example.fileconverter.util.HistoryManager.addRecent(
                        context = ctx,
                        fileName = outName,
                        uri = result,
                        mimeType = "application/pdf",
                        typeLabel = "Images → PDF"
                    )
                    com.example.fileconverter.util.ImpactStatsManager.recordOperation(ctx)
                    _state.value = _state.value.copy(isConverting = false, resultUri = result)
                } else {
                    _state.value = _state.value.copy(isConverting = false, error = "Conversion failed.")
                }
            } catch (e: OutOfMemoryError) {
                _state.value = _state.value.copy(isConverting = false, error = "Out of memory. Try fewer or smaller images.")
            } catch (e: Exception) {
                _state.value = _state.value.copy(isConverting = false, error = "Error: ${e.localizedMessage}")
            }
        }
    }

    fun clearResult() { _state.value = _state.value.copy(resultUri = null, error = null) }
}

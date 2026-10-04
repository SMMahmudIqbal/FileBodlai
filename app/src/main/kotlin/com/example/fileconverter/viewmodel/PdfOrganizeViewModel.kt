package com.example.fileconverter.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.util.HistoryManager
import com.example.fileconverter.util.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PdfOrganizeViewModel(app: Application) : AndroidViewModel(app) {

    data class PageItem(
        val originalIndex: Int,
        val pageNumberDisplay: Int,
        val thumbnail: Bitmap? = null
    )

    data class UiState(
        val pdfUri: Uri? = null,
        val pdfName: String = "",
        val totalPages: Int = 0,
        val pages: List<PageItem> = emptyList(),
        val isLoadingPages: Boolean = false,
        val isProcessing: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun loadPdf(uri: Uri) {
        val ctx = getApplication<Application>()
        val name = FileUtils.getFileName(ctx, uri)
        _state.value = UiState(
            pdfUri = uri,
            pdfName = name,
            isLoadingPages = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pfd = ctx.contentResolver.openFileDescriptor(uri, "r")
                if (pfd == null) {
                    _state.value = _state.value.copy(
                        isLoadingPages = false,
                        error = "Unable to read selected PDF"
                    )
                    return@launch
                }

                val renderer = PdfRenderer(pfd)
                val count = renderer.pageCount
                val pageList = mutableListOf<PageItem>()

                for (i in 0 until count) {
                    val page = renderer.openPage(i)
                    // Generate small thumbnail for preview
                    val scale = (120f / page.width.toFloat()).coerceAtMost(160f / page.height.toFloat())
                    val thumbW = (page.width * scale).toInt().coerceAtLeast(1)
                    val thumbH = (page.height * scale).toInt().coerceAtLeast(1)
                    val bmp = Bitmap.createBitmap(thumbW, thumbH, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    pageList.add(
                        PageItem(
                            originalIndex = i,
                            pageNumberDisplay = i + 1,
                            thumbnail = bmp
                        )
                    )
                }

                renderer.close()
                pfd.close()

                _state.value = _state.value.copy(
                    isLoadingPages = false,
                    totalPages = count,
                    pages = pageList
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingPages = false,
                    error = "Failed to load PDF pages: ${e.localizedMessage}"
                )
            }
        }
    }

    fun movePageUp(index: Int) {
        if (index <= 0) return
        val current = _state.value.pages.toMutableList()
        val item = current.removeAt(index)
        current.add(index - 1, item)
        _state.value = _state.value.copy(pages = current)
    }

    fun movePageDown(index: Int) {
        if (index >= _state.value.pages.size - 1) return
        val current = _state.value.pages.toMutableList()
        val item = current.removeAt(index)
        current.add(index + 1, item)
        _state.value = _state.value.copy(pages = current)
    }

    fun deletePage(index: Int) {
        if (index !in _state.value.pages.indices) return
        val current = _state.value.pages.toMutableList()
        current.removeAt(index)
        _state.value = _state.value.copy(pages = current)
    }

    fun saveReorganizedPdf() {
        val uri = _state.value.pdfUri ?: return
        val pagesToKeep = _state.value.pages
        if (pagesToKeep.isEmpty()) {
            _state.value = _state.value.copy(error = "Must keep at least 1 page")
            return
        }

        val ctx = getApplication<Application>()
        _state.value = _state.value.copy(isProcessing = true, error = null, resultUri = null, progress = Pair(0, pagesToKeep.size))

        viewModelScope.launch(Dispatchers.IO) {
            val outDoc = PdfDocument()
            try {
                val pfd = ctx.contentResolver.openFileDescriptor(uri, "r")
                if (pfd == null) {
                    _state.value = _state.value.copy(isProcessing = false, error = "Cannot read PDF")
                    return@launch
                }

                val renderer = PdfRenderer(pfd)
                for ((idx, pageItem) in pagesToKeep.withIndex()) {
                    _state.value = _state.value.copy(progress = Pair(idx + 1, pagesToKeep.size))
                    val srcPage = renderer.openPage(pageItem.originalIndex)
                    val scale = 2f
                    val w = (srcPage.width * scale).toInt().coerceAtLeast(1)
                    val h = (srcPage.height * scale).toInt().coerceAtLeast(1)
                    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    srcPage.close()

                    val pageInfo = PdfDocument.PageInfo.Builder(srcPage.width, srcPage.height, idx + 1).create()
                    val outPage = outDoc.startPage(pageInfo)
                    val matrix = android.graphics.Matrix()
                    matrix.setScale(1f / scale, 1f / scale)
                    outPage.canvas.drawBitmap(bmp, matrix, null)
                    bmp.recycle()
                    outDoc.finishPage(outPage)
                }

                renderer.close()
                pfd.close()

                val outName = "reordered_${System.currentTimeMillis()}.pdf"
                val pair = MediaStoreHelper.openOutputStream(ctx, outName, "application/pdf")
                    ?: run {
                        outDoc.close()
                        _state.value = _state.value.copy(isProcessing = false, error = "Failed to create output file")
                        return@launch
                    }

                val (resUri, stream) = pair
                stream.use { outDoc.writeTo(it) }
                MediaStoreHelper.finishPending(ctx, resUri)

                HistoryManager.addRecent(
                    context = ctx,
                    fileName = outName,
                    uri = resUri,
                    mimeType = "application/pdf",
                    typeLabel = "Reordered PDF"
                )

                _state.value = _state.value.copy(isProcessing = false, resultUri = resUri)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isProcessing = false, error = "Failed to save: ${e.localizedMessage}")
            } finally {
                try { outDoc.close() } catch (_: Exception) {}
            }
        }
    }

    fun clearResult() {
        _state.value = _state.value.copy(resultUri = null, error = null)
    }
}

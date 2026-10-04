package com.example.fileconverter.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.util.MediaStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Merges PDFs by rendering each page as a bitmap with PdfRenderer and
 * writing it into a new PdfDocument — no external library needed.
 * Note: this rasterizes text (quality depends on render DPI), but works
 * fully offline with built-in Android APIs. Text searchability is lost.
 */
class MergePdfViewModel(app: Application) : AndroidViewModel(app) {

    data class PdfItem(val uri: Uri, val name: String, val size: String)

    data class UiState(
        val pdfs: List<PdfItem> = emptyList(),
        val isConverting: Boolean = false,
        val progress: Pair<Int, Int> = Pair(0, 0),
        val resultUri: Uri? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun addPdfs(uris: List<Uri>) {
        val ctx = getApplication<Application>()
        val newItems = uris.map { uri ->
            PdfItem(
                uri = uri,
                name = FileUtils.getFileName(ctx, uri),
                size = FileUtils.formatSize(FileUtils.getFileSize(ctx, uri))
            )
        }
        _state.value = _state.value.copy(pdfs = _state.value.pdfs + newItems)
    }

    fun removePdf(index: Int) {
        val list = _state.value.pdfs.toMutableList()
        if (index in list.indices) list.removeAt(index)
        _state.value = _state.value.copy(pdfs = list)
    }

    fun moveUp(index: Int) {
        if (index <= 0) return
        val list = _state.value.pdfs.toMutableList()
        val tmp = list[index - 1]; list[index - 1] = list[index]; list[index] = tmp
        _state.value = _state.value.copy(pdfs = list)
    }

    fun moveDown(index: Int) {
        val list = _state.value.pdfs.toMutableList()
        if (index >= list.size - 1) return
        val tmp = list[index + 1]; list[index + 1] = list[index]; list[index] = tmp
        _state.value = _state.value.copy(pdfs = list)
    }

    private var mergeJob: kotlinx.coroutines.Job? = null

    fun cancel() {
        mergeJob?.cancel()
        _state.value = _state.value.copy(
            isConverting = false,
            error = "Merge cancelled"
        )
    }

    fun merge() {
        val pdfs = _state.value.pdfs
        if (pdfs.size < 2) {
            _state.value = _state.value.copy(error = "Please add at least 2 PDFs to merge.")
            return
        }
        val ctx = getApplication<Application>()
        _state.value = _state.value.copy(isConverting = true, error = null, resultUri = null, progress = Pair(0, 0))

        mergeJob = viewModelScope.launch(Dispatchers.IO) {
            val outDoc = PdfDocument()
            var pageCounter = 0
            var totalPages = 0
            try {
                // Count total pages first
                for (item in pdfs) {
                    try {
                        ctx.contentResolver.openFileDescriptor(item.uri, "r")?.use { pfd ->
                            PdfRenderer(pfd).use { r -> totalPages += r.pageCount }
                        }
                    } catch (_: Exception) {}
                }

                // Render each PDF page into the merged document
                for (item in pdfs) {
                    val pfd = ctx.contentResolver.openFileDescriptor(item.uri, "r")
                    if (pfd == null) {
                        _state.value = _state.value.copy(
                            isConverting = false,
                            error = "Cannot open: ${item.name}"
                        )
                        outDoc.close()
                        return@launch
                    }
                    try {
                        val renderer = PdfRenderer(pfd)
                        renderer.use { r ->
                            for (i in 0 until r.pageCount) {
                                pageCounter++
                                _state.value = _state.value.copy(progress = Pair(pageCounter, totalPages))

                                val srcPage = r.openPage(i)
                                val scale = 2f  // 144 DPI equivalent
                                val bmpW = (srcPage.width * scale).toInt().coerceAtLeast(1)
                                val bmpH = (srcPage.height * scale).toInt().coerceAtLeast(1)
                                val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
                                bmp.eraseColor(android.graphics.Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                                srcPage.close()

                                val pageInfo = PdfDocument.PageInfo.Builder(
                                    srcPage.width, srcPage.height, pageCounter
                                ).create()
                                val outPage = outDoc.startPage(pageInfo)
                                val matrix = android.graphics.Matrix()
                                matrix.setScale(1f / scale, 1f / scale)
                                outPage.canvas.drawBitmap(bmp, matrix, null)
                                bmp.recycle()
                                outDoc.finishPage(outPage)
                            }
                        }
                    } catch (e: Exception) {
                        val msg = when {
                            e.message?.contains("password", ignoreCase = true) == true ||
                            e.message?.contains("encrypt", ignoreCase = true) == true ->
                                "${item.name} is password-protected. Please unlock it first."
                            else -> "Error reading ${item.name}: ${e.localizedMessage}"
                        }
                        _state.value = _state.value.copy(isConverting = false, error = msg)
                        outDoc.close()
                        pfd.close()
                        return@launch
                    }
                    pfd.close()
                }

                val outName = "merged_${System.currentTimeMillis()}.pdf"
                val pair = MediaStoreHelper.openOutputStream(ctx, outName, "application/pdf")
                    ?: run {
                        outDoc.close()
                        _state.value = _state.value.copy(isConverting = false, error = "Cannot create output file.")
                        return@launch
                    }
                val (resultUri, outStream) = pair
                outStream.use { outDoc.writeTo(it) }
                MediaStoreHelper.finishPending(ctx, resultUri)
                com.example.fileconverter.util.HistoryManager.addRecent(
                    context = ctx,
                    fileName = outName,
                    uri = resultUri,
                    mimeType = "application/pdf",
                    typeLabel = "Merge PDFs"
                )
                com.example.fileconverter.util.ImpactStatsManager.recordOperation(ctx)
                _state.value = _state.value.copy(isConverting = false, resultUri = resultUri)
            } catch (e: kotlinx.coroutines.CancellationException) {
                _state.value = _state.value.copy(isConverting = false, error = "Merge cancelled")
            } catch (e: OutOfMemoryError) {
                _state.value = _state.value.copy(isConverting = false, error = "Out of memory. Try fewer pages or smaller PDFs.")
            } catch (e: Exception) {
                _state.value = _state.value.copy(isConverting = false, error = "Merge failed: ${e.localizedMessage}")
            } finally {
                try { outDoc.close() } catch (_: Exception) {}
            }
        }
    }

    fun clearResult() { _state.value = _state.value.copy(resultUri = null, error = null) }
}

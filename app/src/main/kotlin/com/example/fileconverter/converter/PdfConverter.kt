package com.example.fileconverter.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.example.fileconverter.util.MediaStoreHelper
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * PDF-related conversion utilities:
 *  - Images -> PDF
 *  - PDF -> Images (via PdfRenderer)
 *  - Text -> PDF
 */
object PdfConverter {

    // Standard page sizes in points (72 pts/inch)
    val A4_W = 595
    val A4_H = 842
    val LETTER_W = 612
    val LETTER_H = 792

    /** Render a single PDF page to Bitmap at the given DPI. Caller must recycle. */
    fun renderPage(renderer: PdfRenderer, pageIndex: Int, dpi: Int): Bitmap {
        val page = renderer.openPage(pageIndex)
        val scale = dpi / 72f
        val width = (page.width * scale).toInt().coerceAtLeast(1)
        val height = (page.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }

    /** Convert list of image URIs to a single PDF saved to Downloads/FileConverter/. */
    fun imagesToPdf(
        context: Context,
        imageUris: List<Uri>,
        pageSizeMode: PageSizeMode,
        marginPts: Int,
        landscape: Boolean,
        outputFileName: String,
        onProgress: (Int, Int) -> Unit
    ): Uri? {
        val doc = PdfDocument()
        try {
            val total = imageUris.size
            imageUris.forEachIndexed { idx, uri ->
                onProgress(idx + 1, total)
                val bitmap = decodeBitmap(context, uri) ?: return@forEachIndexed
                try {
                    val (pageW, pageH) = getPageSize(pageSizeMode, landscape, bitmap, marginPts)
                    val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, idx + 1).create()
                    val page = doc.startPage(pageInfo)
                    val canvas = page.canvas
                    val drawArea = android.graphics.Rect(
                        marginPts, marginPts,
                        pageW - marginPts, pageH - marginPts
                    )
                    val scaledBmp = scaledBitmapToFit(bitmap, drawArea.width(), drawArea.height())
                    // Center the image
                    val left = drawArea.left + (drawArea.width() - scaledBmp.width) / 2
                    val top = drawArea.top + (drawArea.height() - scaledBmp.height) / 2
                    canvas.drawBitmap(scaledBmp, left.toFloat(), top.toFloat(), null)
                    if (scaledBmp !== bitmap) scaledBmp.recycle()
                    doc.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            val pair = MediaStoreHelper.openOutputStream(context, outputFileName, "application/pdf")
                ?: return null
            val (uri, stream) = pair
            stream.use { doc.writeTo(it) }
            MediaStoreHelper.finishPending(context, uri)
            return uri
        } finally {
            doc.close()
        }
    }

    /** Convert PDF pages to images, saved to Downloads/FileConverter/. */
    fun pdfToImages(
        context: Context,
        pdfUri: Uri,
        dpi: Int,
        format: Bitmap.CompressFormat,
        pageRange: IntRange,
        baseName: String,
        onProgress: (Int, Int) -> Unit
    ): List<Uri> {
        val ext = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
        val mimeType = if (format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        val results = mutableListOf<Uri>()
        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return emptyList()
        pfd.use {
            val renderer = PdfRenderer(it)
            renderer.use {
                val clampedRange = pageRange.first.coerceAtLeast(0)..
                        pageRange.last.coerceAtMost(renderer.pageCount - 1)
                val total = clampedRange.count()
                clampedRange.forEachIndexed { progressIdx, pageIdx ->
                    onProgress(progressIdx + 1, total)
                    val bitmap = renderPage(renderer, pageIdx, dpi)
                    try {
                        val fileName = "${baseName}_page${pageIdx + 1}.$ext"
                        val pair = MediaStoreHelper.openImageOutputStream(context, fileName, mimeType)
                            ?: return@forEachIndexed
                        val (uri, stream) = pair
                        stream.use { s ->
                            val quality = if (format == Bitmap.CompressFormat.PNG) 100 else 90
                            bitmap.compress(format, quality, s)
                        }
                        MediaStoreHelper.finishPending(context, uri)
                        results.add(uri)
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
        }
        return results
    }

    /** Convert a .txt file to PDF. */
    fun textToPdf(
        context: Context,
        textUri: Uri,
        outputFileName: String,
        onProgress: (Int, Int) -> Unit
    ): Uri? {
        val lines = mutableListOf<String>()
        context.contentResolver.openInputStream(textUri)?.use { input ->
            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).forEachLine { lines.add(it) }
        }
        if (lines.isEmpty()) return null

        val pageW = A4_W
        val pageH = A4_H
        val marginH = 40
        val marginV = 50
        val fontSize = 12f
        val lineHeight = 18f
        val usableH = pageH - 2 * marginV
        val linesPerPage = (usableH / lineHeight).toInt()

        val paint = android.graphics.Paint().apply {
            textSize = fontSize
            color = android.graphics.Color.BLACK
            isAntiAlias = true
        }

        // Word-wrap each raw line
        val wrappedLines = mutableListOf<String>()
        val usableW = pageW - 2 * marginH
        for (rawLine in lines) {
            if (rawLine.isEmpty()) { wrappedLines.add(""); continue }
            val words = rawLine.split(" ")
            var current = StringBuilder()
            for (word in words) {
                val test = if (current.isEmpty()) word else "$current $word"
                if (paint.measureText(test) <= usableW) {
                    current = StringBuilder(test)
                } else {
                    if (current.isNotEmpty()) wrappedLines.add(current.toString())
                    current = StringBuilder(word)
                }
            }
            if (current.isNotEmpty()) wrappedLines.add(current.toString())
        }

        val totalPages = (wrappedLines.size + linesPerPage - 1) / linesPerPage
        val doc = PdfDocument()
        try {
            for (pageNum in 0 until totalPages) {
                onProgress(pageNum + 1, totalPages)
                val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, pageNum + 1).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                val startLine = pageNum * linesPerPage
                val endLine = minOf(startLine + linesPerPage, wrappedLines.size)
                for (li in startLine until endLine) {
                    val y = marginV + (li - startLine) * lineHeight + lineHeight
                    canvas.drawText(wrappedLines[li], marginH.toFloat(), y, paint)
                }
                doc.finishPage(page)
            }

            val pair = MediaStoreHelper.openOutputStream(context, outputFileName, "application/pdf")
                ?: return null
            val (uri, stream) = pair
            stream.use { doc.writeTo(it) }
            MediaStoreHelper.finishPending(context, uri)
            return uri
        } finally {
            doc.close()
        }
    }

    /**
     * Compress and downsample an existing PDF by re-rendering its pages
     * at specified DPI (96, 150, or 200) and JPEG quality.
     */
    fun compressPdf(
        context: Context,
        inputUri: Uri,
        targetDpi: Int,
        jpegQuality: Int,
        outputFileName: String,
        onProgress: (Int, Int) -> Unit
    ): Uri? {
        val pfd = context.contentResolver.openFileDescriptor(inputUri, "r") ?: return null
        val renderer = PdfRenderer(pfd)
        val doc = PdfDocument()

        try {
            val total = renderer.pageCount
            for (i in 0 until total) {
                onProgress(i + 1, total)
                val page = renderer.openPage(i)
                val scale = targetDpi / 72f
                val w = (page.width * scale).toInt().coerceAtLeast(1)
                val h = (page.height * scale).toInt().coerceAtLeast(1)
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                // Compress bitmap to JPEG stream to reduce size
                val baos = java.io.ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality, baos)
                bmp.recycle()
                val compressedBmp = android.graphics.BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size())

                // Write into PDF page
                val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                val pdfPage = doc.startPage(pageInfo)
                val canvas = pdfPage.canvas
                val rect = android.graphics.Rect(0, 0, page.width, page.height)
                canvas.drawBitmap(compressedBmp, null, rect, null)
                doc.finishPage(pdfPage)
                compressedBmp.recycle()
            }

            val pair = MediaStoreHelper.openOutputStream(context, outputFileName, "application/pdf") ?: return null
            val (uri, stream) = pair
            stream.use { doc.writeTo(it) }
            MediaStoreHelper.finishPending(context, uri)
            return uri
        } finally {
            doc.close()
            renderer.close()
            pfd.close()
        }
    }

    // ---------- helpers ----------

    private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                android.graphics.BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaledBitmapToFit(src: Bitmap, maxW: Int, maxH: Int): Bitmap {
        if (src.width <= maxW && src.height <= maxH) return src
        val scale = minOf(maxW.toFloat() / src.width, maxH.toFloat() / src.height)
        return Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true)
    }

    private fun getPageSize(
        mode: PageSizeMode, landscape: Boolean, bitmap: Bitmap, margin: Int
    ): Pair<Int, Int> {
        return when (mode) {
            PageSizeMode.A4 -> if (landscape) Pair(A4_H, A4_W) else Pair(A4_W, A4_H)
            PageSizeMode.LETTER -> if (landscape) Pair(LETTER_H, LETTER_W) else Pair(LETTER_W, LETTER_H)
            PageSizeMode.FIT_IMAGE -> {
                // Scale image to fit within 2000pt with margin
                val max = 2000 - 2 * margin
                val scale = minOf(max.toFloat() / bitmap.width, max.toFloat() / bitmap.height, 1f)
                val w = (bitmap.width * scale).toInt() + 2 * margin
                val h = (bitmap.height * scale).toInt() + 2 * margin
                if (landscape) Pair(maxOf(w, h), minOf(w, h)) else Pair(w, h)
            }
        }
    }
}

enum class PageSizeMode { A4, LETTER, FIT_IMAGE }

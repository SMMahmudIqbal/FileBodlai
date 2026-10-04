package com.example.fileconverter.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.fileconverter.util.MediaStoreHelper
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipFile

/**
 * Ultra-clean, 100% offline Word (.docx) to PDF converter.
 * Parses OpenXML WordprocessingML (.docx) archives, layout flow, typography,
 * headings, lists, tables, and embedded images into a publication-grade PDF.
 */
object DocxToPdfConverter {

    private const val A4_WIDTH_PT = 595
    private const val A4_HEIGHT_PT = 842
    private const val MARGIN_LEFT = 48f
    private const val MARGIN_RIGHT = 48f
    private const val MARGIN_TOP = 54f
    private const val MARGIN_BOTTOM = 54f
    private const val USABLE_WIDTH = A4_WIDTH_PT - MARGIN_LEFT - MARGIN_RIGHT // 499 pt
    private const val USABLE_HEIGHT = A4_HEIGHT_PT - MARGIN_TOP - MARGIN_BOTTOM // 734 pt
    private const val EMU_PER_PT = 12700f

    enum class ParagraphStyle { TITLE, HEADING1, HEADING2, HEADING3, BODY }

    data class DocxRun(
        val text: String,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val fontSizePt: Float = 11f,
        val color: Int = Color.parseColor("#1F2937")
    )

    data class DocxParagraph(
        val runs: List<DocxRun>,
        val style: ParagraphStyle = ParagraphStyle.BODY,
        val align: Paint.Align = Paint.Align.LEFT,
        val isBullet: Boolean = false,
        val isNumbered: Boolean = false,
        val listIndex: Int = 0,
        val isPageBreakBefore: Boolean = false
    )

    data class DocxTableCell(val paragraphs: List<DocxParagraph>)
    data class DocxTableRow(val cells: List<DocxTableCell>)
    data class DocxTable(val rows: List<DocxTableRow>)

    data class DocxImage(
        val relId: String,
        val widthPt: Float,
        val heightPt: Float
    )

    sealed class DocxElement {
        data class Para(val paragraph: DocxParagraph) : DocxElement()
        data class Img(val image: DocxImage) : DocxElement()
        data class Tbl(val table: DocxTable) : DocxElement()
        object PageBreak : DocxElement()
    }

    fun convert(
        context: Context,
        docxUri: Uri,
        outputFileName: String,
        onProgress: (Int, Int) -> Unit
    ): Uri? {
        val tempFile = File.createTempFile("docx_in_", ".zip", context.cacheDir)
        try {
            context.contentResolver.openInputStream(docxUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            val zip = ZipFile(tempFile)
            zip.use { zipFile ->
                val docEntry = zipFile.getEntry("word/document.xml") ?: return null
                val docXml = zipFile.getInputStream(docEntry).bufferedReader().use { it.readText() }

                // Parse document relationships for media
                val mediaRels = mutableMapOf<String, String>()
                val relsEntry = zipFile.getEntry("word/_rels/document.xml.rels")
                if (relsEntry != null) {
                    val relsXml = zipFile.getInputStream(relsEntry).bufferedReader().use { it.readText() }
                    mediaRels.putAll(parseRels(relsXml))
                }

                val elements = parseDocumentXml(docXml)
                if (elements.isEmpty()) return null

                // Lay out elements into PDF pages
                val doc = PdfDocument()
                try {
                    var pageIndex = 1
                    var currentY = MARGIN_TOP

                    var pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PT, A4_HEIGHT_PT, pageIndex).create()
                    var currentPage = doc.startPage(pageInfo)
                    var canvas = currentPage.canvas

                    fun advancePage() {
                        // Draw footer page number
                        drawFooter(canvas, pageIndex)
                        doc.finishPage(currentPage)
                        pageIndex++
                        pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PT, A4_HEIGHT_PT, pageIndex).create()
                        currentPage = doc.startPage(pageInfo)
                        canvas = currentPage.canvas
                        currentY = MARGIN_TOP
                    }

                    val totalElements = elements.size
                    for ((idx, elem) in elements.withIndex()) {
                        if (idx % 5 == 0) {
                            onProgress(idx + 1, totalElements)
                        }

                        when (elem) {
                            is DocxElement.PageBreak -> {
                                advancePage()
                            }
                            is DocxElement.Para -> {
                                val p = elem.paragraph
                                if (p.isPageBreakBefore) {
                                    advancePage()
                                }

                                val fullText = p.runs.joinToString("") { it.text }
                                if (fullText.isBlank()) {
                                    currentY += 10f
                                    if (currentY > A4_HEIGHT_PT - MARGIN_BOTTOM) advancePage()
                                    continue
                                }

                                val (fontSize, lineHeight, spacingTop, spacingBottom, defaultColor) = when (p.style) {
                                    ParagraphStyle.TITLE -> Tuple5(22f, 28f, 12f, 16f, Color.parseColor("#0F172A"))
                                    ParagraphStyle.HEADING1 -> Tuple5(17f, 24f, 16f, 10f, Color.parseColor("#1E3A8A"))
                                    ParagraphStyle.HEADING2 -> Tuple5(14f, 20f, 12f, 6f, Color.parseColor("#1E293B"))
                                    ParagraphStyle.HEADING3 -> Tuple5(12.5f, 18f, 8f, 4f, Color.parseColor("#334155"))
                                    ParagraphStyle.BODY -> Tuple5(10.5f, 15.5f, 2f, 6f, Color.parseColor("#1F2937"))
                                }

                                currentY += spacingTop
                                if (currentY > A4_HEIGHT_PT - MARGIN_BOTTOM - lineHeight) advancePage()

                                val paint = Paint().apply {
                                    isAntiAlias = true
                                    textSize = fontSize
                                }

                                val bulletPrefix = when {
                                    p.isBullet -> "•   "
                                    p.isNumbered -> "${p.listIndex}.  "
                                    else -> ""
                                }
                                val indent = if (p.isBullet || p.isNumbered) 16f else 0f
                                val usableWidthForText = USABLE_WIDTH - indent

                                // Word wrap
                                val words = (bulletPrefix + fullText).split(" ")
                                var currentLine = StringBuilder()

                                for (w in words) {
                                    val candidate = if (currentLine.isEmpty()) w else "$currentLine $w"
                                    if (paint.measureText(candidate) <= usableWidthForText) {
                                        currentLine = StringBuilder(candidate)
                                    } else {
                                        if (currentLine.isNotEmpty()) {
                                            if (currentY + lineHeight > A4_HEIGHT_PT - MARGIN_BOTTOM) {
                                                advancePage()
                                            }
                                            drawFormattedLine(
                                                canvas = canvas,
                                                line = currentLine.toString(),
                                                x = MARGIN_LEFT + indent,
                                                y = currentY + fontSize,
                                                usableW = usableWidthForText,
                                                align = p.align,
                                                style = p.style,
                                                paint = paint,
                                                color = defaultColor
                                            )
                                            currentY += lineHeight
                                        }
                                        currentLine = StringBuilder(w)
                                    }
                                }

                                if (currentLine.isNotEmpty()) {
                                    if (currentY + lineHeight > A4_HEIGHT_PT - MARGIN_BOTTOM) {
                                        advancePage()
                                    }
                                    drawFormattedLine(
                                        canvas = canvas,
                                        line = currentLine.toString(),
                                        x = MARGIN_LEFT + indent,
                                        y = currentY + fontSize,
                                        usableW = usableWidthForText,
                                        align = p.align,
                                        style = p.style,
                                        paint = paint,
                                        color = defaultColor
                                    )
                                    currentY += lineHeight
                                }

                                currentY += spacingBottom
                            }

                            is DocxElement.Img -> {
                                val target = mediaRels[elem.image.relId]
                                if (target != null) {
                                    val cleanPath = normalizeMediaPath(target)
                                    val imgEntry = zipFile.getEntry(cleanPath)
                                    if (imgEntry != null) {
                                        val bmp = decodeBitmapSafely(zipFile.getInputStream(imgEntry))
                                        if (bmp != null) {
                                            var w = if (elem.image.widthPt > 20) elem.image.widthPt else bmp.width.toFloat()
                                            var h = if (elem.image.heightPt > 20) elem.image.heightPt else bmp.height.toFloat()

                                            // Scale down to usable bounds
                                            val maxAllowedW = USABLE_WIDTH
                                            val maxAllowedH = 340f
                                            val scale = minOf(maxAllowedW / w, maxAllowedH / h, 1f)
                                            w *= scale
                                            h *= scale

                                            if (currentY + h > A4_HEIGHT_PT - MARGIN_BOTTOM) {
                                                advancePage()
                                            }

                                            val left = MARGIN_LEFT + (USABLE_WIDTH - w) / 2f
                                            val destRect = RectF(left, currentY + 6f, left + w, currentY + 6f + h)
                                            canvas.drawBitmap(bmp, null, destRect, null)
                                            bmp.recycle()

                                            currentY += h + 16f
                                        }
                                    }
                                }
                            }

                            is DocxElement.Tbl -> {
                                currentY = drawTable(canvas, elem.table, currentY, ::advancePage)
                            }
                        }
                    }

                    drawFooter(canvas, pageIndex)
                    doc.finishPage(currentPage)
                    onProgress(totalElements, totalElements)

                    val pair = MediaStoreHelper.openOutputStream(context, outputFileName, "application/pdf")
                        ?: return null
                    val (resultUri, outStream) = pair
                    outStream.use { doc.writeTo(it) }
                    MediaStoreHelper.finishPending(context, resultUri)
                    return resultUri
                } finally {
                    doc.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            tempFile.delete()
        }
    }

    private fun drawFormattedLine(
        canvas: android.graphics.Canvas,
        line: String,
        x: Float,
        y: Float,
        usableW: Float,
        align: Paint.Align,
        style: ParagraphStyle,
        paint: Paint,
        color: Int
    ) {
        paint.color = color
        paint.typeface = when (style) {
            ParagraphStyle.TITLE, ParagraphStyle.HEADING1, ParagraphStyle.HEADING2 -> Typeface.DEFAULT_BOLD
            ParagraphStyle.HEADING3 -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else -> Typeface.DEFAULT
        }

        val textWidth = paint.measureText(line)
        val drawX = when (align) {
            Paint.Align.CENTER -> x + (usableW - textWidth) / 2f
            Paint.Align.RIGHT -> x + usableW - textWidth
            else -> x
        }
        canvas.drawText(line, drawX, y, paint)
    }

    private fun drawFooter(canvas: android.graphics.Canvas, pageIndex: Int) {
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 9f
            color = Color.parseColor("#94A3B8")
            typeface = Typeface.DEFAULT
        }
        val text = "Page $pageIndex"
        val textWidth = paint.measureText(text)
        val x = (A4_WIDTH_PT - textWidth) / 2f
        val y = A4_HEIGHT_PT - 24f
        canvas.drawText(text, x, y, paint)
    }

    private fun drawTable(
        canvas: android.graphics.Canvas,
        table: DocxTable,
        startY: Float,
        onPageBreak: () -> Unit
    ): Float {
        if (table.rows.isEmpty()) return startY
        var curY = startY + 8f

        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            textSize = 9.5f
            color = Color.parseColor("#1E293B")
            isAntiAlias = true
        }

        val colCount = table.rows.maxOfOrNull { it.cells.size } ?: 1
        val colWidth = USABLE_WIDTH / colCount.toFloat()
        val rowHeight = 22f

        for ((rIdx, row) in table.rows.withIndex()) {
            if (curY + rowHeight > A4_HEIGHT_PT - MARGIN_BOTTOM) {
                onPageBreak()
                curY = MARGIN_TOP
            }

            for (cIdx in 0 until colCount) {
                val cellLeft = MARGIN_LEFT + cIdx * colWidth
                val cellRect = RectF(cellLeft, curY, cellLeft + colWidth, curY + rowHeight)

                if (rIdx == 0) {
                    canvas.drawRect(cellRect, headerBgPaint)
                }
                canvas.drawRect(cellRect, borderPaint)

                if (cIdx < row.cells.size) {
                    val cell = row.cells[cIdx]
                    val cellText = cell.paragraphs.joinToString(" ") { p ->
                        p.runs.joinToString("") { it.text }
                    }.trim()

                    if (cellText.isNotEmpty()) {
                        val maxChars = (colWidth / 5.5f).toInt().coerceAtLeast(3)
                        val truncated = if (cellText.length > maxChars) cellText.take(maxChars - 2) + "…" else cellText
                        canvas.drawText(truncated, cellLeft + 6f, curY + 14.5f, textPaint)
                    }
                }
            }
            curY += rowHeight
        }
        return curY + 12f
    }

    // =========================================================================
    // Document XML Parsing
    // =========================================================================

    private fun parseDocumentXml(xml: String): List<DocxElement> {
        val elements = mutableListOf<DocxElement>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inParagraph = false
            var inRun = false
            var inTable = false
            var inTableRow = false
            var inTableCell = false

            var curStyle = ParagraphStyle.BODY
            var curAlign = Paint.Align.LEFT
            var curIsBullet = false
            var curIsNumbered = false
            var listIndex = 1
            var pageBreakBefore = false

            var currentRuns = mutableListOf<DocxRun>()
            var curRunBold = false
            var curRunItalic = false
            var curRunUnderline = false
            var curRunSize = 11f
            var curRunColor = Color.parseColor("#1F2937")

            // Table state
            var currentTableRows = mutableListOf<DocxTableRow>()
            var currentRowCells = mutableListOf<DocxTableCell>()
            var currentCellParas = mutableListOf<DocxParagraph>()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "p" -> {
                                inParagraph = true
                                currentRuns = mutableListOf()
                                curStyle = ParagraphStyle.BODY
                                curAlign = Paint.Align.LEFT
                                curIsBullet = false
                                curIsNumbered = false
                                pageBreakBefore = false
                            }
                            "pStyle" -> {
                                val v = parser.getAttributeValue(null, "val")?.lowercase() ?: ""
                                curStyle = when {
                                    v.contains("title") -> ParagraphStyle.TITLE
                                    v.contains("heading1") || v.contains("heading 1") -> ParagraphStyle.HEADING1
                                    v.contains("heading2") || v.contains("heading 2") -> ParagraphStyle.HEADING2
                                    v.contains("heading3") || v.contains("heading 3") -> ParagraphStyle.HEADING3
                                    else -> ParagraphStyle.BODY
                                }
                            }
                            "jc" -> {
                                val v = parser.getAttributeValue(null, "val")
                                if (v == "center") curAlign = Paint.Align.CENTER
                                else if (v == "right") curAlign = Paint.Align.RIGHT
                            }
                            "numPr" -> {
                                curIsBullet = true
                            }
                            "r" -> {
                                inRun = true
                                curRunBold = false
                                curRunItalic = false
                                curRunUnderline = false
                                curRunSize = 11f
                                curRunColor = Color.parseColor("#1F2937")
                            }
                            "b" -> curRunBold = true
                            "i" -> curRunItalic = true
                            "u" -> curRunUnderline = true
                            "sz" -> {
                                val szVal = parser.getAttributeValue(null, "val")?.toFloatOrNull()
                                if (szVal != null && szVal > 0) curRunSize = szVal / 2f
                            }
                            "color" -> {
                                val hex = parser.getAttributeValue(null, "val")
                                if (hex != null && hex.length == 6) {
                                    curRunColor = try { Color.parseColor("#$hex") } catch (_: Exception) { curRunColor }
                                }
                            }
                            "br" -> {
                                val brType = parser.getAttributeValue(null, "type")
                                if (brType == "page") {
                                    if (!inTable) {
                                        elements.add(DocxElement.PageBreak)
                                    }
                                }
                            }
                            "blip" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i) == "embed") {
                                        val embedId = parser.getAttributeValue(i)
                                        if (embedId != null && !inTable) {
                                            elements.add(DocxElement.Img(DocxImage(embedId, 0f, 0f)))
                                        }
                                        break
                                    }
                                }
                            }
                            "extent" -> {
                                val cx = parser.getAttributeValue(null, "cx")?.toLongOrNull()
                                val cy = parser.getAttributeValue(null, "cy")?.toLongOrNull()
                                if (cx != null && cy != null && elements.isNotEmpty()) {
                                    val last = elements.last()
                                    if (last is DocxElement.Img) {
                                        elements[elements.size - 1] = DocxElement.Img(
                                            last.image.copy(
                                                widthPt = cx / EMU_PER_PT,
                                                heightPt = cy / EMU_PER_PT
                                            )
                                        )
                                    }
                                }
                            }
                            "tbl" -> {
                                inTable = true
                                currentTableRows = mutableListOf()
                            }
                            "tr" -> {
                                inTableRow = true
                                currentRowCells = mutableListOf()
                            }
                            "tc" -> {
                                inTableCell = true
                                currentCellParas = mutableListOf()
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inRun && parser.text != null && parser.text.isNotEmpty()) {
                            currentRuns.add(
                                DocxRun(
                                    text = parser.text,
                                    isBold = curRunBold,
                                    isItalic = curRunItalic,
                                    isUnderline = curRunUnderline,
                                    fontSizePt = curRunSize,
                                    color = curRunColor
                                )
                            )
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "r" -> inRun = false
                            "p" -> {
                                inParagraph = false
                                val para = DocxParagraph(
                                    runs = currentRuns,
                                    style = curStyle,
                                    align = curAlign,
                                    isBullet = curIsBullet,
                                    isNumbered = curIsNumbered,
                                    listIndex = listIndex,
                                    isPageBreakBefore = pageBreakBefore
                                )
                                if (inTableCell) {
                                    currentCellParas.add(para)
                                } else {
                                    elements.add(DocxElement.Para(para))
                                }
                            }
                            "tc" -> {
                                inTableCell = false
                                currentRowCells.add(DocxTableCell(currentCellParas))
                            }
                            "tr" -> {
                                inTableRow = false
                                currentTableRows.add(DocxTableRow(currentRowCells))
                            }
                            "tbl" -> {
                                inTable = false
                                if (currentTableRows.isNotEmpty()) {
                                    elements.add(DocxElement.Tbl(DocxTable(currentTableRows)))
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return elements
    }

    private fun parseRels(xml: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "Relationship") {
                    val id = parser.getAttributeValue(null, "Id")
                    val target = parser.getAttributeValue(null, "Target")
                    if (id != null && target != null) {
                        map[id] = target
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return map
    }

    private fun normalizeMediaPath(target: String): String {
        var clean = target.replace("../", "")
        if (!clean.startsWith("word/")) {
            clean = "word/$clean"
        }
        return clean
    }

    private fun decodeBitmapSafely(stream: InputStream): Bitmap? {
        return try {
            val bytes = stream.readBytes()
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)

            var sample = 1
            while (opts.outWidth / sample > 2048 || opts.outHeight / sample > 2048) {
                sample *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
        } catch (_: Exception) {
            null
        }
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}

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
 * High-performance, 100% offline PPTX to PDF converter.
 * Parses OpenXML PresentationML (.pptx) ZIP archives, extracts slide layouts,
 * embedded imagery, backgrounds, and structured typography, and renders them
 * into a vector-accurate PDF document.
 */
object PptxToPdfConverter {

    private const val EMU_PER_PT = 12700f

    data class SlideInfo(
        val entryName: String,
        val slideNumber: Int,
        val relsEntryName: String
    )

    data class ExtractedImage(
        val bitmap: Bitmap,
        val destRect: RectF
    )

    data class TextRun(
        val text: String,
        val fontSizePt: Float = 14f,
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val color: Int = Color.BLACK
    )

    data class TextParagraph(
        val runs: List<TextRun>,
        val isBullet: Boolean = false,
        val alignment: Paint.Align = Paint.Align.LEFT
    )

    data class TextBox(
        val rect: RectF,
        val paragraphs: List<TextParagraph>,
        val bgColor: Int? = null
    )

    data class ParsedSlide(
        val bgColor: Int = Color.WHITE,
        val images: List<ExtractedImage> = emptyList(),
        val textBoxes: List<TextBox> = emptyList()
    )

    fun convert(
        context: Context,
        pptxUri: Uri,
        outputFileName: String,
        onProgress: (Int, Int) -> Unit
    ): Uri? {
        val tempFile = File.createTempFile("pptx_in_", ".zip", context.cacheDir)
        try {
            context.contentResolver.openInputStream(pptxUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return null

            val zip = ZipFile(tempFile)
            zip.use { zipFile ->
                // 1. Determine presentation dimensions
                var slideWidthPt = 720
                var slideHeightPt = 405 // 16:9 widescreen default (10in x 5.625in)

                val presEntry = zipFile.getEntry("ppt/presentation.xml")
                val slideRels = mutableMapOf<String, String>() // rId -> target
                val orderedSlideIds = mutableListOf<String>()

                if (presEntry != null) {
                    val xml = zipFile.getInputStream(presEntry).bufferedReader().use { it.readText() }
                    val dims = parsePresentationDimensions(xml)
                    if (dims != null) {
                        slideWidthPt = dims.first
                        slideHeightPt = dims.second
                    }
                    orderedSlideIds.addAll(parsePresentationSlideOrder(xml))
                }

                // Parse presentation rels
                val presRelsEntry = zipFile.getEntry("ppt/_rels/presentation.xml.rels")
                if (presRelsEntry != null) {
                    val relsXml = zipFile.getInputStream(presRelsEntry).bufferedReader().use { it.readText() }
                    slideRels.putAll(parseRels(relsXml))
                }

                // 2. Discover slides in sequence
                val slides = mutableListOf<SlideInfo>()
                if (orderedSlideIds.isNotEmpty() && slideRels.isNotEmpty()) {
                    for ((index, rId) in orderedSlideIds.withIndex()) {
                        val target = slideRels[rId] ?: continue
                        val cleanTarget = if (target.startsWith("/")) target.removePrefix("/")
                        else if (target.startsWith("ppt/")) target
                        else "ppt/$target"
                        val relsTarget = cleanTarget.replace("slides/", "slides/_rels/") + ".rels"
                        slides.add(SlideInfo(cleanTarget, index + 1, relsTarget))
                    }
                }

                // Fallback if ordered discovery yielded nothing
                if (slides.isEmpty()) {
                    val allEntries = zipFile.entries().asSequence().map { it.name }.toList()
                    val slideNames = allEntries.filter {
                        it.matches(Regex("ppt/slides/slide\\d+\\.xml"))
                    }.sortedBy {
                        Regex("\\d+").find(it)?.value?.toIntOrNull() ?: 0
                    }

                    slideNames.forEachIndexed { idx, name ->
                        val rels = name.replace("slides/", "slides/_rels/") + ".rels"
                        slides.add(SlideInfo(name, idx + 1, rels))
                    }
                }

                if (slides.isEmpty()) return null

                val totalSlides = slides.size
                val doc = PdfDocument()

                try {
                    for ((index, slideInfo) in slides.withIndex()) {
                        onProgress(index + 1, totalSlides)

                        val slideEntry = zipFile.getEntry(slideInfo.entryName) ?: continue
                        val slideXml = zipFile.getInputStream(slideEntry).bufferedReader().use { it.readText() }

                        // Parse slide relationships for media/images
                        val mediaRels = mutableMapOf<String, String>()
                        val relsEntry = zipFile.getEntry(slideInfo.relsEntryName)
                        if (relsEntry != null) {
                            val rXml = zipFile.getInputStream(relsEntry).bufferedReader().use { it.readText() }
                            mediaRels.putAll(parseRels(rXml))
                        }

                        val parsedSlide = parseSlideXml(
                            zipFile = zipFile,
                            slideXml = slideXml,
                            mediaRels = mediaRels,
                            slideWidthPt = slideWidthPt.toFloat(),
                            slideHeightPt = slideHeightPt.toFloat()
                        )

                        // Render page
                        val pageInfo = PdfDocument.PageInfo.Builder(slideWidthPt, slideHeightPt, index + 1).create()
                        val page = doc.startPage(pageInfo)
                        val canvas = page.canvas

                        // Draw background
                        canvas.drawColor(parsedSlide.bgColor)

                        // Draw images
                        for (img in parsedSlide.images) {
                            canvas.drawBitmap(img.bitmap, null, img.destRect, null)
                            img.bitmap.recycle()
                        }

                        // Draw text boxes
                        for (box in parsedSlide.textBoxes) {
                            drawTextBox(canvas, box)
                        }

                        doc.finishPage(page)
                    }

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

    // =========================================================================
    // Parsing Helpers
    // =========================================================================

    private fun parsePresentationDimensions(xml: String): Pair<Int, Int>? {
        return try {
            val parser = createParser(xml)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "sldSz") {
                    val cx = parser.getAttributeValue(null, "cx")?.toLongOrNull() ?: 9144000L
                    val cy = parser.getAttributeValue(null, "cy")?.toLongOrNull() ?: 5143500L
                    val wPt = (cx / EMU_PER_PT).toInt().coerceAtLeast(100)
                    val hPt = (cy / EMU_PER_PT).toInt().coerceAtLeast(100)
                    return Pair(wPt, hPt)
                }
                eventType = parser.next()
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parsePresentationSlideOrder(xml: String): List<String> {
        val result = mutableListOf<String>()
        try {
            val parser = createParser(xml)
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "sldId") {
                    for (i in 0 until parser.attributeCount) {
                        val attrName = parser.getAttributeName(i)
                        if (attrName == "id" && parser.getAttributePrefix(i) == "r") {
                            result.add(parser.getAttributeValue(i))
                            break
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {}
        return result
    }

    private fun parseRels(xml: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val parser = createParser(xml)
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

    private fun parseSlideXml(
        zipFile: ZipFile,
        slideXml: String,
        mediaRels: Map<String, String>,
        slideWidthPt: Float,
        slideHeightPt: Float
    ): ParsedSlide {
        var bgColor = Color.WHITE
        val images = mutableListOf<ExtractedImage>()
        val textBoxes = mutableListOf<TextBox>()

        try {
            val parser = createParser(slideXml)
            var eventType = parser.eventType

            var inBg = false
            var inPic = false
            var inSp = false
            var inTxBody = false
            var inParagraph = false
            var inRun = false

            // Track current coordinates
            var curOffX = 0f
            var curOffY = 0f
            var curExtW = slideWidthPt
            var curExtH = slideHeightPt

            // Current shape state
            var currentParagraphs = mutableListOf<TextParagraph>()
            var currentRuns = mutableListOf<TextRun>()
            var curRunBold = false
            var curRunItalic = false
            var curRunFontSize = 14f
            var curRunColor = Color.BLACK
            var curAlign = Paint.Align.LEFT
            var curIsBullet = false

            var picEmbedId: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "bg" -> inBg = true
                            "pic" -> {
                                inPic = true
                                curOffX = 0f
                                curOffY = 0f
                                curExtW = slideWidthPt
                                curExtH = slideHeightPt
                                picEmbedId = null
                            }
                            "sp" -> {
                                inSp = true
                                curOffX = 40f
                                curOffY = 40f
                                curExtW = slideWidthPt - 80f
                                curExtH = slideHeightPt - 80f
                                currentParagraphs = mutableListOf()
                            }
                            "txBody" -> inTxBody = true
                            "p" -> {
                                inParagraph = true
                                currentRuns = mutableListOf()
                                curAlign = Paint.Align.LEFT
                                curIsBullet = false
                            }
                            "pPr" -> {
                                val algn = parser.getAttributeValue(null, "algn")
                                if (algn == "ctr") curAlign = Paint.Align.CENTER
                                else if (algn == "r") curAlign = Paint.Align.RIGHT
                            }
                            "buChar", "buFont" -> curIsBullet = true
                            "r" -> {
                                inRun = true
                                curRunBold = false
                                curRunItalic = false
                                curRunFontSize = 14f
                                curRunColor = Color.BLACK
                            }
                            "rPr" -> {
                                val b = parser.getAttributeValue(null, "b")
                                if (b == "1" || b == "true") curRunBold = true
                                val i = parser.getAttributeValue(null, "i")
                                if (i == "1" || i == "true") curRunItalic = true
                                val sz = parser.getAttributeValue(null, "sz")?.toFloatOrNull()
                                if (sz != null && sz > 0) {
                                    curRunFontSize = (sz / 100f).coerceIn(8f, 72f)
                                }
                            }
                            "srgbClr" -> {
                                val hex = parser.getAttributeValue(null, "val")
                                if (hex != null && hex.length == 6) {
                                    val parsedColor = parseHexColor(hex)
                                    if (inBg) bgColor = parsedColor
                                    else if (inRun) curRunColor = parsedColor
                                }
                            }
                            "off" -> {
                                val x = parser.getAttributeValue(null, "x")?.toLongOrNull() ?: 0L
                                val y = parser.getAttributeValue(null, "y")?.toLongOrNull() ?: 0L
                                curOffX = x / EMU_PER_PT
                                curOffY = y / EMU_PER_PT
                            }
                            "ext" -> {
                                val cx = parser.getAttributeValue(null, "cx")?.toLongOrNull()
                                val cy = parser.getAttributeValue(null, "cy")?.toLongOrNull()
                                if (cx != null && cy != null) {
                                    curExtW = cx / EMU_PER_PT
                                    curExtH = cy / EMU_PER_PT
                                }
                            }
                            "blip" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i) == "embed") {
                                        picEmbedId = parser.getAttributeValue(i)
                                        break
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inRun && parser.text != null && parser.text.isNotEmpty()) {
                            currentRuns.add(
                                TextRun(
                                    text = parser.text,
                                    fontSizePt = curRunFontSize,
                                    isBold = curRunBold,
                                    isItalic = curRunItalic,
                                    color = curRunColor
                                )
                            )
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "bg" -> inBg = false
                            "r" -> inRun = false
                            "p" -> {
                                inParagraph = false
                                if (currentRuns.isNotEmpty()) {
                                    currentParagraphs.add(
                                        TextParagraph(
                                            runs = currentRuns,
                                            isBullet = curIsBullet,
                                            alignment = curAlign
                                        )
                                    )
                                }
                            }
                            "txBody" -> inTxBody = false
                            "sp" -> {
                                inSp = false
                                if (currentParagraphs.isNotEmpty()) {
                                    val rect = RectF(
                                        curOffX, curOffY,
                                        curOffX + curExtW, curOffY + curExtH
                                    )
                                    textBoxes.add(TextBox(rect, currentParagraphs))
                                }
                            }
                            "pic" -> {
                                inPic = false
                                if (picEmbedId != null) {
                                    val target = mediaRels[picEmbedId]
                                    if (target != null) {
                                        val mediaEntryName = normalizeMediaPath(target)
                                        val entry = zipFile.getEntry(mediaEntryName)
                                        if (entry != null) {
                                            val bmp = decodeBitmapSafely(zipFile.getInputStream(entry))
                                            if (bmp != null) {
                                                val rect = RectF(
                                                    curOffX, curOffY,
                                                    curOffX + curExtW, curOffY + curExtH
                                                )
                                                images.add(ExtractedImage(bmp, rect))
                                            }
                                        }
                                    }
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

        return ParsedSlide(bgColor, images, textBoxes)
    }

    private fun drawTextBox(canvas: android.graphics.Canvas, box: TextBox) {
        val paint = Paint().apply { isAntiAlias = true }
        var y = box.rect.top + 16f
        val startX = box.rect.left
        val usableWidth = box.rect.width().coerceAtLeast(40f)

        for (para in box.paragraphs) {
            val bulletPrefix = if (para.isBullet) "•  " else ""
            val fullText = bulletPrefix + para.runs.joinToString("") { it.text }
            if (fullText.isBlank()) {
                y += 12f
                continue
            }

            val primaryRun = para.runs.firstOrNull() ?: TextRun("")
            paint.textSize = primaryRun.fontSizePt
            paint.color = primaryRun.color
            paint.typeface = when {
                primaryRun.isBold && primaryRun.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                primaryRun.isBold -> Typeface.DEFAULT_BOLD
                primaryRun.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }

            // Word wrapping
            val words = fullText.split(" ")
            var currentLine = StringBuilder()

            for (w in words) {
                val candidate = if (currentLine.isEmpty()) w else "$currentLine $w"
                if (paint.measureText(candidate) <= usableWidth) {
                    currentLine = StringBuilder(candidate)
                } else {
                    if (currentLine.isNotEmpty()) {
                        drawAlignedLine(canvas, currentLine.toString(), startX, y, usableWidth, para.alignment, paint)
                        y += paint.textSize * 1.35f
                    }
                    currentLine = StringBuilder(w)
                }
            }
            if (currentLine.isNotEmpty()) {
                drawAlignedLine(canvas, currentLine.toString(), startX, y, usableWidth, para.alignment, paint)
                y += paint.textSize * 1.45f
            }

            if (y > box.rect.bottom) break
        }
    }

    private fun drawAlignedLine(
        canvas: android.graphics.Canvas,
        line: String,
        left: Float,
        y: Float,
        width: Float,
        align: Paint.Align,
        paint: Paint
    ) {
        val x = when (align) {
            Paint.Align.CENTER -> left + (width / 2f) - (paint.measureText(line) / 2f)
            Paint.Align.RIGHT -> left + width - paint.measureText(line)
            else -> left
        }
        canvas.drawText(line, x, y, paint)
    }

    private fun normalizeMediaPath(target: String): String {
        var clean = target.replace("../", "")
        if (!clean.startsWith("ppt/")) {
            clean = "ppt/$clean"
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

    private fun parseHexColor(hex: String): Int {
        return try {
            Color.parseColor("#$hex")
        } catch (_: Exception) {
            Color.BLACK
        }
    }

    private fun createParser(xml: String): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))
        return parser
    }
}

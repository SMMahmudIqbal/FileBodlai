package com.example.fileconverter.converter

import android.content.Context
import android.net.Uri
import com.example.fileconverter.util.MediaStoreHelper
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.StringReader
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Transforms Word documents (.docx) into native, editable PowerPoint presentations (.pptx).
 * Analyzes semantic structure (titles, headings, bullet lists, body paragraphs, and images)
 * to intelligently create presentation slides with styled layouts.
 */
object DocxToPptxConverter {

    private const val SLIDE_WIDTH_EMU = 9144000L   // 10 inches
    private const val SLIDE_HEIGHT_EMU = 5143500L  // 5.625 inches (16:9 widescreen)

    data class SlideData(
        val title: String,
        val bullets: List<String>,
        val isTitleSlide: Boolean = false,
        val subtitle: String = "",
        val imageBytes: ByteArray? = null,
        val imageExt: String = "jpg"
    )

    data class DocxSection(
        var title: String = "",
        val items: MutableList<String> = mutableListOf(),
        var imageRelId: String? = null
    )

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
            val slides = mutableListOf<SlideData>()

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

                val sections = parseDocxSections(docXml)
                if (sections.isEmpty()) return null

                // Determine Title Slide
                val firstSec = sections.first()
                val mainTitle = firstSec.title.ifBlank { "Presentation" }
                val mainSubtitle = if (firstSec.items.isNotEmpty()) firstSec.items.first() else "Converted from Word Document"

                slides.add(
                    SlideData(
                        title = mainTitle,
                        bullets = emptyList(),
                        isTitleSlide = true,
                        subtitle = mainSubtitle
                    )
                )

                // Add content slides
                val remainingSections = if (firstSec.items.size <= 1 && sections.size > 1) {
                    sections.drop(1)
                } else {
                    sections
                }

                for (sec in remainingSections) {
                    // Extract image bytes if present
                    var imgBytes: ByteArray? = null
                    var imgExt = "jpg"
                    if (sec.imageRelId != null) {
                        val target = mediaRels[sec.imageRelId]
                        if (target != null) {
                            val cleanPath = normalizeMediaPath(target)
                            val entry = zipFile.getEntry(cleanPath)
                            if (entry != null) {
                                imgBytes = zipFile.getInputStream(entry).readBytes()
                                imgExt = if (cleanPath.endsWith(".png", ignoreCase = true)) "png" else "jpg"
                            }
                        }
                    }

                    // Chunk bullets if too long (max 5 per slide for high visual clarity)
                    val chunks = if (sec.items.isEmpty()) listOf(listOf("• Overview")) else sec.items.chunked(5)
                    for ((cIdx, chunk) in chunks.withIndex()) {
                        val slideTitle = if (cIdx == 0) {
                            sec.title.ifBlank { "Key Points" }
                        } else {
                            "${sec.title.ifBlank { "Key Points" }} (Cont.)"
                        }
                        slides.add(
                            SlideData(
                                title = slideTitle,
                                bullets = chunk,
                                isTitleSlide = false,
                                imageBytes = if (cIdx == 0) imgBytes else null,
                                imageExt = imgExt
                            )
                        )
                    }
                }
            }

            if (slides.isEmpty()) return null

            // Build PPTX ZIP
            val pair = MediaStoreHelper.openOutputStream(
                context,
                outputFileName,
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            ) ?: return null

            val (resultUri, outStream) = pair
            val totalSlides = slides.size

            ZipOutputStream(outStream.buffered()).use { pptxZip ->
                // Static OpenXML structure
                writeEntry(pptxZip, "[Content_Types].xml", contentTypesXml(totalSlides, slides))
                writeEntry(pptxZip, "_rels/.rels", rootRelsXml())
                writeEntry(pptxZip, "ppt/presentation.xml", presentationXml(totalSlides))
                writeEntry(pptxZip, "ppt/_rels/presentation.xml.rels", presentationRelsXml(totalSlides))
                writeEntry(pptxZip, "ppt/slideMasters/slideMaster1.xml", slideMasterXml())
                writeEntry(pptxZip, "ppt/slideMasters/_rels/slideMaster1.xml.rels", slideMasterRelsXml())
                writeEntry(pptxZip, "ppt/slideLayouts/slideLayout1.xml", slideLayoutXml())
                writeEntry(pptxZip, "ppt/slideLayouts/_rels/slideLayout1.xml.rels", slideLayoutRelsXml())

                // Per-slide entries
                for ((idx, slide) in slides.withIndex()) {
                    onProgress(idx + 1, totalSlides)
                    val slideNum = idx + 1
                    var imageFilename: String? = null

                    if (slide.imageBytes != null) {
                        imageFilename = "image${slideNum}.${slide.imageExt}"
                        writeEntry(pptxZip, "ppt/media/$imageFilename", slide.imageBytes)
                    }

                    writeEntry(pptxZip, "ppt/slides/slide${slideNum}.xml", generateSlideXml(slide, slideNum, imageFilename != null))
                    writeEntry(pptxZip, "ppt/slides/_rels/slide${slideNum}.xml.rels", generateSlideRelsXml(imageFilename))
                }
            }

            MediaStoreHelper.finishPending(context, resultUri)
            return resultUri
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            tempFile.delete()
        }
    }

    // =========================================================================
    // Word Parsing & Semantic Segmentation
    // =========================================================================

    private fun parseDocxSections(xml: String): List<DocxSection> {
        val sections = mutableListOf<DocxSection>()
        var currentSection = DocxSection()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inParagraph = false
            var inRun = false
            var isHeading = false
            var isTitle = false
            var isPageBreak = false
            var currentRuns = StringBuilder()
            var lastImageEmbed: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tag) {
                            "p" -> {
                                inParagraph = true
                                isHeading = false
                                isTitle = false
                                isPageBreak = false
                                currentRuns = StringBuilder()
                            }
                            "pStyle" -> {
                                val v = parser.getAttributeValue(null, "val")?.lowercase() ?: ""
                                if (v.contains("title")) isTitle = true
                                else if (v.contains("heading1") || v.contains("heading 1") || v.contains("heading2")) isHeading = true
                            }
                            "r" -> inRun = true
                            "br" -> {
                                if (parser.getAttributeValue(null, "type") == "page") isPageBreak = true
                            }
                            "blip" -> {
                                for (i in 0 until parser.attributeCount) {
                                    if (parser.getAttributeName(i) == "embed") {
                                        lastImageEmbed = parser.getAttributeValue(i)
                                        break
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inRun && parser.text != null) {
                            currentRuns.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "r" -> inRun = false
                            "p" -> {
                                inParagraph = false
                                val text = currentRuns.toString().trim()

                                if (isPageBreak && currentSection.items.isNotEmpty()) {
                                    sections.add(currentSection)
                                    currentSection = DocxSection()
                                }

                                if (text.isNotEmpty()) {
                                    if (isTitle || isHeading) {
                                        if (currentSection.items.isNotEmpty() || currentSection.title.isNotBlank()) {
                                            sections.add(currentSection)
                                            currentSection = DocxSection(title = text)
                                        } else {
                                            currentSection.title = text
                                        }
                                    } else {
                                        if (currentSection.title.isBlank() && sections.isEmpty()) {
                                            currentSection.title = text
                                        } else {
                                            currentSection.items.add(text)
                                        }
                                    }
                                }

                                if (lastImageEmbed != null) {
                                    currentSection.imageRelId = lastImageEmbed
                                    lastImageEmbed = null
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            if (currentSection.title.isNotBlank() || currentSection.items.isNotEmpty()) {
                sections.add(currentSection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If no sections were identified, create fallback
        if (sections.isEmpty()) {
            sections.add(DocxSection(title = "Summary", items = mutableListOf("Document contents parsed.")))
        }

        return sections
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

    // =========================================================================
    // OpenXML PresentationML Generators
    // =========================================================================

    private fun contentTypesXml(totalSlides: Int, slides: List<SlideData>): String {
        val slideOverrides = (1..totalSlides).joinToString("\n") { n ->
            """  <Override PartName="/ppt/slides/slide$n.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>"""
        }
        val hasPng = slides.any { it.imageExt.equals("png", ignoreCase = true) }
        val hasJpg = slides.any { it.imageExt.equals("jpg", ignoreCase = true) || it.imageExt.equals("jpeg", ignoreCase = true) }

        val imageDefaults = buildString {
            if (hasPng) appendLine("""  <Default Extension="png" ContentType="image/png"/>""")
            if (hasJpg || !hasPng) appendLine("""  <Default Extension="jpg" ContentType="image/jpeg"/>""")
        }

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
$imageDefaults  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
$slideOverrides
</Types>"""
    }

    private fun rootRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>"""

    private fun presentationXml(totalSlides: Int): String {
        val sldIdList = (1..totalSlides).joinToString("\n") { n ->
            """    <p:sldId id="${256 + n}" r:id="rId$n"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                saveSubsetFonts="1">
  <p:sldMasterIdLst>
    <p:sldMasterId id="2147483648" r:id="rId${totalSlides + 1}"/>
  </p:sldMasterIdLst>
  <p:sldIdLst>
$sldIdList
  </p:sldIdLst>
  <p:sldSz cx="$SLIDE_WIDTH_EMU" cy="$SLIDE_HEIGHT_EMU" type="screen16x9"/>
  <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>"""
    }

    private fun presentationRelsXml(totalSlides: Int): String {
        val slideRels = (1..totalSlides).joinToString("\n") { n ->
            """  <Relationship Id="rId$n" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide$n.xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
$slideRels
  <Relationship Id="rId${totalSlides + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
</Relationships>"""
    }

    private fun slideMasterXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <p:cSld>
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
  <p:sldLayoutIdLst>
    <p:sldLayoutId id="2147483649" r:id="rId1"/>
  </p:sldLayoutIdLst>
  <p:txStyles>
    <p:titleStyle/>
    <p:bodyStyle/>
    <p:otherStyle/>
  </p:txStyles>
</p:sldMaster>"""

    private fun slideMasterRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>"""

    private fun slideLayoutXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
             type="blank" preserve="1">
  <p:cSld name="Blank">
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sldLayout>"""

    private fun slideLayoutRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>"""

    private fun generateSlideXml(slide: SlideData, slideNum: Int, hasImage: Boolean): String {
        return if (slide.isTitleSlide) {
            generateTitleSlideXml(slide)
        } else {
            generateContentSlideXml(slide, slideNum, hasImage)
        }
    }

    private fun generateTitleSlideXml(slide: SlideData): String {
        val safeTitle = escapeXml(slide.title)
        val safeSub = escapeXml(slide.subtitle)

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
       xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
       xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <p:cSld>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
      <!-- Title Box -->
      <p:sp>
        <p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr><p:nvPr/></p:nvSpPr>
        <p:spPr>
          <a:xfrm><a:off x="914400" y="1600000"/><a:ext cx="7315200" cy="1400000"/></a:xfrm>
          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        </p:spPr>
        <p:txBody>
          <a:bodyPr vert="horz" lIns="0" tIns="0" rIns="0" bIns="0" anchor="ctr"/>
          <a:lstStyle/>
          <a:p>
            <a:r>
              <a:rPr lang="en-US" sz="3800" b="1">
                <a:solidFill><a:srgbClr val="0F172A"/></a:solidFill>
              </a:rPr>
              <a:t>$safeTitle</a:t>
            </a:r>
          </a:p>
          <a:p>
            <a:pPr><a:spcBfr><a:spcPts val="1200"/></a:spcBfr></a:pPr>
            <a:r>
              <a:rPr lang="en-US" sz="1800">
                <a:solidFill><a:srgbClr val="64748B"/></a:solidFill>
              </a:rPr>
              <a:t>$safeSub</a:t>
            </a:r>
          </a:p>
        </p:txBody>
      </p:sp>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>"""
    }

    private fun generateContentSlideXml(slide: SlideData, slideNum: Int, hasImage: Boolean): String {
        val safeTitle = escapeXml(slide.title)
        val bodyWidth = if (hasImage) 4600000L else 7772400L

        val bulletParagraphs = slide.bullets.joinToString("\n") { bulletText ->
            val safeText = escapeXml(bulletText)
            """          <a:p>
            <a:pPr marL="285750" indent="-285750">
              <a:spcBfr><a:spcPts val="800"/></a:spcBfr>
              <a:buFont typeface="Arial"/>
              <a:buChar char="•"/>
            </a:pPr>
            <a:r>
              <a:rPr lang="en-US" sz="1700">
                <a:solidFill><a:srgbClr val="1E293B"/></a:solidFill>
              </a:rPr>
              <a:t>$safeText</a:t>
            </a:r>
          </a:p>"""
        }

        val imageShapeXml = if (hasImage) {
            """      <!-- Section Image -->
      <p:pic>
        <p:nvPicPr>
          <p:cNvPr id="4" name="Picture"/>
          <p:cNvPicPr><a:picLocks noChangeAspect="1"/></p:cNvPicPr>
          <p:nvPr/>
        </p:nvPicPr>
        <p:blipFill>
          <a:blip r:embed="rId2"/>
          <a:stretch><a:fillRect/></a:stretch>
        </p:blipFill>
        <p:spPr>
          <a:xfrm><a:off x="5600000" y="1300000"/><a:ext cx="2800000" cy="3200000"/></a:xfrm>
          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        </p:spPr>
      </p:pic>"""
        } else ""

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
       xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
       xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <p:cSld>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
      <!-- Title Shape -->
      <p:sp>
        <p:nvSpPr><p:cNvPr id="2" name="Title"/><p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr><p:nvPr/></p:nvSpPr>
        <p:spPr>
          <a:xfrm><a:off x="685800" y="457200"/><a:ext cx="7772400" cy="650000"/></a:xfrm>
          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        </p:spPr>
        <p:txBody>
          <a:bodyPr vert="horz" lIns="0" tIns="0" rIns="0" bIns="0" anchor="ctr"/>
          <a:lstStyle/>
          <a:p>
            <a:r>
              <a:rPr lang="en-US" sz="2600" b="1">
                <a:solidFill><a:srgbClr val="0F172A"/></a:solidFill>
              </a:rPr>
              <a:t>$safeTitle</a:t>
            </a:r>
          </a:p>
        </p:txBody>
      </p:sp>
      <!-- Body Text Shape -->
      <p:sp>
        <p:nvSpPr><p:cNvPr id="3" name="Content"/><p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr><p:nvPr/></p:nvSpPr>
        <p:spPr>
          <a:xfrm><a:off x="685800" y="1300000"/><a:ext cx="$bodyWidth" cy="3400000"/></a:xfrm>
          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
        </p:spPr>
        <p:txBody>
          <a:bodyPr vert="horz" lIns="0" tIns="0" rIns="0" bIns="0" anchor="t"/>
          <a:lstStyle/>
$bulletParagraphs
        </p:txBody>
      </p:sp>
$imageShapeXml
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>"""
    }

    private fun generateSlideRelsXml(imageFilename: String?): String {
        val imgRel = if (imageFilename != null) {
            """  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/$imageFilename"/>"""
        } else ""

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
$imgRel
</Relationships>"""
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        writeEntry(zip, name, content.toByteArray(Charsets.UTF_8))
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, bytes: ByteArray) {
        val entry = ZipEntry(name)
        zip.putNextEntry(entry)
        zip.write(bytes)
        zip.closeEntry()
    }
}

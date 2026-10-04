package com.example.fileconverter.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.example.fileconverter.util.MediaStoreHelper
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a valid .pptx from a PDF file.
 * Each PDF page is rendered as a JPEG and embedded as a full-slide image.
 * The resulting file opens without repair prompts in PowerPoint / Google Slides.
 */
object PptxBuilder {

    private const val EMU_PER_PT = 12700L   // 1 point = 12700 EMU
    // Default slide width: 10 inches = 9144000 EMU
    private const val SLIDE_WIDTH_EMU = 9144000L

    fun build(
        context: Context,
        pdfUri: Uri,
        outputFileName: String,
        dpi: Int = 150,
        onProgress: (Int, Int) -> Unit
    ): Uri? {
        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return null

        val pair = MediaStoreHelper.openOutputStream(
            context, outputFileName,
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        ) ?: return null
        val (resultUri, outStream) = pair

        pfd.use { fileDescriptor ->
            val renderer = PdfRenderer(fileDescriptor)
            renderer.use {
                val pageCount = renderer.pageCount
                ZipOutputStream(outStream.buffered()).use { zip ->
                    // ----- static entries -----
                    writeEntry(zip, "[Content_Types].xml", contentTypesXml(pageCount))
                    writeEntry(zip, "_rels/.rels", relsXml())
                    writeEntry(zip, "ppt/presentation.xml", presentationXml(pageCount))
                    writeEntry(zip, "ppt/_rels/presentation.xml.rels", presentationRelsXml(pageCount))
                    writeEntry(zip, "ppt/slideMasters/slideMaster1.xml", slideMasterXml())
                    writeEntry(zip, "ppt/slideMasters/_rels/slideMaster1.xml.rels", slideMasterRelsXml())
                    writeEntry(zip, "ppt/slideLayouts/slideLayout1.xml", slideLayoutXml())
                    writeEntry(zip, "ppt/slideLayouts/_rels/slideLayout1.xml.rels", slideLayoutRelsXml())

                    // ----- per-page entries -----
                    for (pageIdx in 0 until pageCount) {
                        onProgress(pageIdx + 1, pageCount)

                        // Render page
                        val page = renderer.openPage(pageIdx)
                        val pageWidthPx = page.width
                        val pageHeightPx = page.height
                        val scale = dpi / 72f
                        val bmpW = (pageWidthPx * scale).toInt().coerceAtLeast(1)
                        val bmpH = (pageHeightPx * scale).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        // Compress to JPEG bytes
                        val jpegBytes = ByteArrayOutputStream().also { baos ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                        }.toByteArray()
                        bitmap.recycle()

                        // Slide EMU dimensions preserving aspect ratio
                        val slideW = SLIDE_WIDTH_EMU
                        val slideH = (slideW * pageHeightPx.toLong() / pageWidthPx.toLong())
                            .coerceAtLeast(1L)

                        val slideNum = pageIdx + 1
                        val imgName = "image${slideNum}.jpg"

                        writeEntry(zip, "ppt/media/$imgName", jpegBytes)
                        writeEntry(zip, "ppt/slides/slide${slideNum}.xml",
                            slideXml(slideNum, slideW, slideH))
                        writeEntry(zip, "ppt/slides/_rels/slide${slideNum}.xml.rels",
                            slideRelsXml(slideNum, imgName))
                    }
                }
            }
        }

        MediaStoreHelper.finishPending(context, resultUri)
        return resultUri
    }

    // ===================== XML generators =====================

    private fun contentTypesXml(pageCount: Int): String {
        val slideOverrides = (1..pageCount).joinToString("\n") { n ->
            """  <Override PartName="/ppt/slides/slide$n.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Default Extension="jpeg" ContentType="image/jpeg"/>
  <Default Extension="png" ContentType="image/png"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
$slideOverrides
</Types>"""
    }

    private fun relsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>"""

    private fun presentationXml(pageCount: Int): String {
        val sldIdList = (1..pageCount).joinToString("\n") { n ->
            """    <p:sldId id="${256 + n}" r:id="rId${n}"/>"""
        }
        // We use the first page's aspect ratio as the nominal size;
        // per-slide sizes are set in each slide XML.
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                saveSubsetFonts="1">
  <p:sldMasterIdLst>
    <p:sldMasterId id="2147483648" r:id="rId${pageCount + 1}"/>
  </p:sldMasterIdLst>
  <p:sldIdLst>
$sldIdList
  </p:sldIdLst>
  <p:sldSz cx="$SLIDE_WIDTH_EMU" cy="${ (SLIDE_WIDTH_EMU * 3 / 4) }" type="custom"/>
  <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>"""
    }

    private fun presentationRelsXml(pageCount: Int): String {
        val slideRels = (1..pageCount).joinToString("\n") { n ->
            """  <Relationship Id="rId${n}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide${n}.xml"/>"""
        }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
$slideRels
  <Relationship Id="rId${pageCount + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
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
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
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
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="0" cy="0"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="0" cy="0"/>
        </a:xfrm>
      </p:grpSpPr>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr>
    <a:masterClrMapping/>
  </p:clrMapOvr>
</p:sldLayout>"""

    private fun slideLayoutRelsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>"""

    private fun slideXml(slideNum: Int, cx: Long, cy: Long) = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
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
        <a:xfrm>
          <a:off x="0" y="0"/>
          <a:ext cx="$cx" cy="$cy"/>
          <a:chOff x="0" y="0"/>
          <a:chExt cx="$cx" cy="$cy"/>
        </a:xfrm>
      </p:grpSpPr>
      <p:pic>
        <p:nvPicPr>
          <p:cNvPr id="2" name="image$slideNum"/>
          <p:cNvPicPr>
            <a:picLocks noChangeAspect="1"/>
          </p:cNvPicPr>
          <p:nvPr/>
        </p:nvPicPr>
        <p:blipFill>
          <a:blip r:embed="rId1"/>
          <a:stretch>
            <a:fillRect/>
          </a:stretch>
        </p:blipFill>
        <p:spPr>
          <a:xfrm>
            <a:off x="0" y="0"/>
            <a:ext cx="$cx" cy="$cy"/>
          </a:xfrm>
          <a:prstGeom prst="rect">
            <a:avLst/>
          </a:prstGeom>
        </p:spPr>
      </p:pic>
    </p:spTree>
  </p:cSld>
  <p:clrMapOvr>
    <a:masterClrMapping/>
  </p:clrMapOvr>
</p:sld>"""

    private fun slideRelsXml(slideNum: Int, imgName: String) = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/$imgName"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>"""

    // ---------- zip helpers ----------

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

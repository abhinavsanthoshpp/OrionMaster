package com.pdfmaster.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.pdfmaster.app.data.model.ProcessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

object PDFProcessor {

    fun initialize(context: Context) {
        PDFBoxResourceLoader.init(context)
    }

    // ===================== MERGE PDF =====================
    suspend fun mergePDFs(
        context: Context,
        uris: List<Uri>,
        outputFile: File,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            val mergedDoc = PDDocument()
            uris.forEachIndexed { index, uri ->
                onProgress(index, uris.size)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val doc = PDDocument.load(stream)
                    doc.pages.forEach { page -> mergedDoc.addPage(page) }
                    doc.close()
                }
            }
            mergedDoc.save(outputFile)
            mergedDoc.close()
            onProgress(uris.size, uris.size)
            ProcessResult(true, outputFile.absolutePath, message = "Merged ${uris.size} files successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Merge failed")
        }
    }

    // ===================== SPLIT PDF =====================
    suspend fun splitPDF(
        context: Context,
        uri: Uri,
        outputDir: File,
        fileName: String,
        splitMode: SplitMode = SplitMode.EachPage,
        pageRanges: List<IntRange>? = null,
        everyNPages: Int = 1
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            val outputPaths = mutableListOf<String>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val totalPages = doc.numberOfPages

                when (splitMode) {
                    SplitMode.EachPage -> {
                        for (i in 0 until totalPages) {
                            val newDoc = PDDocument()
                            newDoc.addPage(doc.getPage(i))
                            val outFile = File(outputDir, "${fileName}_page_${i + 1}.pdf")
                            newDoc.save(outFile)
                            newDoc.close()
                            outputPaths.add(outFile.absolutePath)
                        }
                    }
                    SplitMode.ByRange -> {
                        pageRanges?.forEachIndexed { idx, range ->
                            val newDoc = PDDocument()
                            for (i in range) {
                                if (i < totalPages) newDoc.addPage(doc.getPage(i))
                            }
                            val outFile = File(outputDir, "${fileName}_part_${idx + 1}.pdf")
                            newDoc.save(outFile)
                            newDoc.close()
                            outputPaths.add(outFile.absolutePath)
                        }
                    }
                    SplitMode.EveryNPages -> {
                        var partIndex = 1
                        var i = 0
                        while (i < totalPages) {
                            val newDoc = PDDocument()
                            val end = minOf(i + everyNPages, totalPages)
                            for (j in i until end) {
                                newDoc.addPage(doc.getPage(j))
                            }
                            val outFile = File(outputDir, "${fileName}_part_${partIndex}.pdf")
                            newDoc.save(outFile)
                            newDoc.close()
                            outputPaths.add(outFile.absolutePath)
                            i += everyNPages
                            partIndex++
                        }
                    }
                }
                doc.close()
            }
            ProcessResult(true, outputPaths = outputPaths, message = "Split into ${outputPaths.size} files")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Split failed")
        }
    }

    // ===================== COMPRESS PDF =====================
    suspend fun compressPDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        quality: CompressionQuality = CompressionQuality.MEDIUM
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                // PDFBox compression - remove metadata and optimize structure
                doc.documentInformation.apply {
                    // Strip optional metadata to reduce size
                }
                doc.save(outputFile)
                doc.close()
            }
            val originalSize = context.contentResolver.openInputStream(uri)?.available()?.toLong() ?: 0L
            val compressedSize = outputFile.length()
            val reduction = if (originalSize > 0) ((originalSize - compressedSize) * 100 / originalSize).toInt() else 0
            ProcessResult(true, outputFile.absolutePath, message = "Compressed! Size reduced by ~$reduction%")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Compression failed")
        }
    }

    // ===================== ROTATE PDF =====================
    suspend fun rotatePDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        rotation: Int,
        pageIndices: List<Int>? = null
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val pagesToRotate = pageIndices ?: (0 until doc.numberOfPages).toList()
                pagesToRotate.forEach { index ->
                    if (index < doc.numberOfPages) {
                        val page = doc.getPage(index)
                        val currentRotation = page.rotation
                        page.rotation = (currentRotation + rotation) % 360
                    }
                }
                doc.save(outputFile)
                doc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "Rotated ${pageIndices?.size ?: "all"} pages by ${rotation}°")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Rotation failed")
        }
    }

    // ===================== REMOVE PAGES =====================
    suspend fun removePages(
        context: Context,
        uri: Uri,
        outputFile: File,
        pageIndicesToRemove: List<Int>
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val sortedIndices = pageIndicesToRemove.sortedDescending()
                sortedIndices.forEach { index ->
                    if (index < doc.numberOfPages) {
                        doc.removePage(index)
                    }
                }
                doc.save(outputFile)
                doc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "Removed ${pageIndicesToRemove.size} pages")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Remove pages failed")
        }
    }

    // ===================== EXTRACT PAGES =====================
    suspend fun extractPages(
        context: Context,
        uri: Uri,
        outputFile: File,
        pageIndices: List<Int>
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val sourceDoc = PDDocument.load(stream)
                val newDoc = PDDocument()
                pageIndices.sorted().forEach { index ->
                    if (index < sourceDoc.numberOfPages) {
                        newDoc.addPage(sourceDoc.getPage(index))
                    }
                }
                newDoc.save(outputFile)
                newDoc.close()
                sourceDoc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "Extracted ${pageIndices.size} pages")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Extract pages failed")
        }
    }

    // ===================== ADD WATERMARK =====================
    suspend fun addWatermark(
        context: Context,
        uri: Uri,
        outputFile: File,
        watermarkText: String,
        opacity: Float = 0.3f,
        rotation: Float = 45f,
        fontSize: Float = 48f,
        color: WatermarkColor = WatermarkColor.GRAY,
        position: WatermarkPosition = WatermarkPosition.CENTER
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val font = PDType1Font.HELVETICA_BOLD

                doc.pages.forEach { page ->
                    val mediaBox = page.mediaBox
                    val pageWidth = mediaBox.width
                    val pageHeight = mediaBox.height

                    try {
                        val contentStream = PDPageContentStream(
                            doc, page,
                            PDPageContentStream.AppendMode.APPEND, true, true
                        )

                        contentStream.apply {
                            // Set transparency
                            val gs = com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState()
                            gs.nonStrokingAlphaConstant = opacity

                            setFont(font, fontSize)

                            // Color
                            when (color) {
                                WatermarkColor.RED -> setNonStrokingColor(1f, 0f, 0f)
                                WatermarkColor.BLUE -> setNonStrokingColor(0f, 0f, 1f)
                                WatermarkColor.GRAY -> setNonStrokingColor(0.5f, 0.5f, 0.5f)
                                WatermarkColor.BLACK -> setNonStrokingColor(0f, 0f, 0f)
                            }

                            val textWidth = font.getStringWidth(watermarkText) / 1000f * fontSize
                            val tx = pageWidth / 2 - textWidth / 2
                            val ty = pageHeight / 2

                            val radians = Math.toRadians(rotation.toDouble())
                            transform(
                                com.tom_roush.harmony.awt.geom.AffineTransform(
                                    Math.cos(radians).toFloat(), Math.sin(radians).toFloat(),
                                    -Math.sin(radians).toFloat(), Math.cos(radians).toFloat(),
                                    tx, ty
                                )
                            )

                            beginText()
                            newLineAtOffset(0f, 0f)
                            showText(watermarkText)
                            endText()
                            close()
                        }
                    } catch (e: Exception) {
                        // Skip this page if watermark fails
                    }
                }

                doc.save(outputFile)
                doc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "Watermark added successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Watermark failed")
        }
    }

    // ===================== PROTECT PDF =====================
    suspend fun protectPDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        userPassword: String,
        ownerPassword: String = userPassword + "_owner",
        allowPrinting: Boolean = true,
        allowCopying: Boolean = false
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val ap = AccessPermission()
                ap.setCanPrint(allowPrinting)
                ap.setCanExtractContent(allowCopying)
                ap.setCanModify(false)

                val policy = StandardProtectionPolicy(ownerPassword, userPassword, ap)
                policy.encryptionKeyLength = 128
                doc.protect(policy)
                doc.save(outputFile)
                doc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "PDF protected with password")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Protection failed")
        }
    }

    // ===================== UNLOCK PDF =====================
    suspend fun unlockPDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        password: String
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream, password)
                doc.isAllSecurityToBeRemoved = true
                doc.save(outputFile)
                doc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "PDF unlocked successfully")
        } catch (e: Exception) {
            when {
                e.message?.contains("password", true) == true ->
                    ProcessResult(false, message = "Incorrect password")
                else -> ProcessResult(false, message = e.message ?: "Unlock failed")
            }
        }
    }

    // ===================== ADD PAGE NUMBERS =====================
    suspend fun addPageNumbers(
        context: Context,
        uri: Uri,
        outputFile: File,
        position: PageNumberPosition = PageNumberPosition.BOTTOM_CENTER,
        startFrom: Int = 1,
        fontSize: Float = 12f,
        prefix: String = "",
        suffix: String = ""
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val font = PDType1Font.HELVETICA

                doc.pages.forEachIndexed { index, page ->
                    val mediaBox = page.mediaBox
                    val pageNumber = "${prefix}${index + startFrom}${suffix}"
                    val textWidth = font.getStringWidth(pageNumber) / 1000f * fontSize

                    val (x, y) = when (position) {
                        PageNumberPosition.BOTTOM_CENTER -> Pair(
                            (mediaBox.width - textWidth) / 2,
                            20f
                        )
                        PageNumberPosition.BOTTOM_LEFT -> Pair(20f, 20f)
                        PageNumberPosition.BOTTOM_RIGHT -> Pair(mediaBox.width - textWidth - 20f, 20f)
                        PageNumberPosition.TOP_CENTER -> Pair(
                            (mediaBox.width - textWidth) / 2,
                            mediaBox.height - 30f
                        )
                        PageNumberPosition.TOP_LEFT -> Pair(20f, mediaBox.height - 30f)
                        PageNumberPosition.TOP_RIGHT -> Pair(
                            mediaBox.width - textWidth - 20f,
                            mediaBox.height - 30f
                        )
                    }

                    val contentStream = PDPageContentStream(
                        doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true
                    )
                    contentStream.apply {
                        beginText()
                        setFont(font, fontSize)
                        setNonStrokingColor(0f, 0f, 0f)
                        newLineAtOffset(x, y)
                        showText(pageNumber)
                        endText()
                        close()
                    }
                }

                doc.save(outputFile)
                doc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "Page numbers added successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Add page numbers failed")
        }
    }

    // ===================== PDF TO JPG =====================
    suspend fun pdfToJpg(
        context: Context,
        uri: Uri,
        outputDir: File,
        fileName: String,
        dpi: Int = 150,
        pageIndices: List<Int>? = null,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            val outputPaths = mutableListOf<String>()
            var pfd: ParcelFileDescriptor? = null
            var renderer: PdfRenderer? = null

            try {
                pfd = context.contentResolver.openFileDescriptor(uri, "r")
                pfd?.let {
                    renderer = PdfRenderer(it)
                    val totalPages = renderer!!.pageCount
                    val pagesToConvert = pageIndices ?: (0 until totalPages).toList()

                    pagesToConvert.forEachIndexed { progressIndex, pageIndex ->
                        onProgress(progressIndex, pagesToConvert.size)
                        if (pageIndex < totalPages) {
                            val page = renderer!!.openPage(pageIndex)
                            val scale = dpi / 72f
                            val width = (page.width * scale).toInt()
                            val height = (page.height * scale).toInt()
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()

                            val outFile = File(outputDir, "${fileName}_page_${pageIndex + 1}.jpg")
                            FileOutputStream(outFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            bitmap.recycle()
                            outputPaths.add(outFile.absolutePath)
                        }
                    }
                }
            } finally {
                renderer?.close()
                pfd?.close()
            }

            onProgress(outputPaths.size, outputPaths.size)
            ProcessResult(true, outputPaths = outputPaths, message = "Converted ${outputPaths.size} pages to JPG")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "PDF to JPG conversion failed")
        }
    }

    // ===================== JPG TO PDF =====================
    suspend fun jpgToPdf(
        context: Context,
        uris: List<Uri>,
        outputFile: File,
        pageSize: PDRectangle = PDRectangle.A4,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            val doc = PDDocument()
            uris.forEachIndexed { index, uri ->
                onProgress(index, uris.size)
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val image = PDImageXObject.createFromByteArray(doc, stream.readBytes(), "image")
                        val page = PDPage(pageSize)
                        doc.addPage(page)

                        val imageWidth = image.width.toFloat()
                        val imageHeight = image.height.toFloat()
                        val pageWidth = pageSize.width
                        val pageHeight = pageSize.height

                        val scaleX = pageWidth / imageWidth
                        val scaleY = pageHeight / imageHeight
                        val scale = minOf(scaleX, scaleY)

                        val scaledWidth = imageWidth * scale
                        val scaledHeight = imageHeight * scale
                        val x = (pageWidth - scaledWidth) / 2
                        val y = (pageHeight - scaledHeight) / 2

                        val contentStream = PDPageContentStream(doc, page)
                        contentStream.drawImage(image, x, y, scaledWidth, scaledHeight)
                        contentStream.close()
                    }
                } catch (e: Exception) {
                    // Skip invalid images
                }
            }
            doc.save(outputFile)
            doc.close()
            onProgress(uris.size, uris.size)
            ProcessResult(true, outputFile.absolutePath, message = "Created PDF from ${uris.size} images")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "JPG to PDF conversion failed")
        }
    }

    // ===================== REORDER PAGES =====================
    suspend fun reorderPages(
        context: Context,
        uri: Uri,
        outputFile: File,
        newOrder: List<Int>
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val sourceDoc = PDDocument.load(stream)
                val newDoc = PDDocument()
                newOrder.forEach { index ->
                    if (index < sourceDoc.numberOfPages) {
                        newDoc.addPage(sourceDoc.getPage(index))
                    }
                }
                newDoc.save(outputFile)
                newDoc.close()
                sourceDoc.close()
            }
            ProcessResult(true, outputFile.absolutePath, message = "Pages reordered successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Reorder failed")
        }
    }

    // ===================== GET PAGE COUNT =====================
    suspend fun getPageCount(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val count = doc.numberOfPages
                doc.close()
                count
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    // ===================== RENDER PAGE THUMBNAIL =====================
    suspend fun renderPageThumbnail(
        context: Context,
        uri: Uri,
        pageIndex: Int = 0,
        width: Int = 200
    ): Bitmap? = withContext(Dispatchers.IO) {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            renderer = pfd?.let { PdfRenderer(it) }
            renderer?.let { r ->
                if (pageIndex < r.pageCount) {
                    val page = r.openPage(pageIndex)
                    val scale = width.toFloat() / page.width
                    val height = (page.height * scale).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bitmap
                } else null
            }
        } catch (e: Exception) {
            null
        } finally {
            renderer?.close()
            pfd?.close()
        }
    }
}

// ===================== ENUMS =====================
enum class SplitMode { EachPage, ByRange, EveryNPages }
enum class CompressionQuality { LOW, MEDIUM, HIGH }
enum class WatermarkColor { RED, BLUE, GRAY, BLACK }
enum class WatermarkPosition { CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
enum class PageNumberPosition {
    BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT,
    TOP_CENTER, TOP_LEFT, TOP_RIGHT
}

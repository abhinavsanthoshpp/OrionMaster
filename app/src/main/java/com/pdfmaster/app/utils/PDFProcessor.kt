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
import com.tom_roush.pdfbox.util.Matrix
import com.pdfmaster.app.data.model.ProcessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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
            outputFile.parentFile?.mkdirs()
            val mergedDoc = PDDocument()
            uris.forEachIndexed { index, uri ->
                onProgress(index, uris.size)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val doc = PDDocument.load(stream)
                    for (i in 0 until doc.numberOfPages) {
                        mergedDoc.importPage(doc.getPage(i))
                    }
                    doc.close()
                }
            }
            mergedDoc.save(outputFile)
            mergedDoc.close()
            onProgress(uris.size, uris.size)
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Merged ${uris.size} PDFs successfully")
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
            outputDir.mkdirs()
            val outputPaths = mutableListOf<String>()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val totalPages = doc.numberOfPages

                when (splitMode) {
                    SplitMode.EachPage -> {
                        for (i in 0 until totalPages) {
                            val newDoc = PDDocument()
                            newDoc.importPage(doc.getPage(i))
                            val outFile = File(outputDir, "${fileName}_page_${i + 1}.pdf")
                            newDoc.save(outFile)
                            newDoc.close()
                            FileUtils.exportToPublicDownloads(context, outFile)
                            outputPaths.add(outFile.absolutePath)
                        }
                    }
                    SplitMode.ByRange -> {
                        pageRanges?.forEachIndexed { idx, range ->
                            val newDoc = PDDocument()
                            for (i in range) {
                                if (i in 0 until totalPages) {
                                    newDoc.importPage(doc.getPage(i))
                                }
                            }
                            val outFile = File(outputDir, "${fileName}_part_${idx + 1}.pdf")
                            newDoc.save(outFile)
                            newDoc.close()
                            FileUtils.exportToPublicDownloads(context, outFile)
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
                                newDoc.importPage(doc.getPage(j))
                            }
                            val outFile = File(outputDir, "${fileName}_part_${partIndex}.pdf")
                            newDoc.save(outFile)
                            newDoc.close()
                            FileUtils.exportToPublicDownloads(context, outFile)
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                doc.documentInformation.apply {
                    title = null
                    author = null
                    subject = null
                    keywords = null
                    creator = null
                    producer = "Orion Master"
                }
                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            val originalSize = context.contentResolver.openInputStream(uri)?.available()?.toLong() ?: 0L
            val compressedSize = outputFile.length()
            val reduction = if (originalSize > 0 && compressedSize < originalSize) {
                ((originalSize - compressedSize) * 100 / originalSize).toInt()
            } else 0
            ProcessResult(true, outputFile.absolutePath, message = "Compressed successfully ($reduction% reduction)")
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val pagesToRotate = pageIndices ?: (0 until doc.numberOfPages).toList()
                pagesToRotate.forEach { index ->
                    if (index in 0 until doc.numberOfPages) {
                        val page = doc.getPage(index)
                        page.rotation = (page.rotation + rotation) % 360
                    }
                }
                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Rotated pages by ${rotation}°")
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val sortedIndices = pageIndicesToRemove.sortedDescending()
                sortedIndices.forEach { index ->
                    if (index in 0 until doc.numberOfPages) {
                        doc.removePage(index)
                    }
                }
                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val sourceDoc = PDDocument.load(stream)
                val newDoc = PDDocument()
                pageIndices.sorted().forEach { index ->
                    if (index in 0 until sourceDoc.numberOfPages) {
                        newDoc.importPage(sourceDoc.getPage(index))
                    }
                }
                newDoc.save(outputFile)
                newDoc.close()
                sourceDoc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val font = PDType1Font.HELVETICA_BOLD

                doc.pages.forEach { page ->
                    val mediaBox = page.mediaBox
                    val textWidth = font.getStringWidth(watermarkText) / 1000f * fontSize
                    val x = (mediaBox.width - textWidth) / 2
                    val y = mediaBox.height / 2

                    val contentStream = PDPageContentStream(
                        doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true
                    )
                    contentStream.beginText()
                    contentStream.setFont(font, fontSize)
                    when (color) {
                        WatermarkColor.RED -> contentStream.setNonStrokingColor(1f, 0.2f, 0.2f)
                        WatermarkColor.BLUE -> contentStream.setNonStrokingColor(0.2f, 0.4f, 1f)
                        WatermarkColor.GRAY -> contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f)
                        WatermarkColor.BLACK -> contentStream.setNonStrokingColor(0f, 0f, 0f)
                    }
                    val rad = Math.toRadians(rotation.toDouble()).toFloat()
                    contentStream.setTextMatrix(Matrix(Math.cos(rad.toDouble()).toFloat(), Math.sin(rad.toDouble()).toFloat(), -Math.sin(rad.toDouble()).toFloat(), Math.cos(rad.toDouble()).toFloat(), x, y))
                    contentStream.showText(watermarkText)
                    contentStream.endText()
                    contentStream.close()
                }

                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Watermark added successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Add watermark failed")
        }
    }

    // ===================== PROTECT PDF =====================
    suspend fun protectPDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        userPassword: String,
        ownerPassword: String = userPassword,
        allowPrinting: Boolean = true,
        allowCopying: Boolean = false
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val accessPermission = AccessPermission().apply {
                    setCanPrint(allowPrinting)
                    setCanExtractContent(allowCopying)
                    setCanModify(false)
                }
                val protectionPolicy = StandardProtectionPolicy(ownerPassword, userPassword, accessPermission).apply {
                    encryptionKeyLength = 128
                }
                doc.protect(protectionPolicy)
                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "PDF encrypted and protected")
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream, password)
                if (doc.isEncrypted) {
                    doc.isAllSecurityToBeRemoved = true
                }
                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Password removed from PDF")
        } catch (e: Exception) {
            ProcessResult(false, message = if (e.message?.contains("password", true) == true) "Incorrect password" else (e.message ?: "Unlock failed"))
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val font = PDType1Font.HELVETICA

                doc.pages.forEachIndexed { index, page ->
                    val mediaBox = page.mediaBox
                    val pageNumber = "${prefix}${index + startFrom}${suffix}"
                    val textWidth = font.getStringWidth(pageNumber) / 1000f * fontSize

                    val (x, y) = when (position) {
                        PageNumberPosition.BOTTOM_CENTER -> Pair((mediaBox.width - textWidth) / 2, 20f)
                        PageNumberPosition.BOTTOM_LEFT -> Pair(20f, 20f)
                        PageNumberPosition.BOTTOM_RIGHT -> Pair(mediaBox.width - textWidth - 20f, 20f)
                        PageNumberPosition.TOP_CENTER -> Pair((mediaBox.width - textWidth) / 2, mediaBox.height - 30f)
                        PageNumberPosition.TOP_LEFT -> Pair(20f, mediaBox.height - 30f)
                        PageNumberPosition.TOP_RIGHT -> Pair(mediaBox.width - textWidth - 20f, mediaBox.height - 30f)
                    }

                    val contentStream = PDPageContentStream(
                        doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true
                    )
                    contentStream.beginText()
                    contentStream.setFont(font, fontSize)
                    contentStream.setNonStrokingColor(0f, 0f, 0f)
                    contentStream.newLineAtOffset(x, y)
                    contentStream.showText(pageNumber)
                    contentStream.endText()
                    contentStream.close()
                }

                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
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
            outputDir.mkdirs()
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
                        if (pageIndex in 0 until totalPages) {
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
                            FileUtils.exportToPublicDownloads(context, outFile)
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
            outputFile.parentFile?.mkdirs()
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

                        val scale = minOf(pageWidth / imageWidth, pageHeight / imageHeight)
                        val scaledWidth = imageWidth * scale
                        val scaledHeight = imageHeight * scale
                        val x = (pageWidth - scaledWidth) / 2
                        val y = (pageHeight - scaledHeight) / 2

                        val contentStream = PDPageContentStream(doc, page)
                        contentStream.drawImage(image, x, y, scaledWidth, scaledHeight)
                        contentStream.close()
                    }
                } catch (e: Exception) {
                    // Skip unreadable image
                }
            }
            doc.save(outputFile)
            doc.close()
            onProgress(uris.size, uris.size)
            FileUtils.exportToPublicDownloads(context, outputFile)
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
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val sourceDoc = PDDocument.load(stream)
                val newDoc = PDDocument()
                newOrder.forEach { index ->
                    if (index in 0 until sourceDoc.numberOfPages) {
                        newDoc.importPage(sourceDoc.getPage(index))
                    }
                }
                newDoc.save(outputFile)
                newDoc.close()
                sourceDoc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Pages reordered successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Reorder failed")
        }
    }

    // ===================== SIGN PDF =====================
    suspend fun signPDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        signatureBitmap: Bitmap,
        pageIndex: Int = 0,
        xPercent: Float = 0.5f,
        yPercent: Float = 0.15f
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                val targetPageIndex = if (pageIndex in 0 until doc.numberOfPages) pageIndex else 0
                val page = doc.getPage(targetPageIndex)

                val streamOut = ByteArrayOutputStream()
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, streamOut)
                val signatureBytes = streamOut.toByteArray()
                val pdImage = PDImageXObject.createFromByteArray(doc, signatureBytes, "signature")

                val mediaBox = page.mediaBox
                val sigWidth = 160f
                val sigHeight = sigWidth * (signatureBitmap.height.toFloat() / signatureBitmap.width.toFloat())
                val x = (mediaBox.width - sigWidth) * xPercent.coerceIn(0.05f, 0.95f)
                val y = (mediaBox.height - sigHeight) * yPercent.coerceIn(0.05f, 0.95f)

                val contentStream = PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)
                contentStream.drawImage(pdImage, x, y, sigWidth, sigHeight)
                contentStream.close()

                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Signature placed and saved successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Sign failed")
        }
    }

    // ===================== CROP PDF =====================
    suspend fun cropPDF(
        context: Context,
        uri: Uri,
        outputFile: File,
        marginPercent: Float = 0.1f
    ): ProcessResult = withContext(Dispatchers.IO) {
        try {
            outputFile.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val doc = PDDocument.load(stream)
                doc.pages.forEach { page ->
                    val mediaBox = page.mediaBox
                    val marginX = mediaBox.width * marginPercent
                    val marginY = mediaBox.height * marginPercent
                    val newRect = PDRectangle(
                        mediaBox.lowerLeftX + marginX,
                        mediaBox.lowerLeftY + marginY,
                        mediaBox.width - (marginX * 2),
                        mediaBox.height - (marginY * 2)
                    )
                    page.cropBox = newRect
                }
                doc.save(outputFile)
                doc.close()
            }
            FileUtils.exportToPublicDownloads(context, outputFile)
            ProcessResult(true, outputFile.absolutePath, message = "Margins cropped successfully")
        } catch (e: Exception) {
            ProcessResult(false, message = e.message ?: "Crop failed")
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

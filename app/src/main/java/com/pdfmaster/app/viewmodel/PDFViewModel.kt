package com.pdfmaster.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdfmaster.app.data.local.AppDatabase
import com.pdfmaster.app.data.local.RecentFileEntity
import com.pdfmaster.app.data.model.ProcessResult
import com.pdfmaster.app.utils.FileUtils
import com.pdfmaster.app.utils.PDFProcessor
import com.pdfmaster.app.utils.CompressionQuality
import com.pdfmaster.app.utils.PageNumberPosition
import com.pdfmaster.app.utils.SplitMode
import com.pdfmaster.app.utils.WatermarkColor
import com.pdfmaster.app.utils.WatermarkPosition
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

// ===================== UI STATE =====================
data class ToolUiState(
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val totalSteps: Int = 0,
    val result: ProcessResult? = null,
    val error: String? = null,
    val selectedUris: List<Uri> = emptyList(),
    val selectedUri: Uri? = null,
    val statusMessage: String = ""
)

// ===================== MAIN VIEWMODEL =====================
class PDFViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.recentFileDao()

    private val _uiState = MutableStateFlow(ToolUiState())
    val uiState: StateFlow<ToolUiState> = _uiState.asStateFlow()

    val recentFiles = dao.getAllRecentFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearResult() {
        _uiState.update { it.copy(result = null, error = null) }
    }

    fun setSelectedUri(uri: Uri?) {
        _uiState.update { it.copy(selectedUri = uri, result = null, error = null) }
    }

    fun setSelectedUris(uris: List<Uri>) {
        _uiState.update { it.copy(selectedUris = uris, result = null, error = null) }
    }

    private fun setLoading(loading: Boolean, status: String = "") {
        _uiState.update { it.copy(isLoading = loading, statusMessage = status) }
    }

    private fun setResult(result: ProcessResult) {
        _uiState.update { it.copy(isLoading = false, result = result, statusMessage = "") }
        if (result.success) {
            result.outputPath?.let { saveToRecent(it) }
        }
    }

    private fun setError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message, statusMessage = "") }
    }

    private fun saveToRecent(filePath: String, toolUsed: String? = null) {
        viewModelScope.launch {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    val entity = RecentFileEntity(
                        name = file.nameWithoutExtension,
                        path = filePath,
                        sizeBytes = file.length(),
                        pageCount = 0,
                        lastOpened = System.currentTimeMillis(),
                        toolUsed = toolUsed
                    )
                    dao.insert(entity)
                }
            } catch (e: Exception) { /* ignore */ }
        }
    }

    // ===================== MERGE =====================
    fun mergePDFs(uris: List<Uri>) {
        viewModelScope.launch {
            setLoading(true, "Merging PDFs…")
            val outputDir = FileUtils.getOutputDir(context)
            val outputFile = FileUtils.createOutputFile(outputDir, "merged")
            val result = PDFProcessor.mergePDFs(context, uris, outputFile) { done, total ->
                _uiState.update { it.copy(progress = done, totalSteps = total) }
            }
            setResult(result)
        }
    }

    // ===================== SPLIT =====================
    fun splitPDF(
        uri: Uri,
        mode: SplitMode = SplitMode.EachPage,
        everyNPages: Int = 1,
        pageRanges: List<IntRange>? = null
    ) {
        viewModelScope.launch {
            setLoading(true, "Splitting PDF…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val result = PDFProcessor.splitPDF(context, uri, outputDir, fileName, mode, pageRanges, everyNPages)
            setResult(result)
        }
    }

    // ===================== COMPRESS =====================
    fun compressPDF(uri: Uri, quality: CompressionQuality = CompressionQuality.MEDIUM) {
        viewModelScope.launch {
            setLoading(true, "Compressing PDF…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_compressed")
            val result = PDFProcessor.compressPDF(context, uri, outputFile, quality)
            setResult(result)
        }
    }

    // ===================== ROTATE =====================
    fun rotatePDF(uri: Uri, rotation: Int, pageIndices: List<Int>? = null) {
        viewModelScope.launch {
            setLoading(true, "Rotating PDF…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_rotated")
            val result = PDFProcessor.rotatePDF(context, uri, outputFile, rotation, pageIndices)
            setResult(result)
        }
    }

    // ===================== REMOVE PAGES =====================
    fun removePages(uri: Uri, pageIndices: List<Int>) {
        viewModelScope.launch {
            setLoading(true, "Removing pages…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_modified")
            val result = PDFProcessor.removePages(context, uri, outputFile, pageIndices)
            setResult(result)
        }
    }

    // ===================== EXTRACT PAGES =====================
    fun extractPages(uri: Uri, pageIndices: List<Int>) {
        viewModelScope.launch {
            setLoading(true, "Extracting pages…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_extracted")
            val result = PDFProcessor.extractPages(context, uri, outputFile, pageIndices)
            setResult(result)
        }
    }

    // ===================== WATERMARK =====================
    fun addWatermark(
        uri: Uri,
        text: String,
        opacity: Float = 0.3f,
        rotation: Float = 45f,
        fontSize: Float = 48f,
        color: WatermarkColor = WatermarkColor.GRAY
    ) {
        viewModelScope.launch {
            setLoading(true, "Adding watermark…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_watermarked")
            val result = PDFProcessor.addWatermark(context, uri, outputFile, text, opacity, rotation, fontSize, color)
            setResult(result)
        }
    }

    // ===================== PROTECT =====================
    fun protectPDF(uri: Uri, password: String, allowPrinting: Boolean, allowCopying: Boolean) {
        viewModelScope.launch {
            setLoading(true, "Protecting PDF…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_protected")
            val result = PDFProcessor.protectPDF(context, uri, outputFile, password, allowPrinting = allowPrinting, allowCopying = allowCopying)
            setResult(result)
        }
    }

    // ===================== UNLOCK =====================
    fun unlockPDF(uri: Uri, password: String) {
        viewModelScope.launch {
            setLoading(true, "Unlocking PDF…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_unlocked")
            val result = PDFProcessor.unlockPDF(context, uri, outputFile, password)
            setResult(result)
        }
    }

    // ===================== PAGE NUMBERS =====================
    fun addPageNumbers(
        uri: Uri,
        position: PageNumberPosition = PageNumberPosition.BOTTOM_CENTER,
        startFrom: Int = 1,
        prefix: String = "",
        suffix: String = ""
    ) {
        viewModelScope.launch {
            setLoading(true, "Adding page numbers…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_numbered")
            val result = PDFProcessor.addPageNumbers(context, uri, outputFile, position, startFrom, 12f, prefix, suffix)
            setResult(result)
        }
    }

    // ===================== PDF TO JPG =====================
    fun pdfToJpg(uri: Uri, dpi: Int = 150) {
        viewModelScope.launch {
            setLoading(true, "Converting to images…")
            val outputDir = FileUtils.getOutputDir(context, "PDFMaster/Images")
            val fileName = FileUtils.getFileName(context, uri)
            val result = PDFProcessor.pdfToJpg(context, uri, outputDir, fileName, dpi) { done, total ->
                _uiState.update { it.copy(progress = done, totalSteps = total) }
            }
            setResult(result)
        }
    }

    // ===================== JPG TO PDF =====================
    fun jpgToPdf(uris: List<Uri>) {
        viewModelScope.launch {
            setLoading(true, "Creating PDF from images…")
            val outputDir = FileUtils.getOutputDir(context)
            val outputFile = FileUtils.createOutputFile(outputDir, "images_to_pdf")
            val result = PDFProcessor.jpgToPdf(context, uris, outputFile) { done, total ->
                _uiState.update { it.copy(progress = done, totalSteps = total) }
            }
            setResult(result)
        }
    }

    // ===================== ORGANIZE =====================
    fun reorderPages(uri: Uri, newOrder: List<Int>) {
        viewModelScope.launch {
            setLoading(true, "Reordering pages…")
            val outputDir = FileUtils.getOutputDir(context)
            val fileName = FileUtils.getFileName(context, uri)
            val outputFile = FileUtils.createOutputFile(outputDir, "${fileName}_organized")
            val result = PDFProcessor.reorderPages(context, uri, outputFile, newOrder)
            setResult(result)
        }
    }

    // ===================== RECENT FILES =====================
    fun deleteRecentFile(id: Long) {
        viewModelScope.launch { dao.deleteById(id) }
    }

    fun clearAllRecent() {
        viewModelScope.launch { dao.deleteAll() }
    }
}

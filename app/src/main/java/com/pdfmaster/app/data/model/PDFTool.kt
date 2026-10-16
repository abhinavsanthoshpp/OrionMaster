package com.pdfmaster.app.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class ToolCategory(val displayName: String) {
    ALL("All"),
    ORGANIZE("Organize PDF"),
    OPTIMIZE("Optimize PDF"),
    CONVERT("Convert PDF"),
    EDIT("Edit PDF"),
    SECURITY("PDF Security")
}

data class PDFTool(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val colorHex: Long,
    val iconName: String
)

object PDFTools {
    val allTools = listOf(
        // Organize
        PDFTool("merge", "Merge PDF", "Combine multiple PDFs into one", ToolCategory.ORGANIZE, "merge", 0xFFE53935, "merge"),
        PDFTool("split", "Split PDF", "Separate PDF into multiple files", ToolCategory.ORGANIZE, "split", 0xFFFF7043, "split"),
        PDFTool("remove_pages", "Remove Pages", "Delete unwanted pages from PDF", ToolCategory.ORGANIZE, "remove_pages", 0xFFEF5350, "remove_pages"),
        PDFTool("extract_pages", "Extract Pages", "Extract specific pages to new PDF", ToolCategory.ORGANIZE, "extract_pages", 0xFFF44336, "extract_pages"),
        PDFTool("organize", "Organize PDF", "Reorder and organize pages visually", ToolCategory.ORGANIZE, "organize", 0xFFE53935, "organize"),
        PDFTool("rotate", "Rotate PDF", "Rotate pages to correct orientation", ToolCategory.ORGANIZE, "rotate", 0xFFFF5722, "rotate"),

        // Optimize
        PDFTool("compress", "Compress PDF", "Reduce file size without losing quality", ToolCategory.OPTIMIZE, "compress", 0xFF43A047, "compress"),
        PDFTool("repair", "Repair PDF", "Fix corrupted or damaged PDF files", ToolCategory.OPTIMIZE, "repair", 0xFF66BB6A, "repair"),
        PDFTool("ocr", "OCR PDF", "Make scanned PDFs searchable with OCR", ToolCategory.OPTIMIZE, "ocr", 0xFF4CAF50, "ocr"),

        // Convert
        PDFTool("jpg_to_pdf", "JPG to PDF", "Convert images to PDF documents", ToolCategory.CONVERT, "jpg_to_pdf", 0xFF1E88E5, "jpg_to_pdf"),
        PDFTool("pdf_to_jpg", "PDF to JPG", "Convert PDF pages to image files", ToolCategory.CONVERT, "pdf_to_jpg", 0xFF42A5F5, "pdf_to_jpg"),

        // Edit
        PDFTool("add_watermark", "Add Watermark", "Add text or image watermark to PDF", ToolCategory.EDIT, "add_watermark", 0xFF8E24AA, "add_watermark"),
        PDFTool("add_page_numbers", "Page Numbers", "Add page numbers to your PDF", ToolCategory.EDIT, "add_page_numbers", 0xFF9C27B0, "add_page_numbers"),
        PDFTool("crop", "Crop PDF", "Crop pages to remove margins", ToolCategory.EDIT, "crop", 0xFFAB47BC, "crop"),
        PDFTool("edit", "Edit PDF", "Add text, images and annotations", ToolCategory.EDIT, "edit", 0xFF7B1FA2, "edit"),

        // Security
        PDFTool("protect", "Protect PDF", "Password protect your PDF", ToolCategory.SECURITY, "protect", 0xFFFF6D00, "protect"),
        PDFTool("unlock", "Unlock PDF", "Remove password from PDF", ToolCategory.SECURITY, "unlock", 0xFFFFCA28, "unlock"),
        PDFTool("sign", "Sign PDF", "Add digital signature to PDF", ToolCategory.SECURITY, "sign", 0xFF42A5F5, "sign"),
        PDFTool("compare", "Compare PDF", "Compare two PDFs side by side", ToolCategory.SECURITY, "compare", 0xFF26A69A, "compare")
    )

    fun getByCategory(category: ToolCategory): List<PDFTool> {
        return if (category == ToolCategory.ALL) allTools
        else allTools.filter { it.category == category }
    }
}

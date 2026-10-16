package com.pdfmaster.app.data.model

import androidx.compose.runtime.Immutable

enum class ToolCategory(val displayName: String) {
    ALL("All Tools"),
    ORGANIZE("Organize"),
    OPTIMIZE("Optimize"),
    CONVERT("Convert"),
    EDIT("Edit"),
    SECURITY("Security")
}

@Immutable
data class PDFTool(
    val id: String,
    val name: String,
    val description: String,
    val category: ToolCategory,
    val route: String,
    val colorHex: Long,
    val iconName: String,
    val isFeatured: Boolean = false,
    val badge: String? = null
)

object PDFTools {
    val allTools = listOf(
        // Featured & Organize
        PDFTool("merge", "Merge PDF", "Combine multiple PDFs into a single document", ToolCategory.ORGANIZE, "merge", 0xFFFF3B30, "merge", isFeatured = true, badge = "Popular"),
        PDFTool("compress", "Compress PDF", "Reduce document size while keeping high visual clarity", ToolCategory.OPTIMIZE, "compress", 0xFF00C853, "compress", isFeatured = true, badge = "Fast"),
        PDFTool("jpg_to_pdf", "JPG to PDF", "Convert gallery images or camera scans into a neat PDF", ToolCategory.CONVERT, "jpg_to_pdf", 0xFF007AFF, "jpg_to_pdf", isFeatured = true, badge = "Essential"),
        PDFTool("protect", "Protect PDF", "Encrypt documents with 128-bit AES password security", ToolCategory.SECURITY, "protect", 0xFFFF9500, "protect", isFeatured = true, badge = "AES-128"),

        // Organize
        PDFTool("split", "Split PDF", "Extract pages, custom ranges, or divide every N pages", ToolCategory.ORGANIZE, "split", 0xFFFF6442, "split"),
        PDFTool("remove_pages", "Remove Pages", "Delete unwanted or confidential pages with one tap", ToolCategory.ORGANIZE, "remove_pages", 0xFFFF453A, "remove_pages"),
        PDFTool("extract_pages", "Extract Pages", "Export targeted pages into an independent PDF", ToolCategory.ORGANIZE, "extract_pages", 0xFFFF5722, "extract_pages"),
        PDFTool("organize", "Organize PDF", "Reorder and rearrange page flow numerically", ToolCategory.ORGANIZE, "organize", 0xFFFF3B30, "organize"),
        PDFTool("rotate", "Rotate PDF", "Fix orientation by 90°, 180°, or 270° effortlessly", ToolCategory.ORGANIZE, "rotate", 0xFFFF6442, "rotate"),

        // Optimize
        PDFTool("repair", "Repair PDF", "Rebuild damaged, unreadable, or corrupted PDF streams", ToolCategory.OPTIMIZE, "repair", 0xFF34C759, "repair"),
        PDFTool("ocr", "OCR PDF", "Extract machine-readable text from scanned docs", ToolCategory.OPTIMIZE, "ocr", 0xFF30D158, "ocr", badge = "Pro"),

        // Convert
        PDFTool("pdf_to_jpg", "PDF to JPG", "Render pages into crisp images up to 300 DPI", ToolCategory.CONVERT, "pdf_to_jpg", 0xFF0A84FF, "pdf_to_jpg"),

        // Edit
        PDFTool("add_watermark", "Watermark", "Stamp custom text with angle, opacity, and color control", ToolCategory.EDIT, "add_watermark", 0xFFAF52DE, "add_watermark"),
        PDFTool("add_page_numbers", "Page Numbers", "Place header or footer numbering across 6 positions", ToolCategory.EDIT, "add_page_numbers", 0xFFBF5AF2, "add_page_numbers"),
        PDFTool("crop", "Crop PDF", "Trim page margins for reader-friendly viewing", ToolCategory.EDIT, "crop", 0xFFDA8FFF, "crop"),
        PDFTool("edit", "Edit PDF", "Annotate, draw, and markup PDF files directly", ToolCategory.EDIT, "edit", 0xFF9C27B0, "edit", badge = "Canvas"),

        // Security
        PDFTool("unlock", "Unlock PDF", "Remove passwords and view permissions seamlessly", ToolCategory.SECURITY, "unlock", 0xFFFFD60A, "unlock"),
        PDFTool("sign", "Sign PDF", "Draw or place digital signature on documents", ToolCategory.SECURITY, "sign", 0xFF5856D6, "sign"),
        PDFTool("compare", "Compare PDF", "Inspect differences between two PDFs side-by-side", ToolCategory.SECURITY, "compare", 0xFF32D74B, "compare")
    )

    val featuredTools: List<PDFTool> = allTools.filter { it.isFeatured }

    fun getByCategory(category: ToolCategory): List<PDFTool> {
        return if (category == ToolCategory.ALL) allTools
        else allTools.filter { it.category == category }
    }
}

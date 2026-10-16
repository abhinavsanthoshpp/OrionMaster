package com.pdfmaster.app.data.model

data class RecentFile(
    val id: Long = 0,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val lastOpened: Long = System.currentTimeMillis(),
    val toolUsed: String? = null
) {
    val sizeFormatted: String
        get() = when {
            sizeBytes < 1024 -> "${sizeBytes}B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024}KB"
            else -> String.format("%.1fMB", sizeBytes / (1024f * 1024f))
        }
}

data class ProcessResult(
    val success: Boolean,
    val outputPath: String? = null,
    val outputPaths: List<String>? = null,
    val message: String = "",
    val pageCount: Int = 0
)

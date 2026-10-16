package com.pdfmaster.app.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getFileName(context: Context, uri: Uri): String {
        var name = "document"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            if (nameIndex >= 0) {
                name = cursor.getString(nameIndex) ?: "document"
            }
        }
        return name.removeSuffix(".pdf")
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            if (sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    fun getOutputDir(context: Context, subDir: String = "PDFMaster"): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val outputDir = File(downloadsDir, subDir)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return outputDir
    }

    fun getInternalOutputDir(context: Context, subDir: String = "output"): File {
        val dir = File(context.filesDir, subDir)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun createOutputFile(dir: File, name: String, extension: String = "pdf"): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "${name}_${timestamp}.${extension}")
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            else -> String.format("%.1fMB", bytes / (1024f * 1024f))
        }
    }

    fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "*/*"
        }
    }

    fun getCacheDir(context: Context, subDir: String = "pdf_temp"): File {
        val dir = File(context.cacheDir, subDir)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun clearCache(context: Context) {
        getCacheDir(context).listFiles()?.forEach { it.delete() }
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) + ".pdf"
            val tempFile = File(getCacheDir(context), fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}

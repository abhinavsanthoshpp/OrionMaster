package com.pdfmaster.app.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getFileName(context: Context, uri: Uri): String {
        var name = "document"
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    name = cursor.getString(nameIndex) ?: "document"
                }
            }
        } catch (e: Exception) {
            name = uri.lastPathSegment ?: "document"
        }
        return name.removeSuffix(".pdf")
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst() && sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            /* ignore */
        }
        return size
    }

    /**
     * Reliable app-specific external storage directory that ALWAYS works across Android 6.0 through 15
     * without needing any runtime permissions.
     */
    fun getOutputDir(context: Context, subDir: String = "OrionMaster"): File {
        val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val outputDir = File(baseDir, subDir)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return outputDir
    }

    fun createOutputFile(dir: File, name: String, extension: String = "pdf"): File {
        if (!dir.exists()) dir.mkdirs()
        val cleanName = name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "${cleanName}_${timestamp}.${extension}")
    }

    /**
     * On Android 10+ (API 29+), also saves a copy into public MediaStore Downloads so
     * user can find it directly in system Files app without needing MANAGE_EXTERNAL_STORAGE.
     */
    fun exportToPublicDownloads(context: Context, file: File): Uri? {
        if (!file.exists()) return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                    put(MediaStore.Downloads.MIME_TYPE, getMimeType(file))
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/OrionMaster")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val itemUri = context.contentResolver.insert(collection, values)
                if (itemUri != null) {
                    context.contentResolver.openOutputStream(itemUri)?.use { out ->
                        file.inputStream().use { input -> input.copyTo(out) }
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(itemUri, values, null, null)
                    itemUri
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes} B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.1f MB", bytes / (1024f * 1024f))
        }
    }

    fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "application/octet-stream"
        }
    }

    fun getCacheDir(context: Context, subDir: String = "orion_temp"): File {
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

package com.freight24.common.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    suspend fun getFileFromUri(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val extension = context.contentResolver.getType(uri)?.let {
                if (it.contains("pdf", ignoreCase = true)) "pdf" else "jpg"
            } ?: "pdf"
            val file = File(context.cacheDir, "temp_file_${System.currentTimeMillis()}.$extension")
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0L) file else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun convertImageToPdf(context: Context, uris: List<Uri>): File? = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            var pageCount = 0
            uris.forEachIndexed { index, uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    pageCount += 1
                    bitmap.recycle()
                }
                inputStream?.close()
            }

            if (pageCount == 0) {
                pdfDocument.close()
                return@withContext null
            }

            val file = File(context.cacheDir, "temp_converted_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            if (file.exists() && file.length() > 0L) file else null
        } catch (e: Exception) {
            null
        }
    }
}

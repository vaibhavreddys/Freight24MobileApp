package com.freight24.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

object FileUtils {
    suspend fun getFileFromUri(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        var file: File? = null
        try {
            val isImage = context.contentResolver.getType(uri)?.startsWith("image/") == true
            val target = File.createTempFile("upload_", if (isImage) ".jpg" else ".pdf", context.cacheDir)
            file = target
            if (isImage) {
                target.writeBytes(compressImage(context, uri, DocumentUploadPolicy.MAX_FILE_SIZE_BYTES.toInt()))
            } else {
                val input = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to read document")
                input.use { source ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var total = 0L
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val read = source.read(buffer)
                            if (read < 0) break
                            total += read
                            DocumentUploadPolicy.validateSize(total)
                            output.write(buffer, 0, read)
                        }
                    }
                }
            }
            DocumentUploadPolicy.validateFile(target)
            target
        } catch (error: Exception) {
            file?.delete()
            if (error is DocumentUploadException || error is CancellationException) throw error
            null
        }
    }

    suspend fun convertImageToPdf(
        context: Context,
        uris: List<Uri>,
        maxWidth: Int = DocumentUploadPolicy.IMAGE_MAX_DIMENSION,
    ): File? = withContext(Dispatchers.IO) {
        DocumentUploadPolicy.validateImageCount(uris.size)
        var file: File? = null
        try {
            PDFBoxResourceLoader.init(context.applicationContext)
            val target = File.createTempFile("upload_", ".pdf", context.cacheDir)
            file = target
            PDDocument().use { document ->
                // Reserve PDF object overhead; do not silently drop selected pages.
                val budget = ((DocumentUploadPolicy.MAX_FILE_SIZE_BYTES - 64_000) / uris.size).toInt()
                for (uri in uris) {
                    currentCoroutineContext().ensureActive()
                    val bytes = compressImage(context, uri, budget, maxWidth)
                    val page = PDPage(PDRectangle.A4)
                    document.addPage(page)
                    val image = bytes.inputStream().use { JPEGFactory.createFromStream(document, it) }
                    val scale = minOf(page.mediaBox.width / image.width, page.mediaBox.height / image.height)
                    val width = image.width * scale
                    val height = image.height * scale
                    PDPageContentStream(document, page).use { content ->
                        content.drawImage(image, (page.mediaBox.width - width) / 2, (page.mediaBox.height - height) / 2, width, height)
                    }
                }
                document.save(target)
            }
            DocumentUploadPolicy.validateFile(target)
            target
        } catch (error: Exception) {
            file?.delete()
            if (error is DocumentUploadException || error is CancellationException) throw error
            throw DocumentUploadException("Unable to process every image. Please reselect readable images and try again.")
        }
    }

    private suspend fun compressImage(
        context: Context,
        uri: Uri,
        budgetBytes: Int,
        maxDimension: Int = DocumentUploadPolicy.IMAGE_MAX_DIMENSION,
    ): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Unreadable image")
        val limit = maxDimension.coerceIn(1024, DocumentUploadPolicy.IMAGE_MAX_DIMENSION)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > limit * 2) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        var bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("Unreadable image")
        try {
            val matrix = orientationMatrix(context, uri)
            val scale = minOf(1f, limit.toFloat() / maxOf(bitmap.width, bitmap.height))
            matrix.postScale(scale, scale)
            if (!matrix.isIdentity) {
                val transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (transformed !== bitmap) {
                    bitmap.recycle()
                    bitmap = transformed
                }
            }
            if (bitmap.hasAlpha()) {
                val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                Canvas(opaque).apply {
                    drawColor(Color.WHITE)
                    drawBitmap(bitmap, 0f, 0f, null)
                }
                bitmap.recycle()
                bitmap = opaque
            }
            for (dimension in listOf(limit, 1280, 1024).distinct().filter { it <= limit }) {
                currentCoroutineContext().ensureActive()
                val ratio = minOf(1f, dimension.toFloat() / maxOf(bitmap.width, bitmap.height))
                if (ratio < 1f) {
                    val scaled = Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt().coerceAtLeast(1), (bitmap.height * ratio).toInt().coerceAtLeast(1), true)
                    if (scaled !== bitmap) {
                        bitmap.recycle()
                        bitmap = scaled
                    }
                }
                for (quality in listOf(82, 72, 62)) {
                    currentCoroutineContext().ensureActive()
                    val output = ByteArrayOutputStream()
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)) { "Image compression failed" }
                    if (output.size() <= budgetBytes) return output.toByteArray()
                }
            }
            throw DocumentUploadException("These images cannot fit within 3 MB clearly. Please select fewer images or smaller photos.")
        } finally {
            bitmap.recycle()
        }
    }

    private fun orientationMatrix(context: Context, uri: Uri): Matrix {
        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: IOException) {
            ExifInterface.ORIENTATION_NORMAL
        }
        return Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(-90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(-90f)
            }
        }
    }
}

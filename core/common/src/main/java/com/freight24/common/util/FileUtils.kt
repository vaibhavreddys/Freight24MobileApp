package com.freight24.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    suspend fun getFileFromUri(
        context: Context,
        uri: Uri,
    ): File? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val extension =
                    context.contentResolver.getType(uri)?.let {
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

    suspend fun convertImageToPdf(
        context: Context,
        uris: List<Uri>,
        maxWidth: Int = 1920, // Target max dimension for downsampling
    ): File? =
        withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            var pageCount = 0

            try {
                uris.forEachIndexed { index, uri ->
                    try {
                        // ---------------------------------------------------------
                        // STEP 1: Read image bounds only (first pass)
                        // ---------------------------------------------------------
                        val boundsOptions =
                            BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream, null, boundsOptions)
                        }

                        // Validate dimensions
                        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                            Log.w("PDFConverter", "Invalid image dimensions for $uri")
                            return@forEachIndexed
                        }

                        // ---------------------------------------------------------
                        // STEP 2: Calculate optimal downsampling
                        // ---------------------------------------------------------
                        val decodeOptions =
                            BitmapFactory.Options().apply {
                                inPreferredConfig = Bitmap.Config.RGB_565 // 50% memory vs ARGB_8888
                                inSampleSize = calculateInSampleSize(boundsOptions, maxWidth)
                            }

                        // ---------------------------------------------------------
                        // STEP 3: Decode optimized bitmap (second pass)
                        // ---------------------------------------------------------
                        val bitmap =
                            context.contentResolver.openInputStream(uri)?.use { stream ->
                                BitmapFactory.decodeStream(stream, null, decodeOptions)
                            } ?: run {
                                Log.w("PDFConverter", "Failed to decode bitmap for $uri")
                                return@forEachIndexed
                            }

                        // ---------------------------------------------------------
                        // STEP 4: Fix EXIF rotation
                        // ---------------------------------------------------------
                        val rotatedBitmap = rotateBitmapIfRequired(context, bitmap, uri)
                        if (rotatedBitmap != bitmap && !bitmap.isRecycled) {
                            bitmap.recycle()
                        }

                        // Guard against invalid dimensions after rotation
                        if (rotatedBitmap.width <= 0 || rotatedBitmap.height <= 0) {
                            Log.w("PDFConverter", "Invalid dimensions after rotation")
                            if (!rotatedBitmap.isRecycled) rotatedBitmap.recycle()
                            return@forEachIndexed
                        }

                        // ---------------------------------------------------------
                        // STEP 5: Create PDF page with white background
                        // ---------------------------------------------------------
                        val pageInfo =
                            PdfDocument.PageInfo.Builder(
                                pageWidth,
                                pageHeight,
                                index + 1,
                            ).create()

                        val page = pdfDocument.startPage(pageInfo)
                        page.canvas.drawColor(Color.WHITE) // Prevent transparency issues

                        // ---------------------------------------------------------
                        // STEP 6: Scale & center image proportionally
                        // ---------------------------------------------------------
                        val scale =
                            minOf(
                                pageWidth.toFloat() / rotatedBitmap.width,
                                pageHeight.toFloat() / rotatedBitmap.height,
                            )
                        val scaledWidth = rotatedBitmap.width * scale
                        val scaledHeight = rotatedBitmap.height * scale
                        val left = (pageWidth - scaledWidth) / 2f
                        val top = (pageHeight - scaledHeight) / 2f

                        val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                        page.canvas.drawBitmap(rotatedBitmap, null, destRect, null)

                        pdfDocument.finishPage(page)

                        // Recycle bitmap after page is finished
                        if (!rotatedBitmap.isRecycled) {
                            rotatedBitmap.recycle()
                        }
                        pageCount++
                    } catch (e: Exception) {
                        Log.w("PDFConverter", "Failed to process image: ${uri.lastPathSegment}", e)
                        // Continue with next image - don't fail entire batch
                    }
                }

                // ---------------------------------------------------------
                // STEP 7: Handle empty result
                // ---------------------------------------------------------
                if (pageCount == 0) {
                    pdfDocument.close()
                    Log.e("PDFConverter", "No images were successfully converted")
                    return@withContext null
                }

                // ---------------------------------------------------------
                // STEP 8: Write PDF to file with cleanup on failure
                // ---------------------------------------------------------
                val file = File(context.cacheDir, "converted_${System.currentTimeMillis()}.pdf")

                try {
                    FileOutputStream(file).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    Log.d("PDFConverter", "PDF created: ${file.length() / 1024} KB, $pageCount pages")
                    file.takeIf { it.exists() && it.length() > 0 }
                } catch (e: IOException) {
                    file.delete() // Clean up partial/corrupted file
                    Log.e("PDFConverter", "Failed to write PDF file", e)
                    null
                } finally {
                    pdfDocument.close()
                }
            } catch (e: Exception) {
                Log.e("PDFConverter", "Unexpected error in PDF conversion", e)
                null
            }
        }

    /**
     * Calculate optimal inSampleSize for downsampling.
     * Returns a power-of-2 value that ensures at least one dimension <= maxWidth.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxWidth: Int,
    ): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1

        // Only downsample if image exceeds target
        if (width > maxWidth || height > maxWidth) {
            val halfWidth = width / 2
            val halfHeight = height / 2

            // Double sample size while either dimension still exceeds limit
            while (halfWidth / inSampleSize > maxWidth ||
                halfHeight / inSampleSize > maxWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    /**
     * Apply EXIF orientation to bitmap if needed.
     * Returns the original bitmap if no rotation is required.
     */
    private fun rotateBitmapIfRequired(
        context: Context,
        bitmap: Bitmap,
        uri: Uri,
    ): Bitmap {
        return try {
            val orientation =
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL,
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL

            val rotationDegrees =
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> return bitmap // No rotation needed
                }

            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                ?: bitmap // Fallback if creation fails
        } catch (e: Exception) {
            Log.w("PDFConverter", "EXIF rotation failed", e)
            bitmap // Return original on error
        }
    }
}

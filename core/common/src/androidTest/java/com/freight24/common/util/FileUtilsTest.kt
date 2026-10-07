package com.freight24.common.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import java.io.RandomAccessFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileUtilsTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val files = mutableListOf<File>()

    @After fun cleanup() { files.forEach { it.delete() } }

    private fun temporaryFile(suffix: String) =
        File.createTempFile("upload_test_", suffix, context.cacheDir).also { files.add(it) }

    private fun photo(): Uri {
        val bitmap = Bitmap.createBitmap(3000, 4000, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            val paint = Paint().apply { color = Color.BLACK; textSize = 80f }
            for (row in 1..35) drawText("Lorry Receipt / Invoice 123456", 60f, row * 100f, paint)
        }
        val file = temporaryFile(".jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        bitmap.recycle()
        return Uri.fromFile(file)
    }

    @Test fun fiveHighResolutionPhotosProduceFiveReadablePagesUnderThreeMb() = runBlocking {
        val uri = photo()
        val file = requireNotNull(FileUtils.convertImageToPdf(context, List(5) { uri })).also { files.add(it) }
        assertTrue(file.length() in 1..DocumentUploadPolicy.MAX_FILE_SIZE_BYTES)
        android.util.Log.i("DocumentUploadTest", "Five-page scan PDF: ${file.length()} bytes")
        PDDocument.load(file).use { document ->
            assertEquals(5, document.numberOfPages)
            for (page in document.pages) {
                val resources = page.resources
                val image = resources.getXObject(resources.xObjectNames.first()) as PDImageXObject
                assertTrue(maxOf(image.width, image.height) <= 1600)
                assertTrue(image.cosObject.getNameAsString("Filter").contains("DCTDecode"))
            }
        }
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                assertEquals(5, renderer.pageCount)
                renderer.openPage(0).use { page ->
                    val bitmap = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val pixels = IntArray(bitmap.width * bitmap.height)
                    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    assertTrue("PDF page must contain visible text", pixels.count { Color.red(it) < 100 } > 100)
                    bitmap.recycle()
                }
            }
        }
    }

    @Test fun fiveNoisyPhotosUseCompressionFallbackWithoutLosingPages() = runBlocking {
        val bitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        val random = java.util.Random(42)
        val pixels = IntArray(2000 * 2000) { Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)) }
        bitmap.setPixels(pixels, 0, 2000, 0, 0, 2000, 2000)
        val source = temporaryFile(".jpg")
        source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        bitmap.recycle()
        val file = requireNotNull(FileUtils.convertImageToPdf(context, List(5) { Uri.fromFile(source) })).also { files.add(it) }
        assertTrue(file.length() <= DocumentUploadPolicy.MAX_FILE_SIZE_BYTES)
        android.util.Log.i("DocumentUploadTest", "Five-page noisy-photo PDF: ${file.length()} bytes")
        PDDocument.load(file).use { document ->
            assertEquals(5, document.numberOfPages)
            val resources = document.getPage(0).resources
            val image = resources.getXObject(resources.xObjectNames.first()) as PDImageXObject
            assertTrue("Noisy photos must use smaller resolution fallback", maxOf(image.width, image.height) < 1600)
        }
    }

    @Test fun rejectsSixImages() = runBlocking {
        val uri = photo()
        try {
            FileUtils.convertImageToPdf(context, List(6) { uri })
            fail("Six images must be rejected")
        } catch (_: DocumentUploadException) { }
    }

    @Test fun unreadableImageDoesNotSilentlyDropAPage() = runBlocking {
        val valid = photo()
        val invalid = Uri.fromFile(temporaryFile(".jpg"))
        val before = context.cacheDir.listFiles()!!.filter { it.name.startsWith("upload_") }.toSet()
        try {
            FileUtils.convertImageToPdf(context, listOf(valid, invalid))
            fail("Unreadable page must fail the document")
        } catch (_: DocumentUploadException) { }
        val after = context.cacheDir.listFiles()!!.filter { it.name.startsWith("upload_") }.toSet()
        assertEquals(before, after)
    }

    @Test fun respectsCameraOrientation() = runBlocking {
        val uri = photo()
        ExifInterface(uri.path!!).apply {
            setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
            saveAttributes()
        }
        val file = requireNotNull(FileUtils.convertImageToPdf(context, listOf(uri))).also { files.add(it) }
        PDDocument.load(file).use { document ->
            val resources = document.getPage(0).resources
            val image = resources.getXObject(resources.xObjectNames.first()) as PDImageXObject
            assertTrue(image.width > image.height)
        }
    }

    @Test fun keepsExistingPdfBytesUnchanged() = runBlocking {
        val source = requireNotNull(FileUtils.convertImageToPdf(context, listOf(photo()))).also { files.add(it) }
        val copied = requireNotNull(FileUtils.getFileFromUri(context, Uri.fromFile(source))).also { files.add(it) }
        assertArrayEquals(source.readBytes(), copied.readBytes())
        assertNotEquals(source.path, copied.path)
    }

    @Test fun rejectsOversizedPdfAndDeletesPartialCopy() = runBlocking {
        val source = temporaryFile(".pdf")
        RandomAccessFile(source, "rw").use { it.setLength(3_000_001) }
        val before = context.cacheDir.listFiles()!!.toSet()
        try {
            FileUtils.getFileFromUri(context, Uri.fromFile(source))
            fail("Oversized PDF must be rejected")
        } catch (_: DocumentUploadException) { }
        assertEquals(before, context.cacheDir.listFiles()!!.toSet())
    }
}

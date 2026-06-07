package com.freight24.ui.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import com.rajat.pdfviewer.util.CacheStrategy
import com.rajat.pdfviewer.util.PdfSource
import com.rajat.pdfviewer.util.ToolbarTitleBehavior
import com.rajat.pdfviewer.util.saveTo
import java.io.File

@Composable
fun PdfRendererView(source: PdfSource) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {

        PdfRendererViewCompose(
            source = source,
            lifecycleOwner = lifecycleOwner,
            modifier = Modifier.fillMaxSize(),
            statusCallBack = object : PdfRendererView.StatusCallBack {
                override fun onPdfLoadStart() {
                    Log.d("PdfViewer", "PDF load started")
                }

                override fun onPdfLoadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {
                    Log.d("PdfViewer", "Progress: $progress%")
                }

                override fun onPdfLoadSuccess(absolutePath: String) {
                    Log.d("PdfViewer", "PDF loaded successfully: $absolutePath")
                }

                override fun onError(error: Throwable) {
                    Log.e("PdfViewer", "Error loading PDF", error)
                }

                override fun onPageChanged(currentPage: Int, totalPage: Int) {
                    Log.d("PdfViewer", "Page changed: $currentPage/$totalPage")
                }
            }
        )
    }
}

/**
 * Launches a PDF file from a URL.
 */
fun getPdfLaunchIntent(url: String, context: Context) : Intent {
        return PdfViewerActivity.launchPdfFromUrl(
            context = context,
            pdfUrl = url,
            pdfTitle = "F24 Document",
            saveTo = saveTo.DOWNLOADS,
            enableDownload = true,
            toolbarTitleBehavior = ToolbarTitleBehavior.SINGLE_LINE_SCROLLABLE,
            cacheStrategy = CacheStrategy.MAXIMIZE_PERFORMANCE
        )
}

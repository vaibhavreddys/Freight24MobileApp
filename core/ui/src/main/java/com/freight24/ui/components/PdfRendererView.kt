package com.freight24.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.util.PdfSource
import java.io.File

@Composable
fun PdfRendererView(file: File) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        PdfRendererViewCompose(
            source = PdfSource.LocalFile(file),
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

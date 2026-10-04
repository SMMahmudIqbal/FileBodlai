package com.example.fileconverter.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable

/**
 * PdfViewerScreen delegates directly to QuickLookScreen for
 * memory-safe on-demand rendering, pinch-to-zoom, pan, and scrubbing.
 */
@Composable
fun PdfViewerScreen(
    pdfUri: Uri,
    title: String = "PDF Viewer",
    onBack: () -> Unit
) {
    QuickLookScreen(
        uri = pdfUri,
        mimeType = "application/pdf",
        title = title,
        onBack = onBack
    )
}

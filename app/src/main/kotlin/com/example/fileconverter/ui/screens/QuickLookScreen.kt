package com.example.fileconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileconverter.ui.components.GlassBackground
import com.example.fileconverter.ui.components.GlassCard
import com.example.fileconverter.ui.components.bounceClick
import com.example.fileconverter.ui.theme.BodlaiTheme
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.util.SensoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bodlai Quick Look: High-Performance, Memory-Safe In-App Document Previewer
 * - Supports PDFs (on-demand page rendering at 2x Retina, never buffers entire document)
 * - Supports Images (JPG, PNG, WebP with safe inSampleSize downsampling)
 * - Full interactive Pinch-to-Zoom (1x - 5x) + Pan
 * - Double-tap zoom toggle (1x / 2.5x)
 * - 90° Clockwise rotation
 * - Fluid Page Scrubber Slider + jump controls
 * - Direct Share & Open In External App integration
 */
@Composable
fun QuickLookScreen(
    uri: Uri,
    mimeType: String = "application/pdf",
    title: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = BodlaiTheme.colors

    val fileName = remember(uri) {
        title ?: FileUtils.getFileName(context, uri).ifBlank { "Document Preview" }
    }

    val isPdf = remember(uri, mimeType) {
        mimeType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true)
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    // Initialize document or image
    LaunchedEffect(uri) {
        isLoading = true
        errorMessage = null
        withContext(Dispatchers.IO) {
            try {
                if (isPdf) {
                    val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                    if (pfd != null) {
                        val renderer = PdfRenderer(pfd)
                        fileDescriptor = pfd
                        pdfRenderer = renderer
                        totalPages = renderer.pageCount
                        currentPage = 0
                    } else {
                        errorMessage = "Cannot access PDF file."
                    }
                } else {
                    // Safe image decoding with memory cap
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }

                    var inSample = 1
                    val maxDimension = 2560
                    while (options.outWidth / inSample > maxDimension || options.outHeight / inSample > maxDimension) {
                        inSample *= 2
                    }

                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = inSample
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }

                    val bmp = context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }

                    if (bmp != null) {
                        currentBitmap = bmp
                        totalPages = 1
                        currentPage = 0
                    } else {
                        errorMessage = "Unable to decode image."
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to open document."
            } finally {
                isLoading = false
            }
        }
    }

    // On-demand rendering of current page for PDF (Recycles old page immediately)
    LaunchedEffect(currentPage, pdfRenderer) {
        val renderer = pdfRenderer ?: return@LaunchedEffect
        if (currentPage in 0 until totalPages) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val page = renderer.openPage(currentPage)
                    // High-resolution Retina render (up to 2160w x 3840h max)
                    val targetW = (page.width * 2).coerceIn(400, 2160)
                    val targetH = (page.height * 2).coerceIn(400, 3840)

                    val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    withContext(Dispatchers.Main) {
                        val prev = currentBitmap
                        currentBitmap = bmp
                        prev?.recycle()
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    // Clean up native resources on exit
    DisposableEffect(Unit) {
        onDispose {
            try {
                currentBitmap?.recycle()
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (_: Exception) {}
        }
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceCardSubtle)
                        .border(1.dp, colors.surfaceCardBorder, RoundedCornerShape(12.dp))
                        .bounceClick(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isPdf && totalPages > 0) "Page ${currentPage + 1} of $totalPages • Quick Look" else "Quick Look Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Zoom reset pill indicator
                if (scale != 1f || offset != Offset.Zero) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.accentWarmAmber.copy(alpha = 0.2f))
                            .border(1.dp, colors.accentWarmAmber.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
                            .clickable {
                                SensoryManager.tick(context, view)
                                scale = 1f
                                offset = Offset.Zero
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(scale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentWarmAmber,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // 90° Rotate Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceCardSubtle)
                        .bounceClick {
                            SensoryManager.click(context, view)
                            rotationAngle = (rotationAngle + 90f) % 360f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Rotate 90°",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Share Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceCardSubtle)
                        .bounceClick {
                            SensoryManager.click(context, view)
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    type = mimeType
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
                            } catch (_: Exception) {}
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = colors.accentPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Open External App Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceCardSubtle)
                        .bounceClick {
                            SensoryManager.click(context, view)
                            try {
                                val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(openIntent)
                            } catch (_: Exception) {}
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "External App",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Interactive Viewing Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                SensoryManager.click(context, view)
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                    offset = Offset.Zero
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (errorMessage != null) {
                    GlassCard(
                        backgroundColor = colors.errorGlass,
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Unable to Preview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.errorText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "Unknown error",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.errorText.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    try {
                                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, mimeType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(openIntent)
                                    } catch (_: Exception) {}
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary)
                            ) {
                                Text("Open in External Viewer", color = colors.textOnAccent)
                            }
                        }
                    }
                } else if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "Document Page",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                                rotationZ = rotationAngle
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                // Loading Spinner Overlay
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colors.accentPrimary,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // Bottom Navigation Scrubber (Only for multi-page PDFs)
            if (isPdf && totalPages > 1) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = colors.surfaceCard,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prev page
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceCardSubtle)
                                    .bounceClick {
                                        if (currentPage > 0) {
                                            SensoryManager.tick(context, view)
                                            currentPage--
                                            scale = 1f
                                            offset = Offset.Zero
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Page",
                                    tint = if (currentPage > 0) colors.textPrimary else colors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Current Page Label
                            Text(
                                text = "Page ${currentPage + 1} of $totalPages",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                            // Next page
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceCardSubtle)
                                    .bounceClick {
                                        if (currentPage < totalPages - 1) {
                                            SensoryManager.tick(context, view)
                                            currentPage++
                                            scale = 1f
                                            offset = Offset.Zero
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Page",
                                    tint = if (currentPage < totalPages - 1) colors.textPrimary else colors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Page Scrubber Slider
                        Slider(
                            value = currentPage.toFloat(),
                            onValueChange = { newVal ->
                                val target = newVal.toInt().coerceIn(0, totalPages - 1)
                                if (target != currentPage) {
                                    SensoryManager.tick(context, view)
                                    currentPage = target
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            },
                            valueRange = 0f..(totalPages - 1).toFloat(),
                            steps = (totalPages - 2).coerceAtLeast(0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = colors.accentPrimary,
                                activeTrackColor = colors.accentPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

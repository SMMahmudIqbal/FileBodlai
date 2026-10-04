package com.example.fileconverter.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.GlassTheme
import com.example.fileconverter.util.SensoryManager
import com.example.fileconverter.viewmodel.BatchCompressViewModel

@Composable
fun BatchCompressScreen(onBack: () -> Unit) {
    val vm: BatchCompressViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GlassTheme.colors

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
        }
        if (uris.isNotEmpty()) vm.addImages(uris)
    }

    if (state.resultCount > 0 && state.firstResultUri != null) {
        ResultScreen(
            title = "${state.resultCount} Images Compressed!",
            message = "Saved to Downloads/FileBodlai",
            resultUri = state.firstResultUri!!,
            mimeType = "image/jpeg",
            context = context,
            onDone = { vm.clearResult(); onBack() }
        )
        return
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            GlassTopBar(title = "Batch Compress & Resize", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Drop zone add button
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { imagePicker.launch(arrayOf("image/jpeg", "image/png", "image/webp")) },
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(1.dp, colors.glassBorderTopLeft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = colors.accentPill,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Select Multiple Images",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Batch optimize size & dimensions",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Options card
                GlassCard(
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = "Resize Percentage",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            BatchCompressViewModel.ResizeMode.ORIGINAL to "100%",
                            BatchCompressViewModel.ResizeMode.PERCENT_75 to "75%",
                            BatchCompressViewModel.ResizeMode.PERCENT_50 to "50%",
                            BatchCompressViewModel.ResizeMode.PERCENT_25 to "25%"
                        ).forEach { (mode, label) ->
                            GlassChip(
                                selected = state.resizeMode == mode,
                                label = label,
                                onClick = { vm.setResizeMode(mode) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Target Max File Size Preset",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            null to "Custom",
                            200 to "< 200 KB",
                            500 to "< 500 KB",
                            1000 to "< 1 MB"
                        ).forEach { (kb, label) ->
                            GlassChip(
                                selected = state.targetMaxKb == kb,
                                label = label,
                                onClick = {
                                    SensoryManager.click(context, view)
                                    vm.setTargetMaxKb(kb)
                                }
                            )
                        }
                    }

                    if (state.targetMaxKb == null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quality: ${state.quality}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            val estReduction = (100 - state.quality).coerceAtLeast(0)
                            Text(
                                text = "Est. ~$estReduction% savings",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accentWarmAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = state.quality.toFloat(),
                            onValueChange = { newVal ->
                                val oldVal = state.quality
                                val newInt = newVal.toInt()
                                if (newInt != oldVal) {
                                    SensoryManager.tick(context, view)
                                }
                                vm.setQuality(newInt)
                            },
                            valueRange = 10f..100f,
                            steps = 17,
                            colors = SliderDefaults.colors(thumbColor = colors.accentPill, activeTrackColor = colors.accentPill)
                        )
                    }
                }

                // Total size & estimated savings summary
                if (state.images.isNotEmpty()) {
                    val totalBytes = state.images.sumOf { it.rawBytes }
                    val estSavingsPercent = if (state.targetMaxKb != null) {
                        val maxTargetTotal = state.images.size * state.targetMaxKb!! * 1024L
                        if (totalBytes > maxTargetTotal) {
                            (((totalBytes - maxTargetTotal).toFloat() / totalBytes) * 100).toInt().coerceIn(10, 90)
                        } else 20
                    } else {
                        (100 - state.quality).coerceIn(10, 85)
                    }
                    val estSavedBytes = (totalBytes * (estSavingsPercent / 100.0)).toLong()

                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceCardSubtle,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Original: ${com.example.fileconverter.util.FileUtils.formatSize(totalBytes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "⚡ Est. Saved: ~${com.example.fileconverter.util.FileUtils.formatSize(estSavedBytes)} ($estSavingsPercent% smaller)",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accentWarmAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Interactive Before / After Quality Preview
                if (state.images.isNotEmpty()) {
                    var splitPosition by remember { mutableFloatStateOf(0.5f) }
                    val firstImage = state.images.first()
                    var previewBitmap by remember(firstImage.uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

                    LaunchedEffect(firstImage.uri) {
                        try {
                            val stream = context.contentResolver.openInputStream(firstImage.uri)
                            previewBitmap = BitmapFactory.decodeStream(stream)
                            stream?.close()
                        } catch (_: Exception) {}
                    }

                    if (previewBitmap != null) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Full image render
                                Image(
                                    bitmap = previewBitmap!!.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Left overlay indicator (Original)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Original 100%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }

                                // Right overlay indicator (Simulated Compressed)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.accentWarmAmber.copy(alpha = 0.85f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Compressed ~${state.quality}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Interactive Split Slider Control
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.70f))
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Interactive Compare Split",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = "${(splitPosition * 100).toInt()}% Original",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.accentPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Slider(
                                        value = splitPosition,
                                        onValueChange = { newVal ->
                                            val oldStep = (splitPosition * 20).toInt()
                                            val newStep = (newVal * 20).toInt()
                                            if (newStep != oldStep) {
                                                SensoryManager.tick(context, view)
                                            }
                                            splitPosition = newVal
                                        },
                                        valueRange = 0.05f..0.95f,
                                        modifier = Modifier.height(28.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = colors.accentWarmAmber,
                                            activeTrackColor = colors.accentWarmAmber
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // File list
                if (state.images.isNotEmpty()) {
                    Text(
                        text = "Selected Images (${state.images.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.images) { index, item ->
                            GlassCard(
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = item.size,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = { vm.removeImage(index) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, "Remove", tint = colors.errorText)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Add images to begin batch compression",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary.copy(alpha = 0.6f)
                        )
                    }
                }

                // Error
                state.error?.let {
                    GlassCard(
                        backgroundColor = colors.errorGlass,
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = it,
                            color = colors.errorText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Progress
                if (state.isProcessing) {
                    val (cur, total) = state.progress
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassProgress(
                        current = cur,
                        total = total,
                        label = "Compressing $cur of $total",
                        onCancel = { vm.cancel() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom pinned action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                GlassButton(
                    text = if (state.isProcessing) "Compressing..." else "Compress All (${state.images.size})",
                    onClick = { vm.processBatch() },
                    enabled = state.images.isNotEmpty() && !state.isProcessing,
                    isLoading = state.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

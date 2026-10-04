package com.example.fileconverter.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.GlassTheme
import com.example.fileconverter.viewmodel.PdfToImagesViewModel

@Composable
fun PdfToImagesScreen(onBack: () -> Unit) {
    val vm: PdfToImagesViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val colors = GlassTheme.colors

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            vm.pickPdf(it)
        }
    }

    if (state.resultCount > 0 && state.firstResultUri != null) {
        ResultScreen(
            title = "${state.resultCount} Images Saved!",
            message = "Extracted to Downloads/FileBodlai",
            resultUri = state.firstResultUri!!,
            mimeType = if (state.format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg",
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
            GlassTopBar(title = "PDF → Images", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drop zone card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { pdfPicker.launch(arrayOf("application/pdf")) },
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp,
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, colors.glassBorderTopLeft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = colors.accentPill,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.pdfUri != null) "Tap to change PDF" else "Select PDF to Extract",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Renders pages into clean image files",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (state.pdfUri != null) {
                    GlassFilePill(
                        name = state.pdfName,
                        size = "${state.pageCount} pages"
                    )

                    // Options Card
                    GlassCard(
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "Extraction Settings",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // DPI options
                        Text("Resolution (DPI):", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(72, 150, 300).forEach { dpi ->
                                GlassChip(
                                    selected = state.dpi == dpi,
                                    label = "${dpi} dpi",
                                    onClick = { vm.setDpi(dpi) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Format options
                        Text("Output Format:", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassChip(
                                selected = state.format == Bitmap.CompressFormat.JPEG,
                                label = "JPG",
                                onClick = { vm.setFormat(Bitmap.CompressFormat.JPEG) }
                            )
                            GlassChip(
                                selected = state.format == Bitmap.CompressFormat.PNG,
                                label = "PNG",
                                onClick = { vm.setFormat(Bitmap.CompressFormat.PNG) }
                            )
                        }

                        // Page range sliders
                        if (state.pageCount > 0) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Page Range: ${state.pageRangeStart} – ${state.pageRangeEnd} of ${state.pageCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Start page:", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            Slider(
                                value = state.pageRangeStart.toFloat(),
                                onValueChange = { vm.setRangeStart(it.toInt().coerceAtMost(state.pageRangeEnd)) },
                                valueRange = 1f..state.pageCount.toFloat(),
                                steps = (state.pageCount - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(thumbColor = colors.accentPill, activeTrackColor = colors.accentPill)
                            )
                            Text("End page:", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            Slider(
                                value = state.pageRangeEnd.toFloat(),
                                onValueChange = { vm.setRangeEnd(it.toInt().coerceAtLeast(state.pageRangeStart)) },
                                valueRange = 1f..state.pageCount.toFloat(),
                                steps = (state.pageCount - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(thumbColor = colors.accentPill, activeTrackColor = colors.accentPill)
                            )
                        }
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
                if (state.isConverting) {
                    val (cur, total) = state.progress
                    GlassProgress(
                        current = cur,
                        total = total,
                        label = "Extracting page $cur of $total"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom pinned action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                GlassButton(
                    text = if (state.isConverting) "Exporting..." else "Export Images",
                    onClick = { vm.convert() },
                    enabled = state.pdfUri != null && !state.isConverting,
                    isLoading = state.isConverting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

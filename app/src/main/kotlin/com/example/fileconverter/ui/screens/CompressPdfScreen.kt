package com.example.fileconverter.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.BodlaiTheme
import com.example.fileconverter.util.FileUtils
import com.example.fileconverter.viewmodel.CompressPdfViewModel

@Composable
fun CompressPdfScreen(
    onBack: () -> Unit,
    onViewPdf: (android.net.Uri) -> Unit = {}
) {
    val vm: CompressPdfViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val colors = BodlaiTheme.colors

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            vm.setPdf(uri)
        }
    }

    if (state.resultUri != null) {
        val originalSize = state.rawBytes
        val compressedSize = state.resultBytes
        val savedPercent = if (originalSize > 0) {
            (((originalSize - compressedSize).toFloat() / originalSize) * 100).toInt().coerceAtLeast(0)
        } else 0

        ResultScreen(
            title = "PDF Compressed!",
            message = "Reduced from ${FileUtils.formatSize(originalSize)} to ${FileUtils.formatSize(compressedSize)} ($savedPercent% smaller)",
            resultUri = state.resultUri!!,
            mimeType = "application/pdf",
            context = context,
            onDone = {
                vm.clearResult()
                onBack()
            },
            onViewInApp = {
                onViewPdf(state.resultUri!!)
            }
        )
        return
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            GlassTopBar(title = "Compress PDF", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Drop zone card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { pdfPicker.launch(arrayOf("application/pdf")) },
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
                                .border(1.dp, colors.surfaceCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = colors.accentPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (state.selectedUri != null) state.fileName else "Select PDF File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = if (state.selectedUri != null) "${state.fileSize} • ${state.pageCount} pages" else "Reduce file size for easy sharing",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                if (state.selectedUri != null) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Rename output file field
                    GlassCard(
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "Save as (Filename)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.customName,
                            onValueChange = { vm.setCustomName(it) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accentPrimary,
                                unfocusedBorderColor = colors.surfaceCardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compression level selection
                    GlassCard(
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "Compression Level",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        CompressPdfViewModel.CompressionLevel.values().forEach { level ->
                            val isSelected = state.compressionLevel == level
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { vm.setCompressionLevel(level) }
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accentPrimary else colors.surfaceCardBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = level.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.accentPrimary else colors.textPrimary
                                    )
                                    Text(
                                        text = level.estReduction,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.accentWarmAmber
                                    )
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { vm.setCompressionLevel(level) },
                                    colors = RadioButtonDefaults.colors(selectedColor = colors.accentPrimary)
                                )
                            }
                        }
                    }
                }

                // Error
                state.error?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassCard(
                        backgroundColor = colors.errorGlass,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(text = it, color = colors.errorText, style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Progress
                if (state.isProcessing) {
                    val (cur, total) = state.progress
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassProgress(
                        current = cur,
                        total = total,
                        label = "Compressing page $cur of $total",
                        onCancel = { vm.cancel() }
                    )
                }
            }

            // Bottom action
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                GlassButton(
                    text = if (state.isProcessing) "Compressing..." else "Compress PDF",
                    onClick = { vm.compressPdf() },
                    enabled = state.selectedUri != null && !state.isProcessing,
                    isLoading = state.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

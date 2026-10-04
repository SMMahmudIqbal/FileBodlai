package com.example.fileconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.GlassTheme
import com.example.fileconverter.viewmodel.OcrExtractViewModel

@Composable
fun OcrExtractScreen(onBack: () -> Unit) {
    val vm: OcrExtractViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val colors = GlassTheme.colors

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            val mime = context.contentResolver.getType(uri) ?: ""
            val isPdf = mime.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)
            vm.pickFile(uri, isPdf)
        }
    }

    if (state.resultUri != null) {
        ResultScreen(
            title = "Text Exported!",
            message = "Extracted text saved to Downloads/FileBodlai",
            resultUri = state.resultUri!!,
            mimeType = "text/plain",
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
            GlassTopBar(title = "Document OCR & Text Inspector", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drop zone
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable { filePicker.launch(arrayOf("image/*", "application/pdf")) },
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
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = colors.accentPill,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.fileUri != null) "Tap to change document" else "Select Document or Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extract text & analyze layout completely offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (state.fileUri != null) {
                    GlassFilePill(
                        name = state.fileName,
                        size = if (state.isPdf) "PDF Document" else "Image Document"
                    )
                }

                // Extracted Text View Card
                if (state.extractedText.isNotEmpty()) {
                    GlassCard(
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analysis & Extracted Text",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(state.extractedText))
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = CircleShape
                                ) {
                                    Text("Copy", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { vm.saveAsTextFile() },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = CircleShape
                                ) {
                                    Text("Save .txt", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.glassSurfaceSubtle)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = state.extractedText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary,
                                lineHeight = 18.sp
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
                if (state.isAnalyzing) {
                    val (cur, total) = state.progress
                    GlassProgress(
                        current = cur,
                        total = total,
                        label = if (total > 0) "Analyzing page $cur of $total" else "Scanning document lines..."
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
                    text = if (state.isAnalyzing) "Analyzing..." else "Run Document OCR",
                    onClick = { vm.runOcr() },
                    enabled = state.fileUri != null && !state.isAnalyzing,
                    isLoading = state.isAnalyzing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

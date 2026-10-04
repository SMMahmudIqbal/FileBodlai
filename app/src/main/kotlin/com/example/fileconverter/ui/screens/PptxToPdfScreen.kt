package com.example.fileconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.fileconverter.viewmodel.PptxToPdfViewModel

@Composable
fun PptxToPdfScreen(onBack: () -> Unit) {
    val vm: PptxToPdfViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val colors = GlassTheme.colors

    val pptxPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.grantUriPermission(it)
            vm.pickPptx(it)
        }
    }

    if (state.resultUri != null) {
        ResultScreen(
            title = "PDF Created!",
            message = "Slides converted and saved to Downloads/FileBodlai",
            resultUri = state.resultUri!!,
            mimeType = "application/pdf",
            context = context,
            onDone = {
                vm.clearResult()
                onBack()
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
            GlassTopBar(title = "PPTX → PDF", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Info Card
                GlassCard(
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = colors.glassSurfaceSubtle,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = colors.accentPill,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Preserves slide vector shapes, images, layouts, and typography.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Drop-Zone Pick Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable {
                            pptxPicker.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                    shape = RoundedCornerShape(26.dp),
                    elevation = 8.dp,
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, colors.glassBorderTopLeft, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = colors.accentPill,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (state.pptxUri != null) "Tap to change presentation" else "Select PPTX Presentation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Supports Microsoft PowerPoint (.pptx) slides",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // File selected pill
                if (state.pptxUri != null) {
                    GlassFilePill(
                        name = state.pptxName,
                        size = state.pptxSize
                    )
                }

                // Error Card
                state.error?.let {
                    GlassCard(
                        backgroundColor = colors.errorGlass,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = it,
                            color = colors.errorText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Progress Indicator
                if (state.isConverting) {
                    val (cur, total) = state.progress
                    GlassProgress(
                        current = cur,
                        total = total,
                        label = if (total > 0) "Rendering slide $cur of $total" else "Processing slides..."
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
                    text = if (state.isConverting) "Converting..." else "Convert to PDF",
                    onClick = { vm.convert() },
                    enabled = state.pptxUri != null && !state.isConverting,
                    isLoading = state.isConverting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun Context.grantUriPermission(uri: Uri) {
    try {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) {}
}

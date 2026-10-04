package com.example.fileconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.GlassTheme
import com.example.fileconverter.util.SensoryManager
import com.example.fileconverter.viewmodel.PdfOrganizeViewModel

@Composable
fun PdfOrganizeScreen(onBack: () -> Unit) {
    val vm: PdfOrganizeViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GlassTheme.colors

    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            vm.loadPdf(it)
        }
    }

    if (state.resultUri != null) {
        ResultScreen(
            title = "PDF Reorganized!",
            message = "New PDF saved to Downloads/FileBodlai",
            resultUri = state.resultUri!!,
            mimeType = "application/pdf",
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
            GlassTopBar(title = "Reorder & Delete Pages", onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // Drop zone
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .bounceClick { pdfPicker.launch(arrayOf("application/pdf")) },
                    shape = RoundedCornerShape(26.dp),
                    elevation = 6.dp,
                    contentPadding = PaddingValues(20.dp)
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
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = colors.accentPill,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (state.pdfUri != null) "Tap to change PDF" else "Select PDF to Edit Pages",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Preview thumbnails, reorder & delete",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.isLoadingPages) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.accentPill)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Rendering page thumbnails...", color = colors.textSecondary)
                        }
                    }
                } else if (state.pages.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pages (${state.pages.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Use arrows to reorder, × to delete",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        itemsIndexed(state.pages) { index, item ->
                            GlassCard(
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Thumbnail preview
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(androidx.compose.ui.graphics.Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (item.thumbnail != null) {
                                            Image(
                                                bitmap = item.thumbnail.asImageBitmap(),
                                                contentDescription = "Page ${index + 1}",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Page ${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    SensoryManager.tick(context, view)
                                                    vm.movePageUp(index)
                                                },
                                                modifier = Modifier.size(28.dp),
                                                enabled = index > 0
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.KeyboardArrowUp,
                                                    contentDescription = "Move Left/Up",
                                                    tint = if (index > 0) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    SensoryManager.tick(context, view)
                                                    vm.movePageDown(index)
                                                },
                                                modifier = Modifier.size(28.dp),
                                                enabled = index < state.pages.size - 1
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = "Move Right/Down",
                                                    tint = if (index < state.pages.size - 1) colors.textPrimary else colors.textSecondary.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    SensoryManager.heavyClick(context, view)
                                                    vm.deletePage(index)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Delete Page",
                                                    tint = colors.errorText,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Select a PDF to view and reorder pages",
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
                        label = "Rebuilding PDF: Page $cur of $total"
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
                    text = if (state.isProcessing) "Generating..." else "Save Reorganized PDF",
                    onClick = { vm.saveReorganizedPdf() },
                    enabled = state.pages.isNotEmpty() && !state.isProcessing,
                    isLoading = state.isProcessing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

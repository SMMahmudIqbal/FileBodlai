package com.example.fileconverter.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.converter.PageSizeMode
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.GlassTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.fileconverter.util.SensoryManager
import com.example.fileconverter.viewmodel.ImageToPdfViewModel

@Composable
fun ImageToPdfScreen(onBack: () -> Unit) {
    val vm: ImageToPdfViewModel = viewModel()
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

    if (state.resultUri != null) {
        ResultScreen(
            title = "PDF Created!",
            message = "Images compiled and saved to Downloads/FileBodlai",
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
            GlassTopBar(title = "Images → PDF", onBack = onBack)

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
                                text = "Select Images",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "JPG, PNG, WebP supported",
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
                        text = "Page Settings",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Page size chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PageSizeMode.values().forEach { mode ->
                            GlassChip(
                                selected = state.pageSizeMode == mode,
                                label = mode.name.replace("_", " "),
                                onClick = { vm.setPageSizeMode(mode) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Landscape checkbox
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.landscape,
                            onCheckedChange = { vm.setLandscape(it) },
                            colors = CheckboxDefaults.colors(checkedColor = colors.accentPill)
                        )
                        Text(
                            text = "Landscape Orientation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }

                    // Margin Slider
                    Text(
                        text = "Margin: ${state.marginPts} pt",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Slider(
                        value = state.marginPts.toFloat(),
                        onValueChange = { newVal ->
                            val oldVal = state.marginPts
                            val newInt = newVal.toInt()
                            if (newInt != oldVal) {
                                SensoryManager.tick(context, view)
                            }
                            vm.setMargin(newInt)
                        },
                        valueRange = 0f..60f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accentPill,
                            activeTrackColor = colors.accentPill
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Image list
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
                                contentPadding = PaddingValues(8.dp)
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
                                        onClick = { vm.moveUp(index) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Filled.KeyboardArrowUp, "Up", tint = colors.textSecondary)
                                    }
                                    IconButton(
                                        onClick = { vm.moveDown(index) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Filled.KeyboardArrowDown, "Down", tint = colors.textSecondary)
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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No images chosen yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary.copy(alpha = 0.6f)
                        )
                    }
                }

                // Error
                state.error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassProgress(
                        current = cur,
                        total = total,
                        label = "Adding image $cur of $total"
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
                    text = if (state.isConverting) "Converting..." else "Create PDF",
                    onClick = { vm.convert() },
                    enabled = state.images.isNotEmpty() && !state.isConverting,
                    isLoading = state.isConverting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

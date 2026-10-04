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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fileconverter.ui.components.*
import com.example.fileconverter.ui.theme.GlassTheme
import com.example.fileconverter.viewmodel.ImageToImageViewModel

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.example.fileconverter.util.SensoryManager

@Composable
fun ImageToImageScreen(onBack: () -> Unit) {
    val vm: ImageToImageViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val colors = GlassTheme.colors

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            vm.pickImage(it)
        }
    }

    val resultMime = when (state.targetFormat) {
        Bitmap.CompressFormat.PNG -> "image/png"
        else -> "image/jpeg"
    }

    if (state.resultUri != null) {
        ResultScreen(
            title = "Image Converted!",
            message = "New image saved to Downloads/FileBodlai",
            resultUri = state.resultUri!!,
            mimeType = resultMime,
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
            GlassTopBar(title = "Image Convert", onBack = onBack)

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
                        .clickable { imagePicker.launch(arrayOf("image/jpeg", "image/png", "image/webp", "image/*")) },
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
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = colors.accentPill,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.imageUri != null) "Tap to change image" else "Select Image to Transform",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Supports JPEG, PNG, WebP",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (state.imageUri != null) {
                    GlassFilePill(
                        name = state.imageName,
                        size = state.imageSize
                    )

                    // Options Card
                    GlassCard(
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "Conversion Options",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Target format
                        Text("Target Format:", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            GlassChip(
                                selected = state.targetFormat == Bitmap.CompressFormat.JPEG,
                                label = "JPG",
                                onClick = { vm.setFormat(Bitmap.CompressFormat.JPEG) }
                            )
                            GlassChip(
                                selected = state.targetFormat == Bitmap.CompressFormat.PNG,
                                label = "PNG",
                                onClick = { vm.setFormat(Bitmap.CompressFormat.PNG) }
                            )
                            @Suppress("DEPRECATION")
                            GlassChip(
                                selected = state.targetFormat == Bitmap.CompressFormat.WEBP,
                                label = "WebP",
                                onClick = { @Suppress("DEPRECATION") vm.setFormat(Bitmap.CompressFormat.WEBP) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quality Slider
                        Text("Quality: ${state.quality}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Optional Resize
                        Text("Resize (optional):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.resizeWidth,
                                onValueChange = { vm.setResizeWidth(it) },
                                label = { Text("Width px") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentPill,
                                    unfocusedBorderColor = colors.glassBorderTopLeft
                                )
                            )
                            OutlinedTextField(
                                value = state.resizeHeight,
                                onValueChange = { vm.setResizeHeight(it) },
                                label = { Text("Height px") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentPill,
                                    unfocusedBorderColor = colors.glassBorderTopLeft
                                )
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
                    GlassProgress(
                        current = 1,
                        total = 1,
                        label = "Processing image transformation..."
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
                    text = if (state.isConverting) "Converting..." else "Convert Image",
                    onClick = { vm.convert() },
                    enabled = state.imageUri != null && !state.isConverting,
                    isLoading = state.isConverting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

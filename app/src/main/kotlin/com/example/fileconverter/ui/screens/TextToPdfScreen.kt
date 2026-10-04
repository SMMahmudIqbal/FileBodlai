package com.example.fileconverter.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Edit
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
import com.example.fileconverter.viewmodel.TextToPdfViewModel

@Composable
fun TextToPdfScreen(onBack: () -> Unit) {
    val vm: TextToPdfViewModel = viewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val colors = GlassTheme.colors

    val textPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            vm.pickText(it)
        }
    }

    if (state.resultUri != null) {
        ResultScreen(
            title = "PDF Created!",
            message = "Text formatted and saved to Downloads/FileBodlai",
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
            GlassTopBar(title = "Text → PDF", onBack = onBack)

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
                        .clickable { textPicker.launch(arrayOf("text/plain", "text/*")) },
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
                            text = if (state.textUri != null) "Tap to change text file" else "Select .txt Document",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Auto word-wraps & paginates cleanly",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                if (state.textUri != null) {
                    GlassFilePill(
                        name = state.textName,
                        size = state.textSize
                    )
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
                        label = "Writing page $cur of $total"
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
                    enabled = state.textUri != null && !state.isConverting,
                    isLoading = state.isConverting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

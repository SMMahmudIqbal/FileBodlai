package com.example.fileconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileconverter.ui.components.GlassBackground
import com.example.fileconverter.ui.components.GlassButton
import com.example.fileconverter.ui.components.GlassCard
import com.example.fileconverter.ui.theme.BodlaiTheme

@Composable
fun ResultScreen(
    title: String,
    message: String,
    resultUri: Uri,
    mimeType: String,
    context: Context,
    onDone: () -> Unit,
    onViewInApp: (() -> Unit)? = null
) {
    val colors = BodlaiTheme.colors

    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
        com.example.fileconverter.util.SensoryManager.success(context)
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkScale"
    )

    var showQuickLook by remember { mutableStateOf(false) }

    if (showQuickLook) {
        QuickLookScreen(
            uri = resultUri,
            mimeType = mimeType,
            title = title,
            onBack = { showQuickLook = false }
        )
        return
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = colors.surfaceCard,
                elevation = 8.dp,
                contentPadding = PaddingValues(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Warm amber glowing success circle from reference design
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceCardSubtle)
                            .border(1.5.dp, colors.accentWarmAmber.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(colors.accentWarmAmber.copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(size.width / 2, size.height / 2),
                                    radius = size.width * 0.6f
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Success",
                            tint = colors.accentWarmAmber,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = title,
                        style = BodlaiTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    val canViewInApp = mimeType == "application/pdf" ||
                        mimeType.startsWith("image/") ||
                        mimeType.contains("pdf", ignoreCase = true)

                    if (canViewInApp) {
                        GlassButton(
                            text = "Quick Look in File Bodlai",
                            onClick = {
                                if (onViewInApp != null) {
                                    onViewInApp()
                                } else {
                                    showQuickLook = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Primary solid button (or secondary if in-app quick look is available)
                    GlassButton(
                        text = "Open in External App",
                        onClick = { openFile(context, resultUri, mimeType) },
                        modifier = Modifier.fillMaxWidth(),
                        isSecondary = canViewInApp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary outlined button
                    GlassButton(
                        text = "Share File",
                        onClick = { shareFile(context, resultUri, mimeType) },
                        modifier = Modifier.fillMaxWidth(),
                        isSecondary = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    androidx.compose.material3.TextButton(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Convert Another",
                            color = colors.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun openFile(context: Context, uri: Uri, mimeType: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun shareFile(context: Context, uri: Uri, mimeType: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (_: Exception) {}
}

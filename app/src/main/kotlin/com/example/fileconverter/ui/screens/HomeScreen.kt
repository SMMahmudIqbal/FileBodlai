package com.example.fileconverter.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileconverter.ui.components.GlassBackground
import com.example.fileconverter.ui.components.GlassCard
import com.example.fileconverter.ui.components.bounceClick
import com.example.fileconverter.ui.theme.*
import com.example.fileconverter.util.HistoryManager
import com.example.fileconverter.util.RecentFile
import com.example.fileconverter.util.SensoryManager
import java.text.SimpleDateFormat
import java.util.*

data class ConverterCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val category: String = "All",
    val badge: String = "",
    val indexNum: String = "01"
)

val converterCards = listOf(
    ConverterCard("Compress PDF", "Downsample & shrink file size", Icons.Filled.ThumbUp, "compress_pdf", category = "PDF", badge = "shrink", indexNum = "01"),
    ConverterCard("Merge PDFs", "Combine documents into one", Icons.Filled.Add, "merge_pdf", category = "PDF", badge = "+ pdf", indexNum = "02"),
    ConverterCard("Organize PDF", "Reorder, rotate & delete pages", Icons.AutoMirrored.Filled.List, "organize_pdf", category = "PDF", badge = "pages", indexNum = "03"),
    ConverterCard("PDF → PPTX", "Convert to presentation slides", Icons.Filled.PlayArrow, "pdf_to_pptx", category = "PDF", badge = ".pptx", indexNum = "04"),
    ConverterCard("PPTX → PDF", "Convert slides to PDF document", Icons.Filled.PlayArrow, "pptx_to_pdf", category = "PDF", badge = ".pdf", indexNum = "05"),
    ConverterCard("PDF → Images", "Export pages as high-res images", Icons.Filled.Star, "pdf_to_images", category = "PDF", badge = ".png", indexNum = "06"),
    ConverterCard("Images → PDF", "Bundle pictures to PDF file", Icons.Filled.Star, "images_to_pdf", category = "Image", badge = ".pdf", indexNum = "07"),
    ConverterCard("Batch Resize", "Bulk size & dimension optimizer", Icons.Filled.ThumbUp, "batch_compress", category = "Image", badge = "batch", indexNum = "08"),
    ConverterCard("Image Convert", "Convert JPG, PNG, and WebP", Icons.Filled.Settings, "image_to_image", category = "Image", badge = "format", indexNum = "09"),
    ConverterCard("Word → PDF", "Convert Word document to PDF", Icons.Filled.Edit, "docx_to_pdf", category = "Document", badge = ".pdf", indexNum = "10"),
    ConverterCard("Word → PPTX", "Transform document into slides", Icons.Filled.PlayArrow, "docx_to_pptx", category = "Document", badge = ".pptx", indexNum = "11"),
    ConverterCard("Text → PDF", "Convert plain text to document", Icons.Filled.Edit, "text_to_pdf", category = "Document", badge = ".txt", indexNum = "12"),
    ConverterCard("OCR Inspector", "Extract text & OCR metadata", Icons.Filled.Info, "ocr_extract", category = "Document", badge = "ocr", indexNum = "13")
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = BodlaiTheme.colors
    var isVisible by remember { mutableStateOf(false) }
    var recents by remember { mutableStateOf(emptyList<RecentFile>()) }

    val currentThemePreset = LocalThemePreset.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFirstRunAgreement by remember {
        mutableStateOf(!com.example.fileconverter.util.AppPolicyManager.isAgreementAccepted(context))
    }

    LaunchedEffect(Unit) {
        isVisible = true
        recents = HistoryManager.getRecents(context)
    }

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Living Brand Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "File Bodlai",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.8).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Subtle animated pulse status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.accentPrimary.copy(alpha = 0.15f))
                                .border(1.dp, colors.accentPrimary.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentPrimary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Next-gen private file processing studio",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                // Theme Switcher Button with animated gradient accent ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceCardSubtle)
                        .border(
                            width = if (colors.isNeoBrutalist) 2.dp else 1.2.dp,
                            brush = if (colors.isNeoBrutalist) androidx.compose.ui.graphics.SolidColor(Color.Black)
                            else Brush.sweepGradient(
                                listOf(colors.accentPrimary, colors.accentWarmAmber, colors.accentPrimary)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .bounceClick { showThemeDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Theme Studio",
                        tint = colors.accentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // High-Aesthetic Ultra-Compact Command & Telemetry Capsule
            val isDark = colors.isDark
            val isNeo = colors.isNeoBrutalist
            val isSwiss = colors.isSwissEditorial

            val totalBytesSaved by com.example.fileconverter.util.ImpactStatsManager.bytesSavedState
            val totalFilesProcessed by com.example.fileconverter.util.ImpactStatsManager.filesProcessedState
            val formattedStorage = remember(totalBytesSaved) {
                com.example.fileconverter.util.ImpactStatsManager.formatBytes(totalBytesSaved)
            }

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (isNeo) {
                            drawRoundRect(
                                color = Color.Black,
                                topLeft = Offset(2.5.dp.toPx(), 2.5.dp.toPx()),
                                size = this.size,
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )
                        } else {
                            drawRoundRect(
                                color = colors.accentPrimary.copy(alpha = if (isDark) 0.16f else 0.09f),
                                topLeft = Offset(0f, 2.dp.toPx()),
                                size = this.size,
                                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                            )
                        }
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .bounceClick { onNavigate("batch_compress") },
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colors.surfaceCardSubtle,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Quick Optimizer Action
                    Row(
                        modifier = Modifier.weight(1.15f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(if (isNeo) RoundedCornerShape(8.dp) else CircleShape)
                                .background(
                                    if (isNeo) colors.accentPrimary
                                    else colors.accentPrimary.copy(alpha = 0.20f)
                                )
                                .then(
                                    if (isNeo) Modifier.border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (isNeo) Color.Black else colors.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Column {
                            Text(
                                text = if (isSwiss) "00 / OPTIMIZER" else "Quick Optimizer",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "One-tap batch compressor",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    // Hairline vertical separator
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .width(1.dp)
                            .height(26.dp)
                            .background(colors.surfaceCardBorder.copy(alpha = 0.5f))
                    )

                    // Right: Live On-Device Telemetry Metrics & Launch Action
                    Row(
                        modifier = Modifier.weight(0.95f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formattedStorage,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.accentWarmAmber,
                                    fontSize = 12.sp,
                                    letterSpacing = (-0.3).sp
                                )
                                Text(
                                    text = " saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    fontSize = 9.5.sp
                                )
                            }
                            Text(
                                text = "$totalFilesProcessed ${if (totalFilesProcessed == 1) "file" else "files"} done",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted,
                                fontSize = 9.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(if (isNeo) Color.Black else colors.accentPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Launch",
                                tint = if (isNeo) colors.accentPrimary else colors.textOnAccent,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recent conversions strip (if any)
            if (recents.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Conversions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            HistoryManager.clearAll(context)
                            recents = emptyList()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recents) { item ->
                        RecentFileCard(
                            file = item,
                            context = context,
                            onDelete = {
                                HistoryManager.removeRecent(context, item.id)
                                recents = HistoryManager.getRecents(context)
                            },
                            onOpen = {
                                if (item.mimeType == "application/pdf" || item.mimeType.startsWith("image/")) {
                                    onNavigate("quick_look?uri=" + Uri.encode(item.uriString) + "&mime=" + Uri.encode(item.mimeType))
                                } else {
                                    try {
                                        val uri = Uri.parse(item.uriString)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, item.mimeType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            var selectedCategory by remember { mutableStateOf("All") }
            val filteredCards = remember(selectedCategory) {
                if (selectedCategory == "All") converterCards
                else converterCards.filter { it.category == selectedCategory }
            }

            // Grid of conversion tools header
            Text(
                text = "Converters & Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Dedicated horizontal category filter row with even, fixed-padding pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("All", "PDF", "Image", "Document").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) colors.accentPrimary
                                else if (isNeo) Color.White
                                else colors.surfaceCardSubtle
                            )
                            .border(
                                width = if (isNeo) 2.dp else 1.dp,
                                color = if (isNeo) Color.Black else if (isSelected) Color.White.copy(alpha = 0.2f) else colors.surfaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                SensoryManager.tick(context, view)
                                selectedCategory = cat
                            }
                            .padding(vertical = 6.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.textOnAccent else colors.textSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Main content area with smooth gradient scrim & floating bottom bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Scrollable Grid of conversion tools
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp)
                ) {
                    items(filteredCards.size) { index ->
                        val card = filteredCards[index]
                        NocturnalConverterCard(
                            card = card,
                            onClick = { onNavigate(card.route) }
                        )
                    }
                }

                // Soft dark gradient fade scrim at the bottom to dissolve cards before the bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.bgBottom.copy(alpha = 0.85f),
                                    colors.bgBottom
                                )
                            )
                        )
                )

                // Bottom floating navigation pill (100% offline badge removed)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBottomPill(
                        onNavigate = onNavigate,
                        onOpenTheme = { showThemeDialog = true }
                    )
                }
            }
        }

        // Theme & Settings Hub Dialog
        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentPreset = currentThemePreset.value,
                onSelectPreset = { preset ->
                    currentThemePreset.value = preset
                    ThemeManager.savePreset(context, preset)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        // First Run User Agreement & Privacy Consent
        if (showFirstRunAgreement) {
            FirstRunAgreementDialog(
                onAccept = {
                    SensoryManager.success(context)
                    com.example.fileconverter.util.AppPolicyManager.setAgreementAccepted(context, true)
                    showFirstRunAgreement = false
                }
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentPreset: ThemePreset,
    onSelectPreset: (ThemePreset) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = BodlaiTheme.colors
    var selectedTab by remember { mutableStateOf(0) }
    var activeCategory by remember { mutableStateOf("All") }
    var previewPreset by remember { mutableStateOf(currentPreset) }

    val filteredPresets = remember(activeCategory) {
        when (activeCategory) {
            "Dark" -> ThemePreset.values().filter { it.styleCategory == "Dark" }
            "Day" -> ThemePreset.values().filter { it.styleCategory == "Day" }
            "Art" -> ThemePreset.values().filter { it.styleCategory == "Art" }
            else -> ThemePreset.values().toList()
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            contentPadding = PaddingValues(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (selectedTab) {
                            1 -> "Legal & Privacy"
                            2 -> "About File Bodlai"
                            else -> "Theme Studio"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (selectedTab) {
                            1 -> "100% offline & open-source terms"
                            2 -> "Air-gapped private document studio"
                            else -> "19 bespoke handcrafted palettes"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceCardSubtle)
                        .bounceClick(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Segmented Pills: [Themes] [Legal & Privacy] [About]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceCardSubtle)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Themes", "Legal & Privacy", "About").forEachIndexed { idx, title ->
                    val isTabSelected = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isTabSelected) colors.accentPrimary else Color.Transparent)
                            .clickable {
                                SensoryManager.tick(context, view)
                                selectedTab = idx
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isTabSelected) colors.textOnAccent else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {

            // Interactive Live Theme Preview Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(previewPreset.bgTop, previewPreset.bgMid, previewPreset.bgBottom)
                        )
                    )
                    .border(
                        1.5.dp,
                        if (previewPreset.isNeoBrutalist) Color.Black else previewPreset.primaryAccent.copy(alpha = 0.5f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = previewPreset.title,
                                fontWeight = FontWeight.Bold,
                                color = if (previewPreset.isDark) Color.White else Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(previewPreset.primaryAccent.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = previewPreset.styleCategory.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = previewPreset.primaryAccent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (previewPreset == currentPreset) "Currently Active" else "Tap below to activate",
                            color = if (previewPreset.isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF475569),
                            fontSize = 11.5.sp
                        )
                    }

                    // Mini sample button in preview
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(previewPreset.primaryAccent)
                            .then(
                                if (previewPreset.isNeoBrutalist) Modifier.border(1.5.dp, Color.Black, RoundedCornerShape(10.dp))
                                else Modifier
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Sample",
                            fontWeight = FontWeight.Bold,
                            color = if (previewPreset.isNeoBrutalist) Color.Black else Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Chips (All, Dark, Day, Art)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Dark", "Day", "Art").forEach { cat ->
                    val isSelected = activeCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colors.accentPrimary else colors.surfaceCardSubtle)
                            .clickable { activeCategory = cat }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.textOnAccent else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-column visual palette swatch grid with title & glowing selection indicator
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                items(filteredPresets.size) { idx ->
                    val preset = filteredPresets[idx]
                    val isSelected = preset == currentPreset
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceCardSubtle)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.accentPrimary else colors.surfaceCardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .bounceClick {
                                previewPreset = preset
                                onSelectPreset(preset)
                            }
                            .padding(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Swatch Gradient Block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(preset.bgTop, preset.primaryAccent, preset.warmAccent)
                                        )
                                    )
                                    .border(
                                        width = if (preset.isNeoBrutalist) 1.5.dp else 0.5.dp,
                                        color = if (preset.isNeoBrutalist) Color.Black else Color.White.copy(alpha = 0.35f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.55f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.accentPrimary else colors.textPrimary,
                                maxLines = 1,
                                fontSize = 10.5.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sensory Studio Panel (Tactile Haptics & Mechanical Acoustic Clicks)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceCardSubtle,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = colors.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Tactile Haptics",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Micro-vibrations on touch & dials",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Switch(
                            checked = SensoryManager.hapticsEnabled.value,
                            onCheckedChange = { SensoryManager.setHapticsEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentPrimary,
                                checkedTrackColor = colors.accentPrimary.copy(alpha = 0.35f),
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.surfaceCard
                            ),
                            modifier = Modifier.scale(0.75f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = colors.accentWarmAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Mechanical Audio FX",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Acoustic typewriter click on interactions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Switch(
                            checked = SensoryManager.soundEnabled.value,
                            onCheckedChange = { SensoryManager.setSoundEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentWarmAmber,
                                checkedTrackColor = colors.accentWarmAmber.copy(alpha = 0.35f),
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.surfaceCard
                            ),
                            modifier = Modifier.scale(0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Developer Credit
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceCardSubtle,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "File Bodlai Studio",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "By S. M. Mahmud Iqbal",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.accentPrimary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .bounceClick(onClick = onDismiss)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Apply", color = colors.accentPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            } else if (selectedTab == 1) {
                // TAB 1: LEGAL & PRIVACY
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceCardSubtle,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = colors.accentPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "100% Offline Privacy Policy",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Zero Network Code: File Bodlai does not request android.permission.INTERNET.\n• On-Device Processing: All PDF, PPTX, Image & OCR operations execute strictly in your phone's memory.\n• Scoped Storage: Access is restricted exclusively to files you pick. Outputs save to Downloads/FileBodlai.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceCardSubtle,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.accentWarmAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "User Agreement & Ownership",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• 100% User Ownership: You retain complete copyright and rights to all processed documents.\n• MIT Open Source License: Distributed without warranties under the MIT License for personal and commercial productivity.\n• Zero Third-Party Tracking: No advertising SDKs, crash analytics, or user profiling.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surfaceCardSubtle,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Open Source Acknowledgements",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• PDFBox-Android (Apache License 2.0)\n• Jetpack Compose & Material 3 (Apache License 2.0)\n• Plus Jakarta Sans Font (OFL 1.1)",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.accentPrimary.copy(alpha = 0.15f))
                            .border(1.dp, colors.accentPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .bounceClick {
                                try {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FileBodlai/FileBodlai"))
                                    context.startActivity(browserIntent)
                                } catch (e: Exception) { }
                            }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Full Legal Documents on GitHub",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.accentPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                // TAB 2: ABOUT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.accentPrimary.copy(alpha = 0.2f))
                            .border(1.5.dp, colors.accentPrimary, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "File Bodlai (ফাইল বদলাই)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Version 1.0.0 • Public Release",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Private on-device document converter and smart compressor. Built with Kotlin 2.0, Jetpack Compose & Material 3.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 11.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            SensoryManager.click(context, view)
                            try {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out File Bodlai (ফাইল বদলাই) — a 100% offline, privacy-first file converter and compressor for Android: https://github.com/FileBodlai/FileBodlai"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share File Bodlai"))
                            } catch (e: Exception) { }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentPrimary,
                            contentColor = colors.textOnAccent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share File Bodlai", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            SensoryManager.click(context, view)
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FileBodlai/FileBodlai"))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) { }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, colors.accentPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Star on GitHub", color = colors.accentPrimary, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    Text(
                        text = "Architected by S. M. Mahmud Iqbal",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FirstRunAgreementDialog(
    onAccept: () -> Unit
) {
    val colors = BodlaiTheme.colors

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* Require explicit confirmation on first launch */ },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 14.dp),
            shape = RoundedCornerShape(26.dp),
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary.copy(alpha = 0.2f))
                        .border(1.5.dp, colors.accentPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = colors.accentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Welcome to File Bodlai",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 19.sp
                )
                Text(
                    text = "ফাইল বদলাই • 100% Private File Studio",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceCardSubtle)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🔒", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Zero Data Transmission",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Your documents and photos never leave this device. Zero internet permission.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Text("🚫", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Zero Ads & Telemetry",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "No third-party trackers, no cookies, no user monitoring. Free & open source.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Text("⚖️", fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "User Agreement & License",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Provided under the MIT License for local personal and professional use.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "By continuing, you agree to our User Agreement and offline processing terms. You can review them anytime in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentPrimary,
                        contentColor = colors.textOnAccent
                    )
                ) {
                    Text(
                        text = "Agree & Get Started",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NocturnalConverterCard(
    card: ConverterCard,
    onClick: () -> Unit
) {
    val colors = BodlaiTheme.colors
    val context = LocalContext.current
    val view = LocalView.current
    val isDark = colors.isDark
    val isNeo = colors.isNeoBrutalist
    val isSwiss = colors.isSwissEditorial
    val isMuseum = colors.isMuseumPop
    val isLiquid = colors.isLiquidGlass

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            SensoryManager.click(context, view)
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = when {
            isNeo -> RoundedCornerShape(18.dp)
            isSwiss -> RoundedCornerShape(16.dp)
            isMuseum -> RoundedCornerShape(22.dp)
            else -> RoundedCornerShape(24.dp)
        },
        backgroundColor = colors.surfaceCard,
        contentPadding = PaddingValues(15.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP ROW: Icon Well + Format Badge / Serial Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Well with bespoke theme effects
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .drawBehind {
                            when {
                                isNeo -> {
                                    drawRoundRect(
                                        color = Color.Black,
                                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                        size = this.size,
                                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                    )
                                }
                                isLiquid -> {
                                    drawRoundRect(
                                        color = if (isDark) Color(0x6638BDF8) else Color(0x330284C7),
                                        topLeft = Offset(0f, 3.dp.toPx()),
                                        size = this.size,
                                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                                    )
                                }
                                isSwiss -> {
                                    drawCircle(
                                        color = colors.accentPrimary.copy(alpha = 0.15f),
                                        radius = this.size.minDimension * 0.58f,
                                        center = this.center
                                    )
                                }
                                else -> {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                colors.accentPrimary.copy(alpha = if (isDark) 0.35f else 0.22f),
                                                Color.Transparent
                                            ),
                                            center = this.center,
                                            radius = this.size.minDimension * 0.9f
                                        ),
                                        radius = this.size.minDimension * 0.9f,
                                        center = this.center
                                    )
                                }
                            }
                        }
                        .clip(
                            when {
                                isNeo -> RoundedCornerShape(12.dp)
                                isLiquid -> RoundedCornerShape(14.dp)
                                isSwiss -> CircleShape
                                isMuseum -> RoundedCornerShape(12.dp)
                                else -> CircleShape
                            }
                        )
                        .background(
                            when {
                                isNeo -> colors.accentPrimary
                                isLiquid -> if (isDark) Color(0xFF0F3659).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f)
                                isSwiss -> colors.accentPrimary
                                isMuseum -> if (isDark) Color(0xFF262626) else colors.accentPrimary
                                isDark -> Color.Black.copy(alpha = 0.40f)
                                else -> colors.accentPrimary.copy(alpha = 0.12f)
                            }
                        )
                        .border(
                            width = if (isNeo) 2.dp else 1.dp,
                            color = when {
                                isNeo -> Color.Black
                                isLiquid -> if (isDark) Color(0xFF7DD3FC).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.9f)
                                isSwiss -> colors.accentPrimary
                                isMuseum -> if (isDark) Color.White.copy(alpha = 0.15f) else colors.accentPrimary
                                isDark -> Color.White.copy(alpha = 0.15f)
                                else -> colors.accentPrimary.copy(alpha = 0.30f)
                            },
                            shape = when {
                                isNeo -> RoundedCornerShape(12.dp)
                                isLiquid -> RoundedCornerShape(14.dp)
                                isSwiss -> CircleShape
                                isMuseum -> RoundedCornerShape(12.dp)
                                else -> CircleShape
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = card.icon,
                        contentDescription = card.title,
                        tint = when {
                            isNeo -> Color.Black
                            isSwiss -> Color.White
                            isMuseum -> if (isDark) colors.accentPrimary else Color.White
                            isLiquid -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            else -> colors.accentPrimary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Format / Serial Badge
                if (isSwiss) {
                    // Swiss Bauhaus serial index stamp (Image 1 style)
                    Text(
                        text = "№ ${card.indexNum}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentPrimary,
                        letterSpacing = 0.5.sp,
                        fontSize = 11.sp
                    )
                } else if (card.badge.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isNeo -> Color.Black
                                    isMuseum -> if (isDark) colors.accentPrimary.copy(alpha = 0.20f) else colors.accentPrimary
                                    else -> colors.tagBg
                                }
                            )
                            .then(
                                if (isNeo) Modifier.border(1.dp, colors.accentPrimary, RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.badge.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isNeo -> colors.accentPrimary
                                isMuseum -> if (isDark) colors.accentPrimary else Color.White
                                else -> colors.tagText
                            },
                            fontSize = 9.sp,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            // BOTTOM: Title + Subtitle + Action Arrow
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

@Composable
fun RecentFileCard(
    file: RecentFile,
    context: Context,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    val colors = BodlaiTheme.colors
    val dateStr = remember(file.timestamp) {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(file.timestamp))
    }

    GlassCard(
        modifier = Modifier
            .width(170.dp)
            .bounceClick(onClick = onOpen),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Delete",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = file.fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
fun FloatingBottomPill(
    onNavigate: (String) -> Unit,
    onOpenTheme: () -> Unit
) {
    val colors = BodlaiTheme.colors
    val isDark = colors.isDark
    val isNeo = colors.isNeoBrutalist

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .drawBehind {
                    if (isNeo) {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                            size = this.size,
                            cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx())
                        )
                    } else {
                        // Soft bottom navigation bar ambient glow
                        drawRoundRect(
                            color = if (isDark) colors.shadowColor else Color(0x22000000),
                            topLeft = Offset(0f, 6.dp.toPx()),
                            size = this.size,
                            cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx())
                        )
                    }
                }
                .clip(RoundedCornerShape(32.dp))
                .background(
                    when {
                        isNeo -> Color.White
                        isDark -> Color(0xFF231B2E).copy(alpha = 0.95f)
                        else -> Color.White.copy(alpha = 0.95f)
                    }
                )
                .border(
                    width = if (isNeo) 2.5.dp else 1.dp,
                    color = when {
                        isNeo -> Color.Black
                        isDark -> Color.White.copy(alpha = 0.08f)
                        else -> Color(0xFFCBD5E1).copy(alpha = 0.70f)
                    },
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 16.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab (Active with accent glow or Neo-Brutalist border)
            Row(
                modifier = Modifier
                    .drawBehind {
                        if (isNeo) {
                            drawRoundRect(
                                color = Color.Black,
                                topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                                size = this.size,
                                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                            )
                        } else {
                            // Neon/accent glow behind active tab
                            drawRoundRect(
                                color = colors.accentPrimary.copy(alpha = if (isDark) 0.35f else 0.25f),
                                topLeft = Offset(0f, 2.dp.toPx()),
                                size = this.size,
                                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                            )
                        }
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.accentPrimary)
                    .then(
                        if (isNeo) Modifier.border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = colors.textOnAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textOnAccent
                )
            }

            // Settings & Themes Tab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .bounceClick(onClick = onOpenTheme)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
            }
        }
    }
}

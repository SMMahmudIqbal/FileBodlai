package com.example.fileconverter.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileconverter.ui.theme.BodlaiTheme
import com.example.fileconverter.util.SensoryManager

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = BodlaiTheme.colors
    val isDark = colors.isDark
    val isLiquid = colors.isLiquidGlass
    val isSwiss = colors.isSwissEditorial
    val isMuseum = colors.isMuseumPop
    val isNeo = colors.isNeoBrutalist
    val isZenith = colors.isZenithAurora
    val isBionic = colors.isBionicJade

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundGradient)
    ) {
        // Atmospheric ambient glow spots and artistic canvas graphics
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when {
                isZenith -> {
                    // Top-right radiant orchid violet aurora orb (Image 2 style)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFC084FC).copy(alpha = 0.28f), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.12f),
                            radius = w * 0.85f
                        ),
                        radius = w * 0.85f,
                        center = Offset(w * 0.85f, h * 0.12f)
                    )
                    // Bottom-left bioluminescent aurora spring green orb (Image 2 style)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF34D399).copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(w * 0.15f, h * 0.78f),
                            radius = w * 0.75f
                        ),
                        radius = w * 0.75f,
                        center = Offset(w * 0.15f, h * 0.78f)
                    )
                    // Center warm golden dusk reflection
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(w * 0.45f, h * 0.45f),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(w * 0.45f, h * 0.45f)
                    )
                }
                isBionic -> {
                    // Top-right bionic electric jade contour orb (Image 1 style)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2DD4BF).copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(w * 0.82f, h * 0.10f),
                            radius = w * 0.80f
                        ),
                        radius = w * 0.80f,
                        center = Offset(w * 0.82f, h * 0.10f)
                    )
                    // Center-left solar butter warm halo (Image 1 style)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFDE047).copy(alpha = 0.16f), Color.Transparent),
                            center = Offset(w * 0.10f, h * 0.55f),
                            radius = w * 0.65f
                        ),
                        radius = w * 0.65f,
                        center = Offset(w * 0.10f, h * 0.55f)
                    )
                }
                isLiquid -> {
                    if (isDark) {
                        // Fluid cyan caustic orb (center-top)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.28f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.08f),
                                radius = w * 0.90f
                            ),
                            radius = w * 0.90f,
                            center = Offset(w * 0.5f, h * 0.08f)
                        )

                        // Shimmering oceanic indigo bloom (right side)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF818CF8).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(w * 0.85f, h * 0.40f),
                                radius = w * 0.65f
                            ),
                            radius = w * 0.65f,
                            center = Offset(w * 0.85f, h * 0.40f)
                        )

                        // Deep sea emerald caustic pool (bottom-left)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.20f), Color.Transparent),
                                center = Offset(w * 0.15f, h * 0.80f),
                                radius = w * 0.70f
                            ),
                            radius = w * 0.70f,
                            center = Offset(w * 0.15f, h * 0.80f)
                        )
                    } else {
                        // Frosted Ice Light Glass (Image 2 style)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(w * 0.35f, h * 0.10f),
                                radius = w * 0.85f
                            ),
                            radius = w * 0.85f,
                            center = Offset(w * 0.35f, h * 0.10f)
                        )

                        // Prismatic warm peach caustic glow (right-mid)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFB923C).copy(alpha = 0.18f), Color.Transparent),
                                center = Offset(w * 0.85f, h * 0.35f),
                                radius = w * 0.65f
                            ),
                            radius = w * 0.65f,
                            center = Offset(w * 0.85f, h * 0.35f)
                        )

                        // Soft iridescent violet caustic (bottom-left)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF818CF8).copy(alpha = 0.16f), Color.Transparent),
                                center = Offset(w * 0.20f, h * 0.75f),
                                radius = w * 0.70f
                            ),
                            radius = w * 0.70f,
                            center = Offset(w * 0.20f, h * 0.75f)
                        )
                    }
                }
                isSwiss -> {
                    // Swiss / Bauhaus architectural geometry watermark (Image 1 style)
                    drawCircle(
                        color = colors.accentPrimary.copy(alpha = 0.08f),
                        radius = w * 0.48f,
                        center = Offset(w * 0.95f, h * 0.12f)
                    )
                    drawCircle(
                        color = colors.accentWarmAmber.copy(alpha = 0.06f),
                        radius = w * 0.35f,
                        center = Offset(w * 0.05f, h * 0.85f)
                    )
                }
                isMuseum -> {
                    // High-Fashion Museum fluorescent aura (Image 3 style)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accentPrimary.copy(alpha = if (isDark) 0.22f else 0.14f), Color.Transparent),
                            center = Offset(w * 0.80f, h * 0.15f),
                            radius = w * 0.75f
                        ),
                        radius = w * 0.75f,
                        center = Offset(w * 0.80f, h * 0.15f)
                    )
                }
                isDark -> {
                    // Nocturnal luminous glow spots
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accentPrimary.copy(alpha = 0.20f), Color.Transparent),
                            center = Offset(w * 0.5f, h * 0.12f),
                            radius = w * 0.85f
                        ),
                        radius = w * 0.85f,
                        center = Offset(w * 0.5f, h * 0.12f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accentWarmAmber.copy(alpha = 0.18f), Color.Transparent),
                            center = Offset(w * 0.8f, h * 0.20f),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(w * 0.8f, h * 0.20f)
                    )
                }
                else -> {
                    // Day luminous sunlit aura & delicate color wash
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accentPrimary.copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(w * 0.3f, h * 0.10f),
                            radius = w * 0.85f
                        ),
                        radius = w * 0.85f,
                        center = Offset(w * 0.3f, h * 0.10f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.accentWarmAmber.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(w * 0.85f, h * 0.35f),
                            radius = w * 0.6f
                        ),
                        radius = w * 0.6f,
                        center = Offset(w * 0.85f, h * 0.35f)
                    )
                }
            }
        }

        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = BodlaiTheme.colors.surfaceCard,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    elevation: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = BodlaiTheme.colors
    val isDark = colors.isDark
    val isNeo = colors.isNeoBrutalist
    val isLiquid = colors.isLiquidGlass
    val isSwiss = colors.isSwissEditorial
    val isMuseum = colors.isMuseumPop

    Box(
        modifier = modifier
            .drawBehind {
                val cornerRadius = 24.dp.toPx()
                when {
                    isNeo -> {
                        // Hard solid offset black shadow (Signature Neo-Brutalism)
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                    isLiquid -> {
                        if (isDark) {
                            // Ambient refractive aqua shadow + luminous rim
                            drawRoundRect(
                                color = Color(0x99011425),
                                topLeft = Offset(4.dp.toPx(), 6.dp.toPx()),
                                size = this.size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                            drawRoundRect(
                                color = Color(0x3338BDF8),
                                topLeft = Offset((-1).dp.toPx(), (-1).dp.toPx()),
                                size = this.size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                        } else {
                            // Daylight Frosted Ice glass shadow + bright specular top highlight
                            drawRoundRect(
                                color = Color(0x220F2035),
                                topLeft = Offset(3.dp.toPx(), 5.dp.toPx()),
                                size = this.size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.95f),
                                topLeft = Offset((-1).dp.toPx(), (-1).dp.toPx()),
                                size = this.size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                            )
                        }
                    }
                    isSwiss -> {
                        // Architectural fine paper shadow
                        drawRoundRect(
                            color = Color(0x1814171A),
                            topLeft = Offset(2.dp.toPx(), 4.dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                    isMuseum -> {
                        // Modernist sharp deep shadow
                        drawRoundRect(
                            color = if (isDark) Color(0x88000000) else Color(0x14000000),
                            topLeft = Offset(3.dp.toPx(), 6.dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                    isDark -> {
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.40f),
                            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.08f),
                            topLeft = Offset((-2).dp.toPx(), (-2).dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                    else -> {
                        drawRoundRect(
                            color = Color(0xFF64748B).copy(alpha = 0.14f),
                            topLeft = Offset(3.dp.toPx(), 4.dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.90f),
                            topLeft = Offset((-2).dp.toPx(), (-2).dp.toPx()),
                            size = this.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                        )
                    }
                }
            }
            .clip(shape)
            .background(
                when {
                    isNeo -> androidx.compose.ui.graphics.SolidColor(backgroundColor)
                    isLiquid -> if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0C2B47).copy(alpha = 0.75f),
                                Color(0xFF041829).copy(alpha = 0.55f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.88f),
                                Color(0xFFEBF3FA).copy(alpha = 0.70f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    }
                    isSwiss -> Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor.copy(alpha = 0.98f),
                            backgroundColor.copy(alpha = 0.92f)
                        )
                    )
                    else -> Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                backgroundColor.copy(alpha = 0.88f),
                                backgroundColor.copy(alpha = 0.70f)
                            )
                        } else {
                            listOf(
                                backgroundColor.copy(alpha = 0.98f),
                                backgroundColor.copy(alpha = 0.88f)
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                }
            )
            .border(
                width = if (isNeo) 2.5.dp else if (isLiquid) 1.2.dp else 1.dp,
                brush = when {
                    isNeo -> androidx.compose.ui.graphics.SolidColor(Color.Black)
                    isLiquid -> if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF7DD3FC).copy(alpha = 0.65f), // Shimmering light aqua rim
                                Color(0xFF0284C7).copy(alpha = 0.15f),
                                Color(0xFF38BDF8).copy(alpha = 0.40f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color(0xFF38BDF8).copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.80f)
                            )
                        )
                    }
                    isSwiss -> androidx.compose.ui.graphics.SolidColor(colors.surfaceCardBorder)
                    isMuseum -> if (isDark) {
                        Brush.linearGradient(
                            colors = listOf(
                                colors.accentPrimary.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    } else {
                        androidx.compose.ui.graphics.SolidColor(colors.surfaceCardBorder)
                    }
                    else -> Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color(0xFFE2E8F0).copy(alpha = 0.60f),
                                Color.White.copy(alpha = 0.80f)
                            )
                        }
                    )
                },
                shape = shape
            )
            .padding(contentPadding)
    ) {
        Column {
            content()
        }
    }
}

/**
 * Fluid spring physics & tactile haptic/sound micro-interaction modifier
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val context = LocalContext.current
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            SensoryManager.click(context, view)
        }
    }

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isSecondary: Boolean = false
) {
    val colors = BodlaiTheme.colors
    val context = LocalContext.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btnScale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && enabled && !isLoading) {
            SensoryManager.click(context, view)
        }
    }

    val isDark = colors.isDark
    val isNeo = colors.isNeoBrutalist
    val isSwiss = colors.isSwissEditorial
    val isMuseum = colors.isMuseumPop
    val isLiquid = colors.isLiquidGlass

    val btnShape = when {
        isMuseum -> RoundedCornerShape(999.dp)
        isSwiss -> RoundedCornerShape(14.dp)
        isNeo -> RoundedCornerShape(14.dp)
        else -> RoundedCornerShape(18.dp)
    }

    val bg = when {
        !enabled -> if (isDark) Color(0xFF261D33) else Color(0xFFE2E8F0)
        isSecondary -> if (isNeo || isSwiss) Color.White else Color.Transparent
        else -> colors.accentPrimary
    }

    val textColor = when {
        !enabled -> colors.textMuted
        isSecondary -> if (isNeo) Color.Black else colors.accentPrimary
        else -> colors.textOnAccent
    }

    val borderModifier = when {
        isNeo -> Modifier.border(2.5.dp, Color.Black, btnShape)
        isSwiss && isSecondary -> Modifier.border(1.5.dp, colors.accentPrimary, btnShape)
        isSecondary && enabled -> Modifier.border(1.2.dp, colors.accentPrimary.copy(alpha = 0.8f), btnShape)
        isLiquid && enabled && !isSecondary -> Modifier.border(1.dp, Color.White.copy(alpha = 0.45f), btnShape)
        else -> Modifier
    }

    // Shadow / Glow:
    val shadowModifier = if (enabled) {
        Modifier.drawBehind {
            val cr = if (isMuseum) this.size.height / 2 else 16.dp.toPx()
            when {
                isNeo -> {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = this.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                    )
                }
                isSwiss -> {
                    drawRoundRect(
                        color = Color(0x28E03E2D),
                        topLeft = Offset(0f, 3.dp.toPx()),
                        size = this.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                    )
                }
                isMuseum -> {
                    drawRoundRect(
                        color = colors.accentPrimary.copy(alpha = 0.35f),
                        topLeft = Offset(0f, 4.dp.toPx()),
                        size = this.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                    )
                }
                !isSecondary -> {
                    drawRoundRect(
                        color = colors.accentPrimary.copy(alpha = if (isDark) 0.38f else 0.28f),
                        topLeft = Offset(0f, 4.dp.toPx()),
                        size = this.size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cr, cr)
                    )
                }
            }
        }
    } else Modifier

    Box(
        modifier = modifier
            .scale(scale)
            .then(shadowModifier)
            .then(borderModifier)
            .clip(btnShape)
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = if (isSecondary) colors.accentPrimary else colors.textOnAccent,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun GlassTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingActionText: String? = null,
    onTrailingAction: (() -> Unit)? = null
) {
    val colors = BodlaiTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Minimal rounded-square back button from reference
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceCard)
                .border(1.dp, colors.surfaceCardBorder, RoundedCornerShape(12.dp))
                .bounceClick(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (trailingActionText != null && onTrailingAction != null) {
            Text(
                text = trailingActionText,
                style = MaterialTheme.typography.labelLarge,
                color = colors.accentPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .bounceClick(onClick = onTrailingAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Large Circular Gauge / Clock dial from reference image for conversion progress
 */
@Composable
fun NocturnalCircularDial(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    label: String = "Processing...",
    onCancel: (() -> Unit)? = null
) {
    val colors = BodlaiTheme.colors
    val progress = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0.05f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "circularDial"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circular track
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 16.dp.toPx()
                val radius = (size.minDimension - strokeW) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Outer muted ring
                drawCircle(
                    color = colors.sliderTrackBg,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeW)
                )

                // Active thick rounded lilac progress arc
                drawArc(
                    color = colors.accentPrimary,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            // Big centered percentage + unit
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = BodlaiTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (total > 0) {
                    Text(
                        text = "$current of $total",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        if (onCancel != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.surfaceCardSubtle)
                    .border(1.dp, colors.errorText.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                    .bounceClick(onClick = onCancel)
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        tint = colors.errorText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancel Process",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.errorText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Backward compatibility alias for existing screens
@Composable
fun GlassProgress(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
    label: String? = null,
    onCancel: (() -> Unit)? = null
) {
    NocturnalCircularDial(
        current = current,
        total = total,
        modifier = modifier,
        label = label ?: if (total > 0) "Processing $current of $total" else "Working...",
        onCancel = onCancel
    )
}

@Composable
fun GlassFilePill(
    name: String,
    size: String,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = BodlaiTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceCard)
            .border(1.dp, colors.surfaceCardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = size,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        if (onRemove != null) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceCardSubtle)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Solid Lilac when selected with dark text, dark glass when unselected
 */
@Composable
fun GlassChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BodlaiTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) colors.accentPrimary
                else colors.surfaceCard
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else colors.surfaceCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.textOnAccent else colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

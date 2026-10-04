package com.example.fileconverter.ui.theme

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fileconverter.R

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold)
)

enum class ThemePreset(
    val title: String,
    val styleCategory: String, // "Dark", "Day", "Art"
    val primaryAccent: Color,
    val warmAccent: Color,
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val isDark: Boolean = true,
    val isNeoBrutalist: Boolean = false,
    val isLiquidGlass: Boolean = false,
    val isSwissEditorial: Boolean = false,
    val isMuseumPop: Boolean = false,
    val isZenithAurora: Boolean = false,
    val isBionicJade: Boolean = false
) {
    // --- Nocturnal Dark Themes ---
    NOCTURNAL_LAVENDER(
        title = "Lavender Noir",
        styleCategory = "Dark",
        primaryAccent = Color(0xFFD8B4FE), // Luminous electric lilac
        warmAccent = Color(0xFFFFB84D),   // Golden amber aura
        bgTop = Color(0xFF381F5E),        // Royal velvet amethyst
        bgMid = Color(0xFF160D24),        // Deep midnight nebula
        bgBottom = Color(0xFF090510),     // Abyssal obsidian void
        isDark = true
    ),
    COSMIC_ROSE(
        title = "Cosmic Rose",
        styleCategory = "Dark",
        primaryAccent = Color(0xFFFF6492), // Electric magenta rose
        warmAccent = Color(0xFFFB923C),   // Supernova peach
        bgTop = Color(0xFF4A1054),        // Deep stellar orchid
        bgMid = Color(0xFF1E0626),        // Cosmic void
        bgBottom = Color(0xFF0B020E),     // Pure starlight obsidian
        isDark = true
    ),
    ELECTRIC_LIME(
        title = "Laser Lime",
        styleCategory = "Dark",
        primaryAccent = Color(0xFFA3E635), // Acid neon lime
        warmAccent = Color(0xFF34D399),   // Cyber emerald
        bgTop = Color(0xFF22380A),        // Cybernetic dark olive
        bgMid = Color(0xFF0E1A04),        // Terminal black
        bgBottom = Color(0xFF050B01),     // Deep matrix carbon
        isDark = true
    ),
    OCEAN_CYAN(
        title = "Abyssal Cyan",
        styleCategory = "Dark",
        primaryAccent = Color(0xFF38BDF8), // Radiant oceanic aqua
        warmAccent = Color(0xFF2DD4BF),   // Bioluminescent teal
        bgTop = Color(0xFF0A3B59),        // Midnight marina
        bgMid = Color(0xFF051D2D),        // Oceanic trench
        bgBottom = Color(0xFF020E17),     // Mariana void
        isDark = true
    ),
    SHADOW_MONOCHROME(
        title = "Stealth Carbon",
        styleCategory = "Dark",
        primaryAccent = Color(0xFFF1F5F9), // Pure platinum silver
        warmAccent = Color(0xFF94A3B8),   // Slate chrome
        bgTop = Color(0xFF282C37),        // Matte gunmetal
        bgMid = Color(0xFF16181E),        // Deep graphite
        bgBottom = Color(0xFF0A0B0E),     // Stealth black
        isDark = true
    ),
    GALLERY_OBSIDIAN(
        title = "Obsidian Pop",
        styleCategory = "Dark",
        primaryAccent = Color(0xFFFF2D87), // Fluorescent pop pink
        warmAccent = Color(0xFF818CF8),   // High-fashion periwinkle
        bgTop = Color(0xFF1A1C24),        // Modernist obsidian
        bgMid = Color(0xFF0E1015),        // Gallery black
        bgBottom = Color(0xFF050608),     // Pitch void
        isDark = true,
        isMuseumPop = true
    ),

    // --- Elegant Day / White Themes ---
    PORCELAIN_SNOW(
        title = "Porcelain Snow",
        styleCategory = "Day",
        primaryAccent = Color(0xFF4F46E5), // Royal ultramarine indigo
        warmAccent = Color(0xFFF43F5E),   // Vibrant coral rose
        bgTop = Color(0xFFFFFFFF),        // Pure milk porcelain
        bgMid = Color(0xFFF5F7FA),        // Soft atmospheric fog
        bgBottom = Color(0xFFE9EDF2),     // Crisp platinum
        isDark = false
    ),
    FROSTED_PEARL(
        title = "Frosted Pearl",
        styleCategory = "Day",
        primaryAccent = Color(0xFF0D9488), // Crisp deep teal
        warmAccent = Color(0xFF10B981),   // Vivid emerald jade
        bgTop = Color(0xFFF8FCFA),        // Pale mint pearl
        bgMid = Color(0xFFEBF5F0),        // Soft seafoam wash
        bgBottom = Color(0xFFD9ECE3),     // Glacier jade
        isDark = false
    ),
    MORNING_SKY(
        title = "Morning Sky",
        styleCategory = "Day",
        primaryAccent = Color(0xFF0284C7), // Deep cobalt azure
        warmAccent = Color(0xFF38BDF8),   // Electric sky cyan
        bgTop = Color(0xFFF2F8FD),        // Bright sky light
        bgMid = Color(0xFFE2F0FC),        // Crisp blue breeze
        bgBottom = Color(0xFFCCE4F8),     // Cerulean horizon
        isDark = false
    ),
    ROSE_QUARTZ(
        title = "Rose Quartz",
        styleCategory = "Day",
        primaryAccent = Color(0xFFE11D48), // Carmine ruby
        warmAccent = Color(0xFFFB7185),   // Rose quartz
        bgTop = Color(0xFFFFF5F6),        // Rose silk
        bgMid = Color(0xFFFDE8EB),        // Powder blush
        bgBottom = Color(0xFFFAD4DA),     // Warm champagne rose
        isDark = false
    ),

    // --- Art & Avant-Garde Styles ---
    SWISS_TERRACOTTA(
        title = "Swiss Bauhaus",
        styleCategory = "Art",
        primaryAccent = Color(0xFFE03E2D), // Bauhaus vermilion terracotta
        warmAccent = Color(0xFFD97706),   // Warm amber ochre
        bgTop = Color(0xFFF8F4EC),        // Oatmeal linen paper
        bgMid = Color(0xFFEFE8DB),        // Parchment book page
        bgBottom = Color(0xFFE4DBC9),      // Raw architectural grain
        isDark = false,
        isSwissEditorial = true
    ),
    MUSEUM_POP_PINK(
        title = "Museum Pop",
        styleCategory = "Art",
        primaryAccent = Color(0xFFFF1E75), // Blinding electric pop pink
        warmAccent = Color(0xFF6366F1),   // Modernist gallery indigo
        bgTop = Color(0xFFFFFFFF),        // Stark gallery white
        bgMid = Color(0xFFF6F8FA),        // Light architectural canvas
        bgBottom = Color(0xFFEAEFF5),      // Studio gray
        isDark = false,
        isMuseumPop = true
    ),
    NEO_BRUTALISM(
        title = "Neo Brutalism",
        styleCategory = "Art",
        primaryAccent = Color(0xFFFFD600), // Punchy cyber yellow
        warmAccent = Color(0xFFFF3366),   // Neon coral punch
        bgTop = Color(0xFFFFFDF5),        // Clean brutalist canvas
        bgMid = Color(0xFFFAF4E4),        // Poster cream
        bgBottom = Color(0xFFF1E6CD),      // Cardboard grain
        isDark = false,
        isNeoBrutalist = true
    ),
    LIQUID_GLASS(
        title = "Liquid Glass",
        styleCategory = "Art",
        primaryAccent = Color(0xFF38BDF8), // Radiant fluid cyan
        warmAccent = Color(0xFF818CF8),   // Electric indigo fluid
        bgTop = Color(0xFF0A1C30),        // Deep oceanic translucent abyss
        bgMid = Color(0xFF051120),        // Deep nautical void
        bgBottom = Color(0xFF020710),      // Pure refractive dark
        isDark = true,
        isLiquidGlass = true
    ),
    FROSTED_ICE(
        title = "Frosted Ice",
        styleCategory = "Art",
        primaryAccent = Color(0xFF0284C7), // Sky glacier blue
        warmAccent = Color(0xFFFB923C),   // Peach / Amber prismatic caustics
        bgTop = Color(0xFFEFF5FA),        // Crystalline ice frost
        bgMid = Color(0xFFDCEAF5),        // Soft glacier frost
        bgBottom = Color(0xFFC8DEF0),      // Cool refractive slate
        isDark = false,
        isLiquidGlass = true
    ),

    // --- Inspired by Tesla Phone / Bionic Minimalist UI (Image 1) ---
    BIONIC_JADE(
        title = "Bionic Jade",
        styleCategory = "Art",
        primaryAccent = Color(0xFF2DD4BF), // Crisp electric bionic jade / mint
        warmAccent = Color(0xFFFDE047),   // Solar butter honey gold
        bgTop = Color(0xFF131720),        // Matte bionic slate
        bgMid = Color(0xFF0C0E14),        // Deep cyber carbon
        bgBottom = Color(0xFF050609),     // True obsidian void
        isDark = true,
        isBionicJade = true
    ),
    PORCELAIN_JADE(
        title = "Porcelain Jade",
        styleCategory = "Day",
        primaryAccent = Color(0xFF0D9488), // Deep alpine mint emerald
        warmAccent = Color(0xFFF59E0B),   // Honey gold amber
        bgTop = Color(0xFFFCFDFD),        // Clean studio ceramic
        bgMid = Color(0xFFF0F4F7),        // Soft atmospheric fog
        bgBottom = Color(0xFFE2E9EE),     // Platinum slate
        isDark = false
    ),

    // --- Inspired by Ambient Meditation & Aurora Gradients (Image 2) ---
    ZENITH_AURORA(
        title = "Zenith Aurora",
        styleCategory = "Art",
        primaryAccent = Color(0xFFC084FC), // Radiant electric orchid violet
        warmAccent = Color(0xFF34D399),   // Bioluminescent spring mint emerald
        bgTop = Color(0xFF1A1128),        // Velvet twilight orchid
        bgMid = Color(0xFF0E0818),        // Deep meditative nebula
        bgBottom = Color(0xFF05030A),     // Abyssal cosmic void
        isDark = true,
        isZenithAurora = true
    ),
    LOTUS_DUSK(
        title = "Lotus Dusk",
        styleCategory = "Dark",
        primaryAccent = Color(0xFFF472B6), // Luminous sunset orchid rose
        warmAccent = Color(0xFFFBBF24),   // Radiant golden honey amber
        bgTop = Color(0xFF200E26),        // Deep dusk plum
        bgMid = Color(0xFF120717),        // Midnight twilight
        bgBottom = Color(0xFF07020A),     // Obsidian starlight
        isDark = true
    )
}

@Immutable
data class NocturnalColorPalette(
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val backgroundGradient: Brush,
    val surfaceCard: Color,
    val surfaceCardBorder: Color,
    val surfaceCardSubtle: Color,
    val accentPrimary: Color,
    val textOnAccent: Color,
    val accentWarmAmber: Color,
    val accentWarmAmberEnd: Color = Color(0xFFFF9A3D),
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val toggleOffTrack: Color,
    val toggleOffThumb: Color,
    val sliderTrackBg: Color,
    val errorGlass: Color = Color(0x38EF4444),
    val errorText: Color = Color(0xFFEF4444),
    val successAmber: Color = Color(0xFFFFB155),
    val shadowColor: Color,
    val isDark: Boolean = true,
    val isNeoBrutalist: Boolean = false,
    val isLiquidGlass: Boolean = false,
    val isSwissEditorial: Boolean = false,
    val isMuseumPop: Boolean = false,
    val isZenithAurora: Boolean = false,
    val isBionicJade: Boolean = false,
    val cardGlowColor: Color = Color.Transparent,
    val tagBg: Color = Color.Transparent,
    val tagText: Color = Color.White,
    val accentSecondary: Color = Color(0xFF6366F1)
) {
    // Backward compatibility aliases
    val accentPill: Color get() = accentPrimary
    val glassBorderTopLeft: Color get() = surfaceCardBorder
    val glassSurfaceSubtle: Color get() = surfaceCardSubtle
    val glassSurface: Color get() = surfaceCard
}

val LocalNocturnalColors = staticCompositionLocalOf {
    createPalette(ThemePreset.NOCTURNAL_LAVENDER)
}

fun createPalette(preset: ThemePreset): NocturnalColorPalette {
    val isDark = preset.isDark
    val isNeo = preset.isNeoBrutalist
    val isLiquid = preset.isLiquidGlass
    val isSwiss = preset.isSwissEditorial
    val isMuseum = preset.isMuseumPop
    val isZenith = preset.isZenithAurora
    val isBionic = preset.isBionicJade

    return when {
        isZenith -> {
            NocturnalColorPalette(
                bgTop = preset.bgTop,
                bgMid = preset.bgMid,
                bgBottom = preset.bgBottom,
                backgroundGradient = Brush.verticalGradient(
                    listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                ),
                surfaceCard = Color(0xFF161026).copy(alpha = 0.85f),
                surfaceCardBorder = Color(0xFFC084FC).copy(alpha = 0.35f),
                surfaceCardSubtle = Color(0xFF0F0A1A).copy(alpha = 0.75f),
                accentPrimary = preset.primaryAccent, // Orchid #C084FC
                textOnAccent = Color(0xFF140824),
                accentWarmAmber = preset.warmAccent, // Spring Mint #34D399
                textPrimary = Color(0xFFFAF5FF),
                textSecondary = Color(0xFFD8B4FE).copy(alpha = 0.90f),
                textMuted = Color(0xFFA855F7).copy(alpha = 0.65f),
                toggleOffTrack = Color(0xFF1C1330),
                toggleOffThumb = Color(0xFFC084FC),
                sliderTrackBg = Color(0xFF160E26),
                shadowColor = Color(0x9006020D),
                isDark = true,
                isZenithAurora = true,
                cardGlowColor = Color(0xFFC084FC).copy(alpha = 0.30f),
                tagBg = Color(0xFFC084FC).copy(alpha = 0.20f),
                tagText = Color(0xFFE9D5FF),
                accentSecondary = Color(0xFF34D399)
            )
        }
        isBionic -> {
            NocturnalColorPalette(
                bgTop = preset.bgTop,
                bgMid = preset.bgMid,
                bgBottom = preset.bgBottom,
                backgroundGradient = Brush.verticalGradient(
                    listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                ),
                surfaceCard = Color(0xFF151922).copy(alpha = 0.88f),
                surfaceCardBorder = Color(0xFF2DD4BF).copy(alpha = 0.35f),
                surfaceCardSubtle = Color(0xFF0E1118).copy(alpha = 0.80f),
                accentPrimary = preset.primaryAccent, // Bionic Jade #2DD4BF
                textOnAccent = Color(0xFF041212),
                accentWarmAmber = preset.warmAccent, // Honey Gold #FDE047
                textPrimary = Color(0xFFF0FDF4),
                textSecondary = Color(0xFF99F6E4).copy(alpha = 0.90f),
                textMuted = Color(0xFF5EEAD4).copy(alpha = 0.60f),
                toggleOffTrack = Color(0xFF1A212E),
                toggleOffThumb = Color(0xFF2DD4BF),
                sliderTrackBg = Color(0xFF131720),
                shadowColor = Color(0x90030508),
                isDark = true,
                isBionicJade = true,
                cardGlowColor = Color(0xFF2DD4BF).copy(alpha = 0.28f),
                tagBg = Color(0xFF2DD4BF).copy(alpha = 0.20f),
                tagText = Color(0xFF2DD4BF),
                accentSecondary = Color(0xFFFDE047)
            )
        }
        isNeo -> {
            NocturnalColorPalette(
                bgTop = preset.bgTop,
                bgMid = preset.bgMid,
                bgBottom = preset.bgBottom,
                backgroundGradient = Brush.verticalGradient(
                    listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                ),
                surfaceCard = Color.White,
                surfaceCardBorder = Color.Black,
                surfaceCardSubtle = Color(0xFFF7F3E9),
                accentPrimary = preset.primaryAccent, // Vivid Yellow #FFD600
                textOnAccent = Color.Black,
                accentWarmAmber = preset.warmAccent, // Pink #FF3366
                textPrimary = Color(0xFF0A0A0A),
                textSecondary = Color(0xFF262626),
                textMuted = Color(0xFF525252),
                toggleOffTrack = Color(0xFFE5E5E5),
                toggleOffThumb = Color.Black,
                sliderTrackBg = Color(0xFFE5E5E5),
                shadowColor = Color.Black,
                isDark = false,
                isNeoBrutalist = true,
                isLiquidGlass = false,
                isSwissEditorial = false,
                isMuseumPop = false,
                cardGlowColor = Color.Black,
                tagBg = Color.Black,
                tagText = preset.primaryAccent,
                accentSecondary = preset.warmAccent
            )
        }
        isLiquid -> {
            if (isDark) {
                // High-refraction luminous liquid aqua/cyan glass palette (Dark Oceanic)
                NocturnalColorPalette(
                    bgTop = preset.bgTop,
                    bgMid = preset.bgMid,
                    bgBottom = preset.bgBottom,
                    backgroundGradient = Brush.verticalGradient(
                        listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                    ),
                    surfaceCard = Color(0xFF0C243B).copy(alpha = 0.65f), // Ultra-translucent fluid tint
                    surfaceCardBorder = Color(0xFF38BDF8).copy(alpha = 0.40f), // Shimmering refractive aqua rim
                    surfaceCardSubtle = Color(0xFF071A2C).copy(alpha = 0.55f),
                    accentPrimary = preset.primaryAccent, // Radiant Fluid Cyan #38BDF8
                    textOnAccent = Color(0xFF021324),
                    accentWarmAmber = preset.warmAccent, // Electric Indigo #818CF8
                    textPrimary = Color(0xFFF0FDF4),
                    textSecondary = Color(0xFFBAE6FD).copy(alpha = 0.85f),
                    textMuted = Color(0xFF7DD3FC).copy(alpha = 0.60f),
                    toggleOffTrack = Color(0xFF0C2742),
                    toggleOffThumb = Color(0xFF38BDF8),
                    sliderTrackBg = Color(0xFF0A2138),
                    shadowColor = Color(0x90010E1A),
                    isDark = true,
                    isNeoBrutalist = false,
                    isLiquidGlass = true,
                    isSwissEditorial = false,
                    isMuseumPop = false,
                    cardGlowColor = Color(0xFF38BDF8).copy(alpha = 0.28f),
                    tagBg = Color(0xFF38BDF8).copy(alpha = 0.18f),
                    tagText = Color(0xFF7DD3FC),
                    accentSecondary = preset.warmAccent
                )
            } else {
                // Daylight Frosted Ice Liquid Glass (Image 2)
                NocturnalColorPalette(
                    bgTop = preset.bgTop,
                    bgMid = preset.bgMid,
                    bgBottom = preset.bgBottom,
                    backgroundGradient = Brush.verticalGradient(
                        listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                    ),
                    surfaceCard = Color.White.copy(alpha = 0.75f), // Frosted crystal glass
                    surfaceCardBorder = Color.White.copy(alpha = 0.95f),
                    surfaceCardSubtle = Color(0xFFE2EEF8).copy(alpha = 0.70f),
                    accentPrimary = preset.primaryAccent, // Crisp Sky Azure #0284C7
                    textOnAccent = Color.White,
                    accentWarmAmber = preset.warmAccent, // Amber / Peach caustic shimmer #FB923C
                    textPrimary = Color(0xFF0C2136), // Deep glacial slate
                    textSecondary = Color(0xFF2E4760),
                    textMuted = Color(0xFF5D7B99),
                    toggleOffTrack = Color(0xFFD6E3F0),
                    toggleOffThumb = Color(0xFF0284C7),
                    sliderTrackBg = Color(0xFFD6E3F0),
                    shadowColor = Color(0x280C2136),
                    isDark = false,
                    isNeoBrutalist = false,
                    isLiquidGlass = true,
                    isSwissEditorial = false,
                    isMuseumPop = false,
                    cardGlowColor = Color(0xFF0284C7).copy(alpha = 0.20f),
                    tagBg = Color(0xFF0284C7).copy(alpha = 0.12f),
                    tagText = Color(0xFF0284C7),
                    accentSecondary = preset.warmAccent
                )
            }
        }
        isSwiss -> {
            // Warm Editorial Swiss / Bauhaus paper palette (Image 1)
            NocturnalColorPalette(
                bgTop = preset.bgTop,
                bgMid = preset.bgMid,
                bgBottom = preset.bgBottom,
                backgroundGradient = Brush.verticalGradient(
                    listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                ),
                surfaceCard = Color(0xFFFAF7F0).copy(alpha = 0.96f),
                surfaceCardBorder = Color(0xFFDCD2C1),
                surfaceCardSubtle = Color(0xFFEFE8DB),
                accentPrimary = preset.primaryAccent, // Bauhaus Terracotta #E03E2D
                textOnAccent = Color.White,
                accentWarmAmber = preset.warmAccent, // Burnt Ochre #D97706
                textPrimary = Color(0xFF14171A), // Deep ink carbon black
                textSecondary = Color(0xFF4A525A),
                textMuted = Color(0xFF86909A),
                toggleOffTrack = Color(0xFFDDD3C2),
                toggleOffThumb = Color(0xFF86909A),
                sliderTrackBg = Color(0xFFE3D9C8),
                shadowColor = Color(0x2214171A),
                isDark = false,
                isNeoBrutalist = false,
                isLiquidGlass = false,
                isSwissEditorial = true,
                isMuseumPop = false,
                cardGlowColor = Color(0xFFE03E2D).copy(alpha = 0.16f),
                tagBg = Color(0xFFE03E2D).copy(alpha = 0.12f),
                tagText = Color(0xFFE03E2D),
                accentSecondary = preset.warmAccent
            )
        }
        isMuseum -> {
            if (isDark) {
                // High-Fashion Museum Dark Obsidian palette (Image 3 dark)
                NocturnalColorPalette(
                    bgTop = preset.bgTop,
                    bgMid = preset.bgMid,
                    bgBottom = preset.bgBottom,
                    backgroundGradient = Brush.verticalGradient(
                        listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                    ),
                    surfaceCard = Color(0xFF161820).copy(alpha = 0.90f),
                    surfaceCardBorder = Color.White.copy(alpha = 0.12f),
                    surfaceCardSubtle = Color(0xFF101217).copy(alpha = 0.80f),
                    accentPrimary = preset.primaryAccent, // Fluorescent Pop Pink #FF2D87
                    textOnAccent = Color.White,
                    accentWarmAmber = preset.warmAccent, // Periwinkle #818CF8
                    textPrimary = Color(0xFFFAFAFA),
                    textSecondary = Color(0xFFA3A3A3),
                    textMuted = Color(0xFF737373),
                    toggleOffTrack = Color(0xFF262626),
                    toggleOffThumb = Color(0xFF737373),
                    sliderTrackBg = Color(0xFF262626),
                    shadowColor = Color(0x80000000),
                    isDark = true,
                    isNeoBrutalist = false,
                    isLiquidGlass = false,
                    isSwissEditorial = false,
                    isMuseumPop = true,
                    cardGlowColor = Color(0xFFFF2D87).copy(alpha = 0.28f),
                    tagBg = Color(0xFFFF2D87).copy(alpha = 0.20f),
                    tagText = Color(0xFFFF6492),
                    accentSecondary = preset.warmAccent
                )
            } else {
                // High-Fashion Minimalist Gallery White & Electric Pink (Image 3 light)
                NocturnalColorPalette(
                    bgTop = preset.bgTop,
                    bgMid = preset.bgMid,
                    bgBottom = preset.bgBottom,
                    backgroundGradient = Brush.verticalGradient(
                        listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                    ),
                    surfaceCard = Color.White.copy(alpha = 0.96f),
                    surfaceCardBorder = Color(0xFFE5E7EB),
                    surfaceCardSubtle = Color(0xFFF3F4F6),
                    accentPrimary = preset.primaryAccent, // Blinding Electric Pop Pink #FF1E75
                    textOnAccent = Color.White,
                    accentWarmAmber = preset.warmAccent, // Modernist Indigo #6366F1
                    textPrimary = Color(0xFF0A0A0A),
                    textSecondary = Color(0xFF404040),
                    textMuted = Color(0xFF8A8A8A),
                    toggleOffTrack = Color(0xFFE5E5E5),
                    toggleOffThumb = Color(0xFF8A8A8A),
                    sliderTrackBg = Color(0xFFE5E5E5),
                    shadowColor = Color(0x18000000),
                    isDark = false,
                    isNeoBrutalist = false,
                    isLiquidGlass = false,
                    isSwissEditorial = false,
                    isMuseumPop = true,
                    cardGlowColor = Color(0xFFFF1E75).copy(alpha = 0.22f),
                    tagBg = Color(0xFFFF1E75),
                    tagText = Color.White,
                    accentSecondary = preset.warmAccent
                )
            }
        }
        isDark -> {
            NocturnalColorPalette(
                bgTop = preset.bgTop,
                bgMid = preset.bgMid,
                bgBottom = preset.bgBottom,
                backgroundGradient = Brush.verticalGradient(
                    listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                ),
                surfaceCard = Color(0xFF261D36).copy(alpha = 0.82f),
                surfaceCardBorder = Color.White.copy(alpha = 0.12f),
                surfaceCardSubtle = Color(0xFF1B1428).copy(alpha = 0.75f),
                accentPrimary = preset.primaryAccent,
                textOnAccent = Color(0xFF0F0818),
                accentWarmAmber = preset.warmAccent,
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFAFA3BF).copy(alpha = 0.90f),
                textMuted = Color(0xFFAFA3BF).copy(alpha = 0.60f),
                toggleOffTrack = Color(0xFF281F38),
                toggleOffThumb = Color(0xFF8C7F9E),
                sliderTrackBg = Color(0xFF261D33),
                shadowColor = Color(0x8007050C),
                isDark = true,
                isNeoBrutalist = false,
                isLiquidGlass = false,
                isSwissEditorial = false,
                isMuseumPop = false,
                cardGlowColor = preset.primaryAccent.copy(alpha = 0.25f),
                tagBg = preset.primaryAccent.copy(alpha = 0.18f),
                tagText = preset.primaryAccent,
                accentSecondary = preset.warmAccent
            )
        }
        else -> {
            NocturnalColorPalette(
                bgTop = preset.bgTop,
                bgMid = preset.bgMid,
                bgBottom = preset.bgBottom,
                backgroundGradient = Brush.verticalGradient(
                    listOf(preset.bgTop, preset.bgMid, preset.bgBottom)
                ),
                surfaceCard = Color.White.copy(alpha = 0.92f),
                surfaceCardBorder = Color.White.copy(alpha = 0.95f),
                surfaceCardSubtle = Color(0xFFF1F5F9).copy(alpha = 0.90f),
                accentPrimary = preset.primaryAccent,
                textOnAccent = Color.White,
                accentWarmAmber = preset.warmAccent,
                textPrimary = Color(0xFF0F172A), // Crisp charcoal/slate for high readability
                textSecondary = Color(0xFF475569),
                textMuted = Color(0xFF94A3B8),
                toggleOffTrack = Color(0xFFE2E8F0),
                toggleOffThumb = Color(0xFF94A3B8),
                sliderTrackBg = Color(0xFFE2E8F0),
                shadowColor = Color(0x20000000),
                isDark = false,
                isNeoBrutalist = false,
                isLiquidGlass = false,
                isSwissEditorial = false,
                isMuseumPop = false,
                cardGlowColor = preset.primaryAccent.copy(alpha = 0.16f),
                tagBg = preset.primaryAccent.copy(alpha = 0.14f),
                tagText = preset.primaryAccent,
                accentSecondary = preset.warmAccent
            )
        }
    }
}

object ThemeManager {
    private const val PREFS_NAME = "bodlai_theme_prefs"
    private const val KEY_PRESET = "theme_preset"

    fun getSavedPreset(context: Context): ThemePreset {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_PRESET, ThemePreset.NOCTURNAL_LAVENDER.name)
        return try {
            ThemePreset.valueOf(name ?: ThemePreset.NOCTURNAL_LAVENDER.name)
        } catch (_: Exception) {
            ThemePreset.NOCTURNAL_LAVENDER
        }
    }

    fun savePreset(context: Context, preset: ThemePreset) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PRESET, preset.name)
            .apply()
    }
}

val LocalThemePreset = compositionLocalOf { mutableStateOf(ThemePreset.NOCTURNAL_LAVENDER) }

object BodlaiTheme {
    val colors: NocturnalColorPalette
        @Composable
        get() = LocalNocturnalColors.current

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )

    val typography: Typography
        @Composable
        get() {
            val c = colors
            return Typography(
                displayLarge = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    letterSpacing = (-1.5).sp,
                    color = c.textPrimary
                ),
                displayMedium = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    letterSpacing = (-1).sp,
                    color = c.textPrimary
                ),
                headlineMedium = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp,
                    color = c.textPrimary
                ),
                titleLarge = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = c.textPrimary
                ),
                titleMedium = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = c.textPrimary
                ),
                bodyLarge = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = c.textSecondary
                ),
                bodyMedium = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = c.textSecondary
                ),
                labelLarge = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = c.textPrimary
                ),
                labelSmall = TextStyle(
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = c.textMuted
                )
            )
        }
}

typealias GlassTheme = BodlaiTheme

@Composable
fun FileConverterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentPresetState = remember { mutableStateOf(ThemeManager.getSavedPreset(context)) }
    val palette = remember(currentPresetState.value) { createPalette(currentPresetState.value) }

    val materialColors = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accentPrimary,
            onPrimary = palette.textOnAccent,
            background = palette.bgBottom,
            surface = palette.surfaceCard,
            onSurface = palette.textPrimary
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = palette.accentPrimary,
            onPrimary = palette.textOnAccent,
            background = palette.bgBottom,
            surface = palette.surfaceCard,
            onSurface = palette.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalThemePreset provides currentPresetState,
        LocalNocturnalColors provides palette
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            shapes = BodlaiTheme.shapes,
            typography = BodlaiTheme.typography,
            content = content
        )
    }
}

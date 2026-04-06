package com.neoninnovationlab.neomotion.demo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────────────────────
// NeoMotion Brand Palette
// Deep violet + electric cyan accent — dark theme first
// ──────────────────────────────────────────────────────────────────────────────

private val NeoViolet        = Color(0xFF7C5CBF)   // Primary brand
private val NeoVioletDim     = Color(0xFF5A3F99)
private val NeoVioletOnDark  = Color(0xFFD4BBFF)   // On-dark text on primary
private val NeoVioletContainer = Color(0xFF3B2970) // Surface tint containers

private val NeoCyan          = Color(0xFF00D4C8)   // Secondary / accent
private val NeoCyanDim       = Color(0xFF009E96)
private val NeoCyanOnDark    = Color(0xFF00FFF5)

private val NeoSurface       = Color(0xFF0F0D17)   // Almost-black background
private val NeoSurface1      = Color(0xFF1A1628)   // Cards
private val NeoSurface2      = Color(0xFF24203A)   // Elevated surfaces
private val NeoSurface3      = Color(0xFF2E2A47)   // Dialogs / sheets

private val NeoOnSurface     = Color(0xFFEDE8FF)   // Primary text
private val NeoOnSurfaceVar  = Color(0xFFB0AAD4)   // Secondary text / hint

private val NeoError         = Color(0xFFFF6B6B)
private val NeoOnError       = Color(0xFF1C0000)
private val NeoErrorContainer = Color(0xFF930000)

// ──────────────────────────────────────────────────────────────────────────────
// Color Schemes
// ──────────────────────────────────────────────────────────────────────────────

private val NeoDarkColorScheme = darkColorScheme(
    primary              = NeoViolet,
    onPrimary            = Color.White,
    primaryContainer     = NeoVioletContainer,
    onPrimaryContainer   = NeoVioletOnDark,

    secondary            = NeoCyan,
    onSecondary          = Color(0xFF00201E),
    secondaryContainer   = NeoCyanDim,
    onSecondaryContainer = NeoCyanOnDark,

    background           = NeoSurface,
    onBackground         = NeoOnSurface,

    surface              = NeoSurface1,
    onSurface            = NeoOnSurface,
    surfaceVariant       = NeoSurface2,
    onSurfaceVariant     = NeoOnSurfaceVar,

    surfaceTint          = NeoViolet,

    error                = NeoError,
    onError              = NeoOnError,
    errorContainer       = NeoErrorContainer,
    onErrorContainer     = Color(0xFFFFDAD6),

    outline              = Color(0xFF534C70),
    outlineVariant       = Color(0xFF3A3456),
)

private val NeoLightColorScheme = lightColorScheme(
    primary              = NeoVioletDim,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFEADDFF),
    onPrimaryContainer   = Color(0xFF21005D),

    secondary            = NeoCyanDim,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFCEFAF8),
    onSecondaryContainer = Color(0xFF002021),

    background           = Color(0xFFF8F5FF),
    onBackground         = Color(0xFF1C1B1F),

    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1C1B1F),
    surfaceVariant       = Color(0xFFE7E0EC),
    onSurfaceVariant     = Color(0xFF49454F),

    error                = Color(0xFFB3261E),
    onError              = Color.White,
    errorContainer       = Color(0xFFF9DEDC),
    onErrorContainer     = Color(0xFF410E0B),
)

// ──────────────────────────────────────────────────────────────────────────────
// Theme Composable
// ──────────────────────────────────────────────────────────────────────────────

/**
 * The root Material3 theme for the NeoMotion demo app.
 *
 * Defaults to dark theme. Light theme is provided as a complete fallback.
 * Typography and shapes use Material3 defaults for now — they can be customized
 * in a future iteration without touching other files.
 *
 * MVVM role: Pure UI / View layer concern. No state or business logic.
 */
@Composable
fun NeoMotionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NeoDarkColorScheme else NeoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content     = content,
    )
}

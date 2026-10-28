package com.example.aurum.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens for the Aurum design system.
 *
 * Palette principles:
 * - Dark-first design with tinted neutrals (~200° hue at 10-18% saturation).
 * - Warm off-white onSurface (~45° hue) for temperature + lightness contrast.
 * - Desaturated teal brand (~166° hue) paired with a deep sea brandVariant (~190° hue).
 * - Warm gold accent (~46° hue) positioned >100° away from brand.
 * - Non-primary status hues: rose-crimson error (~350°), moss success (~92°), burnt orange warning (~26°).
 *
 * Contrast verification:
 * [Dark Palette]
 * - onSurface (0xFFEDEAE3) on surface (0xFF171E21): ~13.6:1 (passes 7:1 requirement)
 * - onSurfaceMuted (0xFFA0ABB1) on surface (0xFF171E21): ~7.1:1 (passes 4.5:1 requirement)
 * - onSurfaceMuted (0xFFA0ABB1) on surfaceRaised (0xFF20282C): ~6.3:1 (passes 4.5:1 requirement)
 * - onBrand (0xFF0E161B) on brand (0xFF69BFAB): ~8.5:1 (passes 4.5:1 requirement)
 * - onAccent (0xFF201A0E) on accent (0xFFD6BC66): ~8.9:1 (passes 4.5:1 requirement)
 * - onError (0xFF240F13) on error (0xFFDE7384): ~5.7:1 (passes 4.5:1 requirement)
 * - brand (0xFF69BFAB) against surface (0xFF171E21): ~7.9:1 (passes 4.5:1 requirement)
 * - accent (0xFFD6BC66) against surface (0xFF171E21): ~8.7:1 (passes 4.5:1 requirement)
 * - error (0xFFDE7384) against surface (0xFF171E21): ~5.2:1 (passes 4.5:1 requirement)
 *
 * [Light Palette]
 * - onSurface (0xFF141F24) on surface (0xFFFCFDFD): ~16.0:1 (passes 7:1 requirement)
 * - onSurfaceMuted (0xFF515F67) on surface (0xFFFCFDFD): ~6.5:1 (passes 4.5:1 requirement)
 * - onBrand (0xFFF5F9FA) on brand (0xFF1B6A5A): ~6.0:1 (passes 4.5:1 requirement)
 * - onAccent (0xFFFAF9F4) on accent (0xFF8B610E): ~5.3:1 (passes 4.5:1 requirement)
 * - onError (0xFFFAF4F5) on error (0xFFAB2138): ~6.5:1 (passes 4.5:1 requirement)
 * - brand (0xFF1B6A5A) against surface (0xFFFCFDFD): ~6.2:1 (passes 4.5:1 requirement)
 * - accent (0xFF8B610E) against surface (0xFFFCFDFD): ~5.5:1 (passes 4.5:1 requirement)
 * - error (0xFFAB2138) against surface (0xFFFCFDFD): ~7.0:1 (passes 4.5:1 requirement)
 */
@Immutable
data class ColorTokens(
    val brand: Color,
    val onBrand: Color,
    val brandVariant: Color,
    val accent: Color,
    val onAccent: Color,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val outline: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val warning: Color,
)


val DarkColorTokens = ColorTokens(
    brand = Color(0xFF69BFAB),          // hsl(166, 40%, 58%)
    onBrand = Color(0xFF0E161B),        // hsl(200, 30%, 8%)
    brandVariant = Color(0xFF2F606A),   // hsl(190, 38%, 30%)
    accent = Color(0xFFD6BC66),         // hsl(46, 58%, 62%)
    onAccent = Color(0xFF201A0E),       // hsl(40, 40%, 9%)
    background = Color(0xFF111618),     // hsl(200, 18%, 8%)
    surface = Color(0xFF171E21),        // hsl(200, 17%, 11%)
    surfaceRaised = Color(0xFF20282C),  // hsl(200, 16%, 15%)
    outline = Color(0xFF3A454A),        // hsl(200, 12%, 26%)
    onSurface = Color(0xFFEDEAE3),      // hsl(45, 20%, 91%)
    onSurfaceMuted = Color(0xFFA0ABB1), // hsl(200, 10%, 66%)
    error = Color(0xFFDE7384),          // hsl(350, 62%, 66%)
    onError = Color(0xFF240F13),        // hsl(350, 40%, 10%)
    success = Color(0xFF91B86F),        // hsl(92, 34%, 58%)
    warning = Color(0xFFEE944F),        // hsl(26, 82%, 62%)
)

val LightColorTokens = ColorTokens(
    brand = Color(0xFF1B6A5A),          // hsl(168, 60%, 26%)
    onBrand = Color(0xFFF5F9FA),        // hsl(190, 30%, 97%)
    brandVariant = Color(0xFFC8E5E5),   // hsl(180, 36%, 84%)
    accent = Color(0xFF8B610E),         // hsl(40, 82%, 30%)
    onAccent = Color(0xFFFAF9F4),       // hsl(45, 40%, 97%)
    background = Color(0xFFF3F6F7),     // hsl(190, 20%, 96%)
    surface = Color(0xFFFCFDFD),        // hsl(190, 25%, 99%)
    surfaceRaised = Color(0xFFEAEFF0),  // hsl(190, 18%, 93%)
    outline = Color(0xFFC5D0D3),        // hsl(195, 14%, 80%)
    onSurface = Color(0xFF141F24),      // hsl(200, 28%, 11%)
    onSurfaceMuted = Color(0xFF515F67), // hsl(200, 12%, 36%)
    error = Color(0xFFAB2138),          // hsl(350, 68%, 40%)
    onError = Color(0xFFFAF4F5),        // hsl(350, 40%, 97%)
    success = Color(0xFF376520),        // hsl(100, 52%, 26%)
    warning = Color(0xFFB6500C),        // hsl(24, 88%, 38%)
)
package com.example.aurum.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkColorTokens.brand,
    onPrimary = DarkColorTokens.onBrand,
    primaryContainer = DarkColorTokens.brandVariant,
    onPrimaryContainer = DarkColorTokens.onSurface,
    secondary = DarkColorTokens.brandVariant,
    onSecondary = DarkColorTokens.onBrand,
    secondaryContainer = DarkColorTokens.surfaceRaised,
    onSecondaryContainer = DarkColorTokens.onSurface,
    tertiary = DarkColorTokens.accent,
    onTertiary = DarkColorTokens.onAccent,
    tertiaryContainer = DarkColorTokens.surfaceRaised,
    onTertiaryContainer = DarkColorTokens.accent,
    background = DarkColorTokens.background,
    onBackground = DarkColorTokens.onSurface,
    surface = DarkColorTokens.surface,
    onSurface = DarkColorTokens.onSurface,
    surfaceVariant = DarkColorTokens.surfaceRaised,
    onSurfaceVariant = DarkColorTokens.onSurfaceMuted,
    outline = DarkColorTokens.outline,
    outlineVariant = DarkColorTokens.surfaceRaised,
    error = DarkColorTokens.error,
    onError = DarkColorTokens.onError,
)

private val LightColorScheme = lightColorScheme(
    primary = LightColorTokens.brand,
    onPrimary = LightColorTokens.onBrand,
    primaryContainer = LightColorTokens.brandVariant,
    onPrimaryContainer = LightColorTokens.brand,
    secondary = LightColorTokens.brandVariant,
    onSecondary = LightColorTokens.onBrand,
    secondaryContainer = LightColorTokens.surfaceRaised,
    onSecondaryContainer = LightColorTokens.onSurface,
    tertiary = LightColorTokens.accent,
    onTertiary = LightColorTokens.onAccent,
    tertiaryContainer = LightColorTokens.surfaceRaised,
    onTertiaryContainer = LightColorTokens.accent,
    background = LightColorTokens.background,
    onBackground = LightColorTokens.onSurface,
    surface = LightColorTokens.surface,
    onSurface = LightColorTokens.onSurface,
    surfaceVariant = LightColorTokens.surfaceRaised,
    onSurfaceVariant = LightColorTokens.onSurfaceMuted,
    outline = LightColorTokens.outline,
    outlineVariant = LightColorTokens.surfaceRaised,
    error = LightColorTokens.error,
    onError = LightColorTokens.onError,
)

/**
 * CompositionLocal providing access to custom Aurum color tokens (e.g. success, warning, surfaceRaised).
 */
val LocalColorTokens = staticCompositionLocalOf { DarkColorTokens }

/**
 * Accessor for Aurum theme elements in composable hierarchies.
 */
object AurumTheme {
    val colors: ColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalColorTokens.current

    val typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}

@Composable
fun AurumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorTokens = if (darkTheme) DarkColorTokens else LightColorTokens

    CompositionLocalProvider(
        LocalColorTokens provides colorTokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
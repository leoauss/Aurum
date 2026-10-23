package com.example.aurum.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shape tokens for the Aurum design system.
 *
 * Mapped to UI roles:
 * - extraSmall (4.dp): Badges, tooltips, small chips.
 * - small (8.dp): Input fields, buttons, snackbars.
 * - medium (16.dp): Transaction cards, alert dialogs, numeric keypad buttons.
 * - large (24.dp): Payee confirmation cards, UPI launcher sheets.
 * - extraLarge (32.dp): Full-width modal bottom sheet headers, major surface containers.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val Shapes = AppShapes

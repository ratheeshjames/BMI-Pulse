package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.domain.model.BmiCategory

// Material 3 Expressive Emerald / Teal Vitality Brand Palette
val EmeraldPrimaryLight = Color(0xFF006C51)
val OnEmeraldPrimaryLight = Color(0xFFFFFFFF)
val EmeraldPrimaryContainerLight = Color(0xFF84F8D1)
val OnEmeraldPrimaryContainerLight = Color(0xFF002116)

val TealSecondaryLight = Color(0xFF006874)
val OnTealSecondaryLight = Color(0xFFFFFFFF)
val TealSecondaryContainerLight = Color(0xFF97F0FF)
val OnTealSecondaryContainerLight = Color(0xFF001F24)

val CoralTertiaryLight = Color(0xFF984061)
val OnCoralTertiaryLight = Color(0xFFFFFFFF)
val CoralTertiaryContainerLight = Color(0xFFFFD9E2)
val OnCoralTertiaryContainerLight = Color(0xFF3E001D)

val SurfaceLight = Color(0xFFF4FBF7)
val SurfaceContainerLight = Color(0xFFE8F5EE)
val SurfaceContainerHighLight = Color(0xFFDCECE4)
val OnSurfaceLight = Color(0xFF161D1A)
val OnSurfaceVariantLight = Color(0xFF3F4944)

// Dark Expressive Palette
val EmeraldPrimaryDark = Color(0xFF67DBB5)
val OnEmeraldPrimaryDark = Color(0xFF003828)
val EmeraldPrimaryContainerDark = Color(0xFF00513C)
val OnEmeraldPrimaryContainerDark = Color(0xFF84F8D1)

val TealSecondaryDark = Color(0xFF4FD8EB)
val OnTealSecondaryDark = Color(0xFF00363D)
val TealSecondaryContainerDark = Color(0xFF004F58)
val OnTealSecondaryContainerDark = Color(0xFF97F0FF)

val CoralTertiaryDark = Color(0xFFFFB0C8)
val OnCoralTertiaryDark = Color(0xFF5E1133)
val CoralTertiaryContainerDark = Color(0xFF7B2949)
val OnCoralTertiaryContainerDark = Color(0xFFFFD9E2)

val SurfaceDark = Color(0xFF0E1512)
val SurfaceContainerDark = Color(0xFF1A2420)
val SurfaceContainerHighDark = Color(0xFF23302B)
val OnSurfaceDark = Color(0xFFDEE4E0)
val OnSurfaceVariantDark = Color(0xFFBFC9C3)

// High Contrast WCAG AAA Tokens
val HighContrastPrimaryDark = Color(0xFF4ADE80)
val HighContrastBackgroundDark = Color(0xFF000000)
val HighContrastSurfaceDark = Color(0xFF111815)

/**
 * Returns WCAG AA accessible semantic color for each BMI category.
 */
fun BmiCategory.accentColor(isDark: Boolean): Color {
    return when (this) {
        BmiCategory.SEVERE_THINNESS -> if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
        BmiCategory.MODERATE_THINNESS -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
        BmiCategory.MILD_THINNESS -> if (isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
        BmiCategory.NORMAL -> if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
        BmiCategory.OVERWEIGHT -> if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
        BmiCategory.OBESE_CLASS_I -> if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C)
        BmiCategory.OBESE_CLASS_II -> if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
        BmiCategory.OBESE_CLASS_III -> if (isDark) Color(0xFFF43F5E) else Color(0xFF9F1239)
    }
}

fun BmiCategory.containerColor(isDark: Boolean): Color {
    return when (this) {
        BmiCategory.SEVERE_THINNESS,
        BmiCategory.MODERATE_THINNESS,
        BmiCategory.MILD_THINNESS -> if (isDark) Color(0xFF0C2D48) else Color(0xFFE0F2FE)
        BmiCategory.NORMAL -> if (isDark) Color(0xFF063E28) else Color(0xFFDCFCE7)
        BmiCategory.OVERWEIGHT -> if (isDark) Color(0xFF3E2706) else Color(0xFFFEF3C7)
        BmiCategory.OBESE_CLASS_I,
        BmiCategory.OBESE_CLASS_II,
        BmiCategory.OBESE_CLASS_III -> if (isDark) Color(0xFF45121B) else Color(0xFFFFE4E6)
    }
}

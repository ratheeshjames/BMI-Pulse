package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@RequiresOptIn(message = "This Material 3 Expressive API is experimental and uses physics-driven motion and morphing shapes.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalMaterial3ExpressiveApi

/**
 * Material 3 Expressive MotionScheme with dedicated Spatial (bouncier) and Effects (smoother) springs.
 */
@Immutable
data class MotionScheme(
    val defaultSpatialSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),
    val fastSpatialSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    val slowSpatialSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    ),
    val defaultEffectsSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    val fastEffectsSpec: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
) {
    companion object {
        @ExperimentalMaterial3ExpressiveApi
        fun expressive(): MotionScheme = MotionScheme()
    }
}

val LocalMotionScheme = staticCompositionLocalOf { MotionScheme() }

/**
 * MaterialShapes: Morphing polygon/scallop/clover/star shapes for BMI badges, avatars, and expressive buttons.
 */
object MaterialShapes {
    /**
     * Creates a smooth morphing scalloped/starburst badge shape where [morphProgress] (0f..1f)
     * interpolates between a soft rounded circle/squircle and an expressive 12-lobe cookie/scallop badge.
     */
    fun morphingBadgeShape(lobes: Int = 10, morphProgress: Float = 1f): Shape = object : Shape {
        override fun createOutline(
            size: Size,
            layoutDirection: LayoutDirection,
            density: Density
        ): Outline {
            val path = Path()
            val cx = size.width / 2f
            val cy = size.height / 2f
            val baseRadius = min(cx, cy)
            val amplitude = baseRadius * 0.085f * morphProgress.coerceIn(0f, 1f)
            val meanRadius = baseRadius - amplitude
            val steps = 120
            for (i in 0..steps) {
                val theta = (2.0 * PI * i) / steps
                val r = meanRadius + amplitude * cos(lobes * theta).toFloat()
                val x = cx + r * cos(theta).toFloat()
                val y = cy + r * sin(theta).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            return Outline.Generic(path)
        }
    }
}

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val ExpressiveLightColorScheme = lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = OnEmeraldPrimaryLight,
    primaryContainer = EmeraldPrimaryContainerLight,
    onPrimaryContainer = OnEmeraldPrimaryContainerLight,
    secondary = TealSecondaryLight,
    onSecondary = OnTealSecondaryLight,
    secondaryContainer = TealSecondaryContainerLight,
    onSecondaryContainer = OnTealSecondaryContainerLight,
    tertiary = CoralTertiaryLight,
    onTertiary = OnCoralTertiaryLight,
    tertiaryContainer = CoralTertiaryContainerLight,
    onTertiaryContainer = OnCoralTertiaryContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight
)

private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = OnEmeraldPrimaryDark,
    primaryContainer = EmeraldPrimaryContainerDark,
    onPrimaryContainer = OnEmeraldPrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = OnTealSecondaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = OnTealSecondaryContainerDark,
    tertiary = CoralTertiaryDark,
    onTertiary = OnCoralTertiaryDark,
    tertiaryContainer = CoralTertiaryContainerDark,
    onTertiaryContainer = OnCoralTertiaryContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark
)

private val HighContrastDarkColorScheme = ExpressiveDarkColorScheme.copy(
    primary = HighContrastPrimaryDark,
    onPrimary = Color.Black,
    background = HighContrastBackgroundDark,
    surface = HighContrastSurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFE5E7EB)
)

private val HighContrastLightColorScheme = ExpressiveLightColorScheme.copy(
    primary = Color(0xFF004D38),
    onPrimary = Color.White,
    background = Color.White,
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color(0xFF111827)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialExpressiveTheme(
    colorScheme: ColorScheme,
    motionScheme: MotionScheme = MotionScheme.expressive(),
    shapes: Shapes = ExpressiveShapes,
    typography: Typography = ExpressiveTypography,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalMotionScheme provides motionScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            typography = typography,
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BmiPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        highContrast && darkTheme -> HighContrastDarkColorScheme
        highContrast && !darkTheme -> HighContrastLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ExpressiveDarkColorScheme
        else -> ExpressiveLightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = ExpressiveShapes,
        typography = ExpressiveTypography,
        content = content
    )
}

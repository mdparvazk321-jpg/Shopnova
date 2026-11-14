package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = NovaPink,
    onPrimary = NovaWhite,
    primaryContainer = NovaPinkSoft,
    onPrimaryContainer = NovaPinkDark,
    secondary = NovaTurquoise,
    onSecondary = NovaWhite,
    secondaryContainer = NovaCyanSoft,
    onSecondaryContainer = NovaNavy,
    tertiary = NovaCyan,
    onTertiary = NovaNavy,
    background = NovaBackground,
    onBackground = NovaNavy,
    surface = NovaWhite,
    onSurface = NovaNavy,
    surfaceVariant = NovaSurfaceVariant,
    onSurfaceVariant = NovaSlate,
    outline = NovaOutline,
    error = NovaError,
    onError = NovaWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = NovaPink,
    onPrimary = NovaWhite,
    primaryContainer = NovaPinkDark,
    onPrimaryContainer = NovaPinkSoft,
    secondary = NovaCyan,
    onSecondary = NovaNavy,
    secondaryContainer = NovaDarkSurfaceVariant,
    onSecondaryContainer = NovaCyan,
    tertiary = NovaTurquoise,
    onTertiary = NovaWhite,
    background = NovaDarkBackground,
    onBackground = NovaWhite,
    surface = NovaDarkSurface,
    onSurface = NovaWhite,
    surfaceVariant = NovaDarkSurfaceVariant,
    onSurfaceVariant = NovaCyanSoft,
    outline = NovaDarkOutline,
    error = NovaError,
    onError = NovaWhite
)

val ShopNovaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ShopNovaTheme(
    darkTheme: Boolean = false, // Crisp white-first e-commerce experience; toggleable if desired
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShopNovaTypography,
        shapes = ShopNovaShapes,
        content = content
    )
}

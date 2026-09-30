package com.lamz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.lamz.data.model.FontSize
import com.lamz.data.model.LauncherTheme

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledAccent,
    onPrimary = AmoledBackground,
    background = AmoledBackground,
    onBackground = AmoledTextPrimary,
    surface = AmoledSurface,
    onSurface = AmoledTextPrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledTextSecondary,
    outline = AmoledOutline
)

private val MinimalDarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline
)

private val MinimalLightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
)

private val MonochromeColorScheme = darkColorScheme(
    primary = MonoAccent,
    onPrimary = MonoBackground,
    background = MonoBackground,
    onBackground = MonoTextPrimary,
    surface = MonoSurface,
    onSurface = MonoTextPrimary,
    surfaceVariant = MonoSurfaceVariant,
    onSurfaceVariant = MonoTextSecondary,
    outline = MonoOutline
)

private val DeveloperColorScheme = darkColorScheme(
    primary = DevAccent,
    onPrimary = DevBackground,
    background = DevBackground,
    onBackground = DevTextPrimary,
    surface = DevSurface,
    onSurface = DevTextPrimary,
    surfaceVariant = DevSurfaceVariant,
    onSurfaceVariant = DevTextSecondary,
    outline = DevOutline,
    tertiary = DevGreen
)

@Composable
fun MinimalOSTheme(
    theme: LauncherTheme = LauncherTheme.AMOLED,
    fontSize: FontSize = FontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        LauncherTheme.AMOLED -> AmoledColorScheme
        LauncherTheme.MINIMAL_DARK -> MinimalDarkColorScheme
        LauncherTheme.MINIMAL_LIGHT -> MinimalLightColorScheme
        LauncherTheme.MONOCHROME -> MonochromeColorScheme
        LauncherTheme.DEVELOPER -> DeveloperColorScheme
    }

    val typography = createMinimalTypography(
        fontScale = fontSize.scaleFactor,
        fontFamily = if (theme == LauncherTheme.DEVELOPER) FontFamily.Monospace else FontFamily.SansSerif
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

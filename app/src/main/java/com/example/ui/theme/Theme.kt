package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LifeLinkLightColorScheme = lightColorScheme(
    primary = RedPrimary,
    onPrimary = CleanWhite,
    primaryContainer = RedContainer,
    onPrimaryContainer = OnRedContainer,
    secondary = RedDark,
    onSecondary = CleanWhite,
    secondaryContainer = Color(0xFFFFEBEE),
    onSecondaryContainer = RedDark,
    tertiary = Color(0xFF0288D1),
    onTertiary = CleanWhite,
    background = Color(0xFFFBFBFB),
    onBackground = TextPrimary,
    surface = CleanWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = TextSecondary,
    outline = BorderGray,
    error = UrgentCritical,
    onError = CleanWhite
)

private val LifeLinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    onPrimary = Color(0xFF490008),
    primaryContainer = Color(0xFF8C0014),
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = Color(0xFFFFB4AB),
    onSecondary = Color(0xFF561E19),
    tertiary = Color(0xFF81D4FA),
    onTertiary = Color(0xFF00344F),
    background = Color(0xFF141212),
    onBackground = Color(0xFFEDE0DF),
    surface = Color(0xFF1C1919),
    onSurface = Color(0xFFEDE0DF),
    surfaceVariant = Color(0xFF2B2525),
    onSurfaceVariant = Color(0xFFD7C1C0),
    outline = Color(0xFF4E4443),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun LifeLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LifeLinkDarkColorScheme else LifeLinkLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LifeLinkTheme(darkTheme = darkTheme, content = content)
}

package com.example.daybrief.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary               = Blue40,
    onPrimary             = Color.White,
    primaryContainer      = Blue90,
    onPrimaryContainer    = Blue10,
    secondary             = Color(0xFF545F71),
    onSecondary           = Color.White,
    secondaryContainer    = Color(0xFFD8E3F8),
    onSecondaryContainer  = Color(0xFF111C2B),
    tertiary              = Amber40,
    onTertiary            = Color.White,
    tertiaryContainer     = Color(0xFFFFDBCC),
    onTertiaryContainer   = Color(0xFF3A0A00),
    background            = Neutral99,
    onBackground          = Neutral10,
    surface               = Neutral99,
    onSurface             = Neutral10,
    surfaceVariant        = NeutralVar90,
    onSurfaceVariant      = Color(0xFF44474F),
    outline               = NeutralVar50,
    outlineVariant        = NeutralVar80,
)

private val DarkColorScheme = darkColorScheme(
    primary               = Blue80,
    onPrimary             = Blue20,
    primaryContainer      = Blue40,
    onPrimaryContainer    = Blue90,
    secondary             = Color(0xFFBCC8E0),
    onSecondary           = Color(0xFF263344),
    secondaryContainer    = Color(0xFF3C4858),
    onSecondaryContainer  = Color(0xFFD8E3F8),
    tertiary              = Amber80,
    onTertiary            = Color(0xFF5C1900),
    tertiaryContainer     = Color(0xFF833200),
    onTertiaryContainer   = Color(0xFFFFDBCC),
    background            = Neutral10,
    onBackground          = Neutral90,
    surface               = Neutral10,
    onSurface             = Neutral90,
    surfaceVariant        = Color(0xFF44474F),
    onSurfaceVariant      = NeutralVar80,
    outline               = NeutralVar60,
    outlineVariant        = Color(0xFF44474F),
)

@Composable
fun DayBriefTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

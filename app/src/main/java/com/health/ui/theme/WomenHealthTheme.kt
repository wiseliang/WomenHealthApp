package com.health.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB03A5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF74565E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF2B151C),
    tertiary = Color(0xFF7D5731),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFFFFF8F8),
    onBackground = Color(0xFF22191B),
    surface = Color(0xFFFFF8F8),
    onSurface = Color(0xFF22191B),
    surfaceVariant = Color(0xFFF3DDE2),
    onSurfaceVariant = Color(0xFF514347),
    error = Color(0xFFBA1A1A),
    outline = Color(0xFF837377),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1C5),
    onPrimary = Color(0xFF65002E),
    primaryContainer = Color(0xFF8E1F44),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFE4BEC6),
    onSecondary = Color(0xFF422931),
    secondaryContainer = Color(0xFF5B3F47),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFEEBD92),
    onTertiary = Color(0xFF482A08),
    tertiaryContainer = Color(0xFF62401C),
    background = Color(0xFF1B1113),
    onBackground = Color(0xFFEDE0E2),
    surface = Color(0xFF1B1113),
    onSurface = Color(0xFFEDE0E2),
    surfaceVariant = Color(0xFF514347),
    onSurfaceVariant = Color(0xFFD5C1C6),
    error = Color(0xFFFFB4AB),
    outline = Color(0xFF9E8C90),
)

@Composable
fun WomenHealthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

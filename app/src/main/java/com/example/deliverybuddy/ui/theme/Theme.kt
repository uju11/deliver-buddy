package com.example.deliverybuddy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE9ECEF),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF343A40),
    onPrimaryContainer = Color(0xFFF8F9FA),
    secondary = Color(0xFFADB5BD),
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF2A2A2A),
    onSecondaryContainer = Color(0xFFE9ECEF),
    background = NeutralDarkBackground,
    onBackground = TextPrimaryDark,
    surface = NeutralDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NeutralDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF212529),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9ECEF),
    onPrimaryContainer = Color(0xFF212529),
    secondary = Color(0xFF495057),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF1F3F5),
    onSecondaryContainer = Color(0xFF212529),
    background = NeutralLightBackground,
    onBackground = TextPrimaryLight,
    surface = NeutralLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = NeutralLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = NeutralLightOutline
)

@Composable
fun DeliveryBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to clean minimalist custom neutral scheme, allow opt-in if dynamic requested
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

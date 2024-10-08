package com.pabloboo.runtracker.ui.theme

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
    primary = Color(0xFF0F8DE7), // Primary color used for main buttons, app bars, and highlighted elements.
    secondary = Color(0xFFFBA105), // Secondary color for secondary actions, icons, and complementary elements.
    tertiary = Color(0xFFF32848),  // Tertiary color for alerts, notifications, and specific call-to-action buttons.
    background = Color(0xFF121212), // Background color for the main app screen in dark mode.
    surface = Color(0xFF1E1E1E), // Surface color for cards, lists, and grouped information areas in dark mode.
    onPrimary = Color.White, // Text and icon color on primary-colored components.
    onSecondary = Color.Black, // Text and icon color on secondary-colored components.
    onTertiary = Color.Black, // Text and icon color on tertiary-colored components.
    onBackground = Color(0xFFF5F5F5), // Text color on the main background in dark mode.
    onSurface = Color(0xFFF5F5F5) // Text color on surfaces like cards and dialogs in dark mode.
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF119AE8), // Primary color used for main buttons, app bars, and highlighted elements.
    secondary = Color(0xFFFEA203), // Secondary color for secondary actions, icons, and complementary elements.
    tertiary = Color(0xFFFE1F4E), // Tertiary color for alerts, notifications, and specific call-to-action buttons.
    background = Color(0xFFFFFFFF), // Background color for the main app screen.
    surface = Color(0xFFF5F5F5), // Surface color for cards, lists, and grouped information areas.
    onPrimary = Color.White, // Text and icon color on primary-colored components.
    onSecondary = Color.White, // Text and icon color on secondary-colored components.
    onTertiary = Color.White, // Text and icon color on tertiary-colored components.
    onBackground = Color(0xFF121212), // Text color on the main background.
    onSurface = Color(0xFF121212) // Text color on surfaces like cards and dialogs.
)

@Composable
fun RunTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
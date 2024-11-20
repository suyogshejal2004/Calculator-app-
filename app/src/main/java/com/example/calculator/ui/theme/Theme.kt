package com.example.calculator.ui.theme

import android.app.Activity
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
    primary = Color(0xFF00C853),      // Vibrant Green
    secondary = Color(0xFF2D2D2D),    // Dark Gray
    tertiary = Color(0xFF404040),     // Medium Gray
    background = Color(0xFF121212),   // Dark Background
    surface = Color(0xFF1E1E1E),      // Slightly Lighter Background
    error = Color(0xFFCF6679),        // Error Red
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00B049),      // Slightly Darker Green
    secondary = Color(0xFFF5F5F5),    // Light Gray
    tertiary = Color(0xFFE0E0E0),     // Medium Gray
    background = Color.White,         // White Background
    surface = Color(0xFFFAFAFA),      // Slightly Off-White
    error = Color(0xFFB00020),        // Error Red
    onPrimary = Color.White,
    onSecondary = Color(0xFF1A1A1A),  // Dark Text
    onTertiary = Color(0xFF1A1A1A),   // Dark Text
    onBackground = Color(0xFF1A1A1A), // Dark Text
    onSurface = Color(0xFF1A1A1A),    // Dark Text
    onError = Color.White
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
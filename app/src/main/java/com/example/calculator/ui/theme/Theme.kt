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
import androidx.core.graphics.ColorUtils

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),     // Green
    secondary = Color(0xFF2C2C2C),   // Dark Gray for number buttons
    tertiary = Color(0xFF3F3F3F),    // Slightly lighter gray for operators
    background = Color(0xFF121212),  // Dark background
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679)        // Red for clear button
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),     // Green
    secondary = Color(0xFFE8E8E8),   // Light Gray for number buttons
    tertiary = Color(0xFFD3D3D3),    // Slightly darker gray for operators
    background = Color(0xFFFAFAFA),  // Light background
    surface = Color(0xFFFAFAFA),
    error = Color(0xFFB00020)        // Red for clear button
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
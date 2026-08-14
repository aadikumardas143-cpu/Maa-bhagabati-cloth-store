package com.example.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = Maroon80,
    secondary = Gold80,
    tertiary = Magenta80,
    background = Color(0xFF1F030A),
    surface = Color(0xFF2B0A12),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFFCE4E8),
    onSurface = Color(0xFFFCE4E8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalMaroon,
    secondary = ZariGold,
    tertiary = DeepMagenta,
    background = SilkOffWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFF21191B),
    onSurface = Color(0xFF21191B)
  )

@Composable
fun MaaBhagabatiTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaaBhagabatiTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}


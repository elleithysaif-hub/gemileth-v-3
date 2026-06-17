package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    tertiary = Pink80,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDarkVariant,
    onPrimary = SurfaceDark,
    onSecondary = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryHighContrast,
    secondary = SecondaryHighContrast,
    tertiary = Pink40,
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceLightVariant,
    onPrimary = SurfaceLight,
    onSecondary = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextPrimaryLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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

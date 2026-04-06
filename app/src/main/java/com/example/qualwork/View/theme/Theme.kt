package com.example.qualwork.View.theme

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
    primary = redColor,
    onPrimary = textColorWhite,

    background = backgroundColorDark,
    onSurface = textColorWhite,
    onBackground = textColorWhite,
    error =  redColor,
)

private val LightColorScheme = lightColorScheme(
    primary = redColor,
    onPrimary = textColorWhite,

    background = backgroundColorLight,
    onSurface = textColorBlack,
    onBackground = textColorBlack,
    outline = textColorBlack,
    error =  redColor,


)

@Composable
fun QualWorkTheme(
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
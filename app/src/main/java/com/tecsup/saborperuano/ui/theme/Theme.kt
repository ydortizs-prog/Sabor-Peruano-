package com.tecsup.saborperuano.ui.theme

import androidx.compose.ui.graphics.Color
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
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun SaborPeruanoTheme(
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

private val CatalogoClaro = lightColorScheme(
    primary = Color(0xFF8D2828),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD7),
    onPrimaryContainer = Color(0xFF3B0808),
    secondary = Color(0xFF45614A),
    secondaryContainer = Color(0xFFD7E8D3),
    onSecondaryContainer = Color(0xFF17291B),
    tertiaryContainer = Color(0xFFFBE4AF),
    onTertiaryContainer = Color(0xFF362706),
    background = Color(0xFFFFF8EE),
    surface = Color(0xFFFFFCF7),
    onSurface = Color(0xFF29201B),
    onSurfaceVariant = Color(0xFF594741)
)

private val CatalogoOscuro = darkColorScheme(
    primary = Color(0xFFFFB4AC),
    onPrimary = Color(0xFF551716),
    primaryContainer = Color(0xFF75302D),
    onPrimaryContainer = Color(0xFFFFDAD7),
    secondary = Color(0xFFB7D5B5),
    secondaryContainer = Color(0xFF314B35),
    onSecondaryContainer = Color(0xFFD7E8D3),
    tertiaryContainer = Color(0xFF523E18),
    onTertiaryContainer = Color(0xFFFBE4AF),
    background = Color(0xFF191310),
    surface = Color(0xFF241C18),
    onSurface = Color(0xFFF5E7DC),
    onSurfaceVariant = Color(0xFFD8C2B8)
)

@Composable
fun TemaCatalogo(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CatalogoOscuro else CatalogoClaro,
        typography = Typography,
        content = content
    )
}

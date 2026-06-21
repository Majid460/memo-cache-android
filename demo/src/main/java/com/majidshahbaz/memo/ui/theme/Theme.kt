package com.majidshahbaz.memo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonElectricPurple,
    secondary = UserBubbleColor,
    tertiary = BotTextColor,
    background = DarkBackground,
    surface = SurfaceCardColor,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BotTextColor,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun MemoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}

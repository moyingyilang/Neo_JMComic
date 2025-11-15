package com.yourpackage.neojmcomic.utils.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 全局主题配置（整合调色板、形状、间距） */
object UiTheme {
    // 统一间距常量
    val spacing: UiSpacing = UiSpacing(
        xs = 4.dp,
        sm = 8.dp,
        md = 16.dp,
        lg = 24.dp,
        xl = 32.dp
    )

    // 形状配置
    val shapes: UiShapes = UiShapes(
        small = 4.dp,
        medium = 8.dp,
        large = 16.dp
    )

    // 明暗模式颜色方案
    private val LightColorScheme = lightColorScheme(
        primary = UiPalette.Primary,
        secondary = UiPalette.Secondary,
        error = UiPalette.Error,
        background = UiPalette.Background.Light,
        surface = UiPalette.Surface.Light,
        onPrimary = UiPalette.OnPrimary,
        onBackground = UiPalette.OnBackground.Light
    )

    private val DarkColorScheme = darkColorScheme(
        primary = UiPalette.Primary,
        secondary = UiPalette.Secondary,
        error = UiPalette.Error,
        background = UiPalette.Background.Dark,
        surface = UiPalette.Surface.Dark,
        onPrimary = UiPalette.OnPrimary,
        onBackground = UiPalette.OnBackground.Dark
    )

    @Composable
    fun UiTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
        MaterialTheme(
            colorScheme = colorScheme,
            typography = UiTypography.typography,
            shapes = androidx.compose.material3.Shapes(
                small = androidx.compose.foundation.shape.RoundedCornerShape(shapes.small),
                medium = androidx.compose.foundation.shape.RoundedCornerShape(shapes.medium),
                large = androidx.compose.foundation.shape.RoundedCornerShape(shapes.large)
            ),
            content = content
        )
    }

    // 间距数据类
    data class UiSpacing(
        val xs: Dp,
        val sm: Dp,
        val md: Dp,
        val lg: Dp,
        val xl: Dp
    )

    // 形状数据类
    data class UiShapes(
        val small: Dp,
        val medium: Dp,
        val large: Dp
    )
}

/** 调色板（整合palette.txt） */
object UiPalette {
    val Primary = androidx.compose.ui.graphics.Color(0xFF6200EE)
    val Secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6)
    val Error = androidx.compose.ui.graphics.Color(0xFFB00020)
    val OnPrimary = androidx.compose.ui.graphics.Color.White

    object Background {
        val Light = androidx.compose.ui.graphics.Color(0xFFF5F5F5)
        val Dark = androidx.compose.ui.graphics.Color(0xFF121212)
    }

    object Surface {
        val Light = androidx.compose.ui.graphics.Color.White
        val Dark = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
    }

    object OnBackground {
        val Light = androidx.compose.ui.graphics.Color(0xFF000000)
        val Dark = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    }
}

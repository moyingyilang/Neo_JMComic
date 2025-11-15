package com.yourpackage.neojmcomic.utils.theme

import com.yourpackage.neojmcomic.utils.deepmerge
import com.yourpackage.neojmcomic.utils.theme.shape.ShapeConfig
import com.yourpackage.neojmcomic.utils.theme.breakpoint.BreakpointsConfig
import com.yourpackage.neojmcomic.utils.theme.spacing.SpacingConfig
import com.yourpackage.neojmcomic.utils.theme.palette.PaletteConfig
import com.yourpackage.neojmcomic.utils.theme.sx.StyleFunctionSx
import com.yourpackage.neojmcomic.utils.theme.sx.DefaultSxConfig
import com.yourpackage.neojmcomic.utils.theme.cssContainerQueries

/**
 * 主题配置数据类
 */
data class Theme(
    val breakpoints: BreakpointsConfig,
    val direction: String = "ltr",
    val components: MutableMap<String, Any> = mutableMapOf(),
    val palette: PaletteConfig,
    val spacing: SpacingConfig,
    val shape: ShapeConfig,
    val unstable_sxConfig: Map<String, Any> = DefaultSxConfig.config,
    val applyStyles: (Any) -> Unit = {}
) {
    /** 支持SX样式配置 */
    fun sx(props: Map<String, Any>): Map<String, Any> {
        return StyleFunctionSx.sx(props, this)
    }
}

/**
 * 创建应用主题
 * @param options 自定义主题配置
 * @param args 额外扩展配置
 * @return 合并后的主题实例
 */
fun createTheme(
    options: ThemeOptions = ThemeOptions(),
    vararg args: Map<String, Any>
): Theme {
    val breakpoints = BreakpointsConfig.create(options.breakpointsInput)
    val spacing = SpacingConfig.create(options.spacingInput)
    val shape = ShapeConfig.merge(ShapeConfig.default, options.shapeInput)
    val palette = PaletteConfig.merge(PaletteConfig.default, options.paletteInput)

    var theme = Theme(
        breakpoints = breakpoints,
        palette = palette,
        spacing = spacing,
        shape = shape
    )

    // 应用CSS容器查询配置
    theme = cssContainerQueries(theme)

    // 合并额外配置
    args.forEach { extraConfig ->
        theme = deepmerge(theme, extraConfig)
    }

    return theme
}

/** 主题创建选项 */
data class ThemeOptions(
    val breakpointsInput: Map<String, Any> = emptyMap(),
    val paletteInput: Map<String, Any> = emptyMap(),
    val spacingInput: Any? = null,
    val shapeInput: Map<String, Any> = emptyMap(),
    val unstable_sxConfig: Map<String, Any> = emptyMap()
)

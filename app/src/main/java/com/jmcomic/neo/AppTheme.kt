// 对应React的createTheme结构
data class AppTheme(
    val palette: Palette,
    val spacing: Spacing,
    val typography: Typography,
    val breakpoints: Breakpoints,
    val shape: Shape,
    val shadows: Shadows,
    val transitions: Transitions,
    val zIndex: ZIndex
) {
    // 对应React的unstable_sx功能
    fun sx(block: SxScope.() -> Unit): SxResult {
        return SxFunction.execute(this, block)
    }
}

// 调色板系统 - 对应React的palette
data class Palette(
    val mode: ColorMode,
    val primary: ColorScheme,
    val secondary: ColorScheme,
    val error: ColorScheme,
    val warning: ColorScheme,
    val info: ColorScheme,
    val success: ColorScheme,
    val background: BackgroundColors,
    val text: TextColors,
    val common: CommonColors,
    val grey: GreyPalette,
    val action: ActionColors
) {
    enum class ColorMode { LIGHT, DARK }
}

// 间距系统 - 对应React的spacing
class Spacing(private val base: Int = 8) {
    fun value(multiplier: Int): Int = base * multiplier
    fun value(multiplier: Float): Float = base * multiplier
    operator fun invoke(multiplier: Int): Int = value(multiplier)
    operator fun invoke(multiplier: Float): Float = value(multiplier)
}
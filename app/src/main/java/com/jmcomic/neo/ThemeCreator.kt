// 对应React的createTheme.js
object ThemeCreator {
    
    fun createTheme(options: ThemeOptions = ThemeOptions()): AppTheme {
        val breakpoints = createBreakpoints(options.breakpoints)
        val spacing = createSpacing(options.spacing)
        val palette = createPalette(options.palette)
        
        return AppTheme(
            palette = palette,
            spacing = spacing,
            typography = createTypography(palette, options.typography),
            breakpoints = breakpoints,
            shape = createShape(options.shape),
            shadows = createShadows(),
            transitions = createTransitions(options.transitions),
            zIndex = createZIndex()
        ).apply {
            // 对应React的cssContainerQueries和applyStyles
            applyContainerQueries()
            applyComponentStyles()
        }
    }
    
    fun createThemeWithVars(options: ThemeOptions = ThemeOptions()): AppTheme {
        val theme = createTheme(options)
        // 实现CSS变量支持 - 对应createThemeWithVars.js
        return theme.applyCssVars()
    }
    
    private fun createPalette(paletteInput: PaletteInput): Palette {
        return Palette(
            mode = paletteInput.mode ?: ColorMode.LIGHT,
            primary = createColorScheme(paletteInput.primary, DefaultColors.primary),
            secondary = createColorScheme(paletteInput.secondary, DefaultColors.secondary),
            error = createColorScheme(paletteInput.error, DefaultColors.error),
            warning = createColorScheme(paletteInput.warning, DefaultColors.warning),
            info = createColorScheme(paletteInput.info, DefaultColors.info),
            success = createColorScheme(paletteInput.success, DefaultColors.success),
            background = createBackgroundColors(paletteInput.mode ?: ColorMode.LIGHT),
            text = createTextColors(paletteInput.mode ?: ColorMode.LIGHT),
            common = createCommonColors(),
            grey = createGreyPalette(),
            action = createActionColors(paletteInput.mode ?: ColorMode.LIGHT)
        )
    }
    
    private fun createColorScheme(
        input: ColorSchemeInput?, 
        default: ColorScheme
    ): ColorScheme {
        return ColorScheme(
            main = input?.main ?: default.main,
            light = input?.light ?: default.light,
            dark = input?.dark ?: default.dark,
            contrastText = input?.contrastText ?: default.contrastText
        )
    }
}
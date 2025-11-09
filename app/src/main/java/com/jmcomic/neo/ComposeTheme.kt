// 对应React的ThemeProvider
@Composable
fun ComicAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val theme = remember(darkTheme) {
        if (darkTheme) ThemeManager.createDarkTheme() 
        else ThemeManager.createLightTheme()
    }
    
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(theme.palette.primary.main),
            onPrimary = Color(theme.palette.primary.contrastText),
            secondary = Color(theme.palette.secondary.main),
            onSecondary = Color(theme.palette.secondary.contrastText),
            background = Color(theme.palette.background.default),
            onBackground = Color(theme.palette.text.primary)
        )
    } else {
        lightColorScheme(
            primary = Color(theme.palette.primary.main),
            onPrimary = Color(theme.palette.primary.contrastText),
            secondary = Color(theme.palette.secondary.main),
            onSecondary = Color(theme.palette.secondary.contrastText),
            background = Color(theme.palette.background.default),
            onBackground = Color(theme.palette.text.primary)
        )
    }
    
    MaterialTheme(
        colorScheme = colors,
        typography = ComicTypography,
        content = content
    )
}

// 对应React的useTheme hook
@Composable
fun useTheme(): AppTheme {
    val isDark = isSystemInDarkTheme()
    return remember(isDark) {
        if (isDark) ThemeManager.createDarkTheme() 
        else ThemeManager.createLightTheme()
    }
}
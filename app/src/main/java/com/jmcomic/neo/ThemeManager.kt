// 对应React的主题状态管理
object ThemeManager {
    
    private var currentTheme: AppTheme = ThemeCreator.createTheme()
    private val themeListeners = mutableListOf<(AppTheme) -> Unit>()
    
    // 对应React的useTheme hook
    fun getCurrentTheme(): AppTheme = currentTheme
    
    fun isDarkTheme(): Boolean = currentTheme.palette.mode == ColorMode.DARK
    
    fun toggleDarkMode() {
        val newMode = if (isDarkTheme()) ColorMode.LIGHT else ColorMode.DARK
        switchTheme(newMode)
    }
    
    fun switchTheme(mode: ColorMode) {
        val newTheme = if (mode == ColorMode.DARK) {
            createDarkTheme()
        } else {
            createLightTheme()
        }
        setTheme(newTheme)
    }
    
    fun setTheme(theme: AppTheme) {
        currentTheme = theme
        notifyThemeChanged()
        persistTheme()
    }
    
    fun createLightTheme(): AppTheme {
        return ThemeCreator.createTheme(
            ThemeOptions(palette = PaletteInput(mode = ColorMode.LIGHT))
        )
    }
    
    fun createDarkTheme(): AppTheme {
        return ThemeCreator.createTheme(
            ThemeOptions(palette = PaletteInput(mode = ColorMode.DARK))
        )
    }
    
    fun addThemeListener(listener: (AppTheme) -> Unit) {
        themeListeners.add(listener)
    }
    
    fun removeThemeListener(listener: (AppTheme) -> Unit) {
        themeListeners.remove(listener)
    }
    
    private fun notifyThemeChanged() {
        themeListeners.forEach { it(currentTheme) }
    }
    
    private fun persistTheme() {
        // 保存主题设置到SharedPreferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            ContextProvider.applicationContext
        )
        prefs.edit().putBoolean("is_dark_theme", isDarkTheme()).apply()
    }
    
    // 对应React的sx prop功能
    fun resolveSx(context: Context, sxConfig: SxConfig): TypedArray {
        return SxFunction.resolve(context, currentTheme, sxConfig)
    }
}
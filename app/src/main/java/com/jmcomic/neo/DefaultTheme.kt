package com.yourpackage.neojmcomic.utils.theme

/**
 * 默认主题实例（基于createTheme创建，开箱即用）
 */
val defaultTheme: Theme by lazy {
    createTheme()
}

/**
 * 根据系统模式（亮色/暗色）创建默认主题
 * @param darkMode 是否深色模式
 */
fun createDefaultTheme(darkMode: Boolean = false): Theme {
    return createTheme(
        ThemeOptions(
            paletteInput = mapOf(
                "mode" to if (darkMode) "dark" else "light"
            )
        )
    )
}

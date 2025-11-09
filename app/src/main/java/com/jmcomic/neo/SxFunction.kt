// 对应React的sx prop和styleFunctionSx
object SxFunction {
    
    fun execute(theme: AppTheme, block: SxScope.() -> Unit): SxResult {
        val scope = SxScope(theme)
        scope.block()
        return scope.build()
    }
    
    fun resolve(context: Context, theme: AppTheme, sxConfig: SxConfig): TypedArray {
        return context.obtainStyledAttributes(
            sxConfig.attrs,
            sxConfig.styleAttrs
        ).apply {
            // 应用主题相关的样式
            applyThemeStyles(theme, sxConfig)
        }
    }
    
    class SxScope(private val theme: AppTheme) {
        private val styles = mutableMapOf<String, Any>()
        
        // 对应React的sx prop中的spacing
        fun spacing(value: Int) {
            styles["padding"] = theme.spacing(value)
        }
        
        fun spacing(value: Float) {
            styles["padding"] = theme.spacing(value)
        }
        
        // 对应颜色系统
        fun color(color: String) {
            when (color) {
                "primary.main" -> styles["color"] = theme.palette.primary.main
                "secondary.main" -> styles["color"] = theme.palette.secondary.main
                "text.primary" -> styles["color"] = theme.palette.text.primary
                // ... 其他颜色映射
            }
        }
        
        fun build(): SxResult {
            return SxResult(styles.toMap())
        }
    }
}
// App.kt
class App : Application() {
    
    companion object {
        lateinit var instance: App
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化全局配置
        GlobalConfigManager.initialize(this)
        
        // 初始化分析工具
        AnalyticsManager.initialize(this)
        
        // 检查三天免打扰
        AuthManager.checkThreeDaysExpiry()
    }
}
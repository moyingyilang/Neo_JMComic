/**
 * 🎯 应用初始化管理器
 * 📋 对应React Native的App.tsx初始化逻辑
 */
object AppInitializer {
    
    private const val TAG = "AppInitializer"
    
    /**
     * 🎯 完整应用初始化
     */
    fun initialize(application: Application) {
        initializeSecurity(application)
        initializeAnalytics(application)
        initializeHost()
        initializeHotUpdate()
        initializeGlobalConfig(application)
        setupErrorHandling()
    }
    
    /**
     * 🎯 初始化安全防护
     * 📋 对应React Native的usePWAProtection, useDevtoolsBlocker
     */
    private fun initializeSecurity(application: Application) {
        // PWA保护 - Android不需要
        AppLogger.d("PWA protection not needed on Android")
        
        // 开发者工具检测
        SecurityManager.enableDevToolsBlocker(application as? Activity)
        
        // 返回按钮退出确认
        // 在MainActivity中实现
        
        AppLogger.d("Security initialization completed")
    }
    
    /**
     * 🎯 初始化分析工具
     * 📋 对应React Native的Clarity和usePageTracking
     */
    private fun initializeAnalytics(application: Application) {
        if (BuildConfig.DEBUG) {
            AppLogger.d("Development Mode - Analytics disabled")
        } else {
            // 初始化分析工具
            AnalyticsManager.initialize(application)
            
            // 可以在这里添加其他分析工具
            // ClarityManager.initialize(application)
            
            AppLogger.d("Analytics initialization completed")
        }
    }
    
    /**
     * 🎯 初始化主机配置
     * 📋 对应React Native的fetchHostData逻辑
     */
    private fun initializeHost() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppLogger.d("Starting host initialization...")
                
                val hostData = ApiEndpoints.fetchHostData()
                val apiUrl = ApiEndpoints.setGlobalHostFromData(hostData)
                
                AppLogger.d("Host initialization completed: $apiUrl")
                
                // 通知全局配置更新
                GlobalConfigManager.updateApiUrl(apiUrl)
                GlobalConfigManager.updateHostServer(hostData.jm3Server)
                
            } catch (e: Exception) {
                AppLogger.e("Host initialization failed", e)
                // 可以在这里处理初始化失败的情况
                EventBus.post(HostInitializationFailedEvent(e))
            }
        }
    }
    
    /**
     * 🎯 初始化热更新
     * 📋 对应React Native的initHotUpdate
     */
    private fun initializeHotUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppLogger.d("Checking for hot updates...")
                
                HotUpdateManager.checkForUpdates { updateAvailable ->
                    if (updateAvailable) {
                        AppLogger.d("Hot update available")
                        // 显示更新对话框
                        EventBus.post(ShowUpdateDialogEvent)
                    } else {
                        AppLogger.d("No hot updates available")
                    }
                }
                
            } catch (e: Exception) {
                AppLogger.e("Hot update check failed", e)
            }
        }
    }
    
    /**
     * 🎯 初始化全局配置
     */
    private fun initializeGlobalConfig(application: Application) {
        GlobalConfigManager.initialize(application)
        AuthManager.initialize(application)
        
        // 检查三天免打扰
        AuthManager.checkThreeDaysExpiry()
        
        AppLogger.d("Global config initialization completed")
    }
    
    /**
     * 🎯 设置错误处理
     * 📋 对应React Native的window.onerror
     */
    private fun setupErrorHandling() {
        // 设置全局未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e("Uncaught exception in thread: ${thread.name}", throwable)
            
            // 记录错误日志
            ErrorHandler.logError(throwable)
            
            // 可以跳转到错误页面或显示错误对话框
            EventBus.post(AppCrashEvent(throwable))
            
            // 默认处理：退出应用
            Process.killProcess(Process.myPid())
            System.exit(1)
        }
        
        AppLogger.d("Error handling setup completed")
    }
}

/**
 * 🎯 应用事件定义
 */
sealed class AppEvent {
    data class HostInitializationFailedEvent(val exception: Exception) : AppEvent()
    object ShowUpdateDialogEvent : AppEvent()
    data class AppCrashEvent(val throwable: Throwable) : AppEvent()
}
/**
 * 🎯 应用类 - 完善版本
 * 📋 对应React Native的App.tsx
 */
class App : Application() {
    
    companion object {
        lateinit var instance: App
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        AppLogger.d("App onCreate started")
        
        // 执行完整初始化
        AppInitializer.initialize(this)
        
        // 设置开发模式日志
        if (BuildConfig.DEBUG) {
            AppLogger.d("Running in Development Mode!")
        } else {
            AppLogger.d("Running in Production Mode!")
        }
        
        AppLogger.d("App onCreate completed")
    }
    
    /**
     * 🎯 获取应用版本信息
     * 📋 对应React Native的getLocalVersion
     */
    fun getLocalVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    /**
     * 🎯 检查环境配置
     */
    fun checkEnvironment() {
        when {
            BuildConfig.DEBUG -> {
                AppLogger.d("Debug build - Analytics and some features are disabled")
            }
            else -> {
                AppLogger.d("Release build - All features enabled")
            }
        }
    }
}
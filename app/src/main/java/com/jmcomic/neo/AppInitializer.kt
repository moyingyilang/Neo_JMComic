// manager/AppInitializer.kt
object AppInitializer {
    
    fun initialize(application: Application) {
        initializeGlobalConfig(application)
        initializeAnalytics(application)
        initializeHost()
        initializeHotUpdate()
        setupErrorHandling()
        initializeSecurity()
    }
    
    private fun initializeGlobalConfig(application: Application) {
        GlobalConfigManager.initialize(application)
        AuthManager.initialize(application)
    }
    
    private fun initializeAnalytics(application: Application) {
        if (BuildConfig.DEBUG) {
            Log.d("App", "Development Mode - Analytics disabled")
        } else {
            AnalyticsManager.initialize(application)
            // Clarity 分析初始化
            ClarityManager.initialize(application)
        }
    }
    
    private fun initializeHost() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hostData = HostManager.fetchHostData()
                GlobalConfigManager.updateApiUrl(hostData.baseUrl)
                GlobalConfigManager.updateHostServer(hostData.servers)
            } catch (e: Exception) {
                Log.e("AppInitializer", "Host initialization failed", e)
            }
        }
    }
    
    private fun initializeHotUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            HotUpdateManager.checkForUpdates { updateAvailable ->
                if (updateAvailable) {
                    // 显示更新对话框
                    EventBus.post(ShowUpdateDialogEvent)
                }
            }
        }
    }
    
    private fun setupErrorHandling() {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("App", "Uncaught exception", throwable)
            ErrorHandler.logError(throwable)
            // 可以跳转到错误页面
            EventBus.post(AppCrashEvent(throwable))
        }
    }
    
    private fun initializeSecurity() {
        SecurityManager.enableProtection()
        SecurityManager.blockDevTools()
    }
}
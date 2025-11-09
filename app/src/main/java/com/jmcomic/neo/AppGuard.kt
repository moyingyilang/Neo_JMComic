// security/AppGuard.kt
object AppGuard {
    
    fun enablePWAProtection() {
        // 防止PWA相关安全问题
        WebViewSecurity.enableProtection()
    }
    
    fun blockDevTools() {
        // 阻止开发者工具
        if (BuildConfig.DEBUG.not()) {
            DevToolsBlocker.enable()
        }
    }
    
    fun handleBackButtonExit(activity: Activity): Boolean {
        return BackButtonExitHandler.handle(activity)
    }
}

class BackButtonExitHandler(private val activity: Activity) {
    private var backPressedTime = 0L
    private val exitInterval = 2000L // 2秒内再次点击退出
    
    fun handle(): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - backPressedTime < exitInterval) {
            activity.finish()
            return true
        } else {
            backPressedTime = currentTime
            Toast.makeText(activity, "再按一次退出应用", Toast.LENGTH_SHORT).show()
            return true
        }
    }
    
    companion object {
        fun handle(activity: Activity): Boolean {
            return BackButtonExitHandler(activity).handle()
        }
    }
}
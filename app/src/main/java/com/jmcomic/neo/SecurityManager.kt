// SecurityManager.kt
object SecurityManager {
    
    // PWA保护 - Android不需要，因为是原生应用
    fun enablePWAProtection() {
        // 在Android中不需要实现
    }
    
    // 开发者工具检测
    fun enableDevToolsBlocker(activity: Activity) {
        if (BuildConfig.DEBUG) return // 调试模式不启用
        
        // 检测是否开启了USB调试
        val usbDebugging = Settings.Global.getInt(
            activity.contentResolver,
            Settings.Global.ADB_ENABLED, 0
        ) == 1
        
        if (usbDebugging) {
            // 可以记录日志或采取其他措施
            Log.w("Security", "USB debugging detected")
        }
        
        // 检测应用是否可调试
        if (activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            // 在发布版本中不应该出现这种情况
            Log.e("Security", "App is debuggable in release build!")
        }
    }
    
    // 返回按钮退出确认
    class BackButtonHandler(private val activity: Activity) {
        
        private var backPressCount = 0
        private val backPressThreshold = 2000L // 2秒内按两次退出
        private var lastBackPressTime = 0L
        
        fun handleBackPress(): Boolean {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastBackPressTime > backPressThreshold) {
                // 第一次按返回键
                backPressCount = 1
                lastBackPressTime = currentTime
                showExitToast()
                return true // 消费掉事件，不立即退出
            } else {
                // 第二次按返回键
                if (backPressCount == 1) {
                    activity.finish()
                    return true
                }
            }
            return false
        }
        
        private fun showExitToast() {
            Toast.makeText(activity, "再按一次退出应用", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 根检测
    fun isDeviceRooted(): Boolean {
        // 检查常见的root二进制文件
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        return paths.any { File(it).exists() }
    }
    
    // 模拟器检测
    fun isRunningOnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
               Build.FINGERPRINT.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("unknown") ||
               Build.HARDWARE.contains("goldfish") ||
               Build.HARDWARE.contains("ranchu") ||
               Build.MODEL.contains("google_sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK built for") ||
               Build.MANUFACTURER.contains("Genymotion") ||
               Build.PRODUCT.contains("sdk_google") ||
               Build.PRODUCT.contains("google_sdk") ||
               Build.PRODUCT.contains("sdk") ||
               Build.PRODUCT.contains("sdk_x86") ||
               Build.PRODUCT.contains("vbox86p") ||
               Build.PRODUCT.contains("emulator") ||
               Build.PRODUCT.contains("simulator")
    }
}
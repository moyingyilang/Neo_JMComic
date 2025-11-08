// HotUpdateManager.kt
object HotUpdateManager {
    
    private const val PREFS_HOT_UPDATE = "hot_update_prefs"
    private const val KEY_LOCAL_VERSION = "buildVersion"
    private const val KEY_UPDATE_PREFIX = "update_"
    
    private val DEFAULT_VERSION = BuildConfig.VERSION_NAME
    
    // 初始化热更新
    suspend fun initHotUpdate(context: Context) {
        if (!isNativePlatform()) {
            // Web环境不执行热更新
            return
        }
        
        val localVersion = getLocalVersion()
        val remoteVersion = fetchRemoteVersion()
        
        if (isNewerVersion(remoteVersion, localVersion)) {
            showUpdateDialog(context, remoteVersion)
        } else {
            // 检查缓存是否有效
            val cacheFolder = "$KEY_UPDATE_PREFIX$localVersion/build"
            val cachedFlag = getUpdateFlag(localVersion)
            
            if (cachedFlag == "true") {
                val cacheExists = checkCacheExists(context, cacheFolder)
                if (cacheExists) {
                    // 使用缓存版本
                    applyCachedUpdate(context, cacheFolder)
                } else {
                    // 缓存丢失，重新检查更新
                    forceUpdateCheck(context)
                }
            }
        }
    }
    
    // 获取本地版本
    suspend fun getLocalVersion(): String {
        val prefs = getHotUpdatePreferences()
        return prefs.getString(KEY_LOCAL_VERSION, DEFAULT_VERSION) ?: DEFAULT_VERSION
    }
    
    // 获取远程版本
    private suspend fun fetchRemoteVersion(): String {
        return withContext(Dispatchers.IO) {
            try {
                val versionUrl = "${GlobalStore.apiUrl}static/jmapp3apk/version.json"
                val response = HttpClient.get(versionUrl)
                if (response.isSuccessful) {
                    val json = response.body?.string() ?: "{}"
                    val data = Gson().fromJson(json, VersionResponse::class.java)
                    data.version ?: DEFAULT_VERSION
                } else {
                    DEFAULT_VERSION
                }
            } catch (e: Exception) {
                Log.e("HotUpdate", "Fetch remote version failed", e)
                DEFAULT_VERSION
            }
        }
    }
    
    // 版本比较
    private fun isNewerVersion(remote: String, local: String): Boolean {
        val r = remote.split('.').map { it.toIntOrNull() ?: 0 }
        val l = local.split('.').map { it.toIntOrNull() ?: 0 }
        
        // 确保都是3位版本号
        val remoteParts = r + List(3 - r.size) { 0 }
        val localParts = l + List(3 - l.size) { 0 }
        
        for (i in 0 until 3) {
            if (remoteParts[i] > localParts[i]) return true
            if (remoteParts[i] < localParts[i]) return false
        }
        return false
    }
    
    // 显示更新对话框
    private fun showUpdateDialog(context: Context, newVersion: String) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("发现新版本")
            .setMessage("版本: $newVersion\n是否立即更新？")
            .setPositiveButton("立即更新") { _, _ ->
                startUpdateFlow(context, newVersion)
            }
            .setNegativeButton("稍后", null)
            .setCancelable(false)
            .create()
        
        dialog.show()
    }
    
    // 开始更新流程
    private fun startUpdateFlow(context: Context, newVersion: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateProgress(25)
                val encryptedData = downloadUpdate(newVersion)
                
                updateProgress(50)
                val decryptionKey = fetchDecryptionKey()
                
                updateProgress(75)
                val decryptedData = decryptUpdate(encryptedData, decryptionKey)
                
                updateProgress(85)
                unzipToCache(context, decryptedData, newVersion)
                
                updateProgress(95)
                cleanOldCaches(context, newVersion)
                setLocalVersion(newVersion)
                setUpdateFlag(newVersion, "true")
                
                updateProgress(100)
                applyUpdate(context, newVersion)
                
            } catch (e: Exception) {
                Log.e("HotUpdate", "Update failed", e)
                updateProgress(-1) // 错误
                Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // 下载更新包
    private suspend fun downloadUpdate(version: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val zipUrl = "${GlobalStore.apiUrl}static/jmapp3apk/build.enc?v=$version"
            val response = HttpClient.get(zipUrl)
            if (response.isSuccessful) {
                response.body?.bytes() ?: throw IllegalStateException("下载数据为空")
            } else {
                throw IllegalStateException("下载失败: ${response.code}")
            }
        }
    }
    
    // 获取解密密钥
    private suspend fun fetchDecryptionKey(): DecryptionKey {
        return withContext(Dispatchers.IO) {
            val keyUrl = "${GlobalStore.apiUrl}static/jmapp3apk/encrypt_key.json"
            val response = HttpClient.get(keyUrl)
            if (response.isSuccessful) {
                val json = response.body?.string() ?: "{}"
                Gson().fromJson(json, DecryptionKey::class.java)
            } else {
                throw IllegalStateException("获取解密密钥失败")
            }
        }
    }
    
    // 解密数据
    private fun decryptUpdate(encryptedData: ByteArray, key: DecryptionKey): ByteArray {
        // 实现AES解密逻辑
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(hexStringToByteArray(key.keyHex), "AES")
        val ivSpec = IvParameterSpec(hexStringToByteArray(key.ivHex))
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(encryptedData)
    }
    
    // 解压到缓存
    private fun unzipToCache(context: Context, data: ByteArray, version: String) {
        val folder = File(context.cacheDir, "$KEY_UPDATE_PREFIX$version")
        if (folder.exists()) {
            folder.deleteRecursively()
        }
        folder.mkdirs()
        
        ZipInputStream(ByteArrayInputStream(data)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val file = File(folder, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                entry = zis.nextEntry
            }
        }
    }
    
    // 应用更新
    private fun applyUpdate(context: Context, version: String) {
        val updateFolder = File(context.cacheDir, "$KEY_UPDATE_PREFIX$version/build")
        if (updateFolder.exists() && updateFolder.isDirectory) {
            // 如果是WebView应用，可以在这里设置新的baseUrl
            // 对于原生应用，可能需要重启应用或动态加载资源
            Toast.makeText(context, "更新完成，请重启应用", Toast.LENGTH_SHORT).show()
        }
    }
    
    // 工具函数
    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + 
                           Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }
    
    private fun setLocalVersion(version: String) {
        val prefs = getHotUpdatePreferences()
        prefs.edit().putString(KEY_LOCAL_VERSION, version).apply()
    }
    
    private fun setUpdateFlag(version: String, flag: String) {
        val prefs = getHotUpdatePreferences()
        prefs.edit().putString("$KEY_UPDATE_PREFIX$version", flag).apply()
    }
    
    private fun getUpdateFlag(version: String): String? {
        val prefs = getHotUpdatePreferences()
        return prefs.getString("$KEY_UPDATE_PREFIX$version", null)
    }
    
    private fun getHotUpdatePreferences(): SharedPreferences {
        return App.context.getSharedPreferences(PREFS_HOT_UPDATE, Context.MODE_PRIVATE)
    }
    
    private fun isNativePlatform(): Boolean {
        return true // Android总是原生平台
    }
    
    private fun updateProgress(progress: Int) {
        // 发送进度更新，可以通过LiveData或回调通知UI
    }
    
    private fun checkCacheExists(context: Context, path: String): Boolean {
        return File(context.cacheDir, path).exists()
    }
    
    private fun applyCachedUpdate(context: Context, cachePath: String) {
        // 应用缓存的更新
        applyUpdate(context, cachePath.removePrefix("$KEY_UPDATE_PREFIX").removeSuffix("/build"))
    }
    
    private fun forceUpdateCheck(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            initHotUpdate(context)
        }
    }
    
    private fun cleanOldCaches(context: Context, currentVersion: String) {
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith(KEY_UPDATE_PREFIX) && 
                !file.name.contains(currentVersion)) {
                file.deleteRecursively()
                
                // 清除对应的preference标记
                val prefs = getHotUpdatePreferences()
                prefs.edit().remove(file.name).apply()
            }
        }
    }
}

data class VersionResponse(val version: String?)
data class DecryptionKey(val ivHex: String, val keyHex: String)
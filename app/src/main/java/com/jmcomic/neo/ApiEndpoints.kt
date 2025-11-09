/**
 * 🎯 API端点管理工具
 * 📋 对应React Native的ApiEndpointUtil.ts
 * 🔄 统一管理所有API端点，提供动态主机配置
 */
object ApiEndpoints {
    
    // 使用现有的ApiPaths常量
    private var baseUrl: String = ""
    
    /**
     * 🎯 设置基础URL
     */
    fun setBaseUrl(url: String) {
        baseUrl = url
        AppLogger.d("API base URL set to: $url")
    }
    
    /**
     * 🎯 获取完整的API端点URL
     */
    fun getEndpoint(key: String): String {
        if (baseUrl.isEmpty()) {
            throw IllegalStateException("API endpoints 尚未初始化，请先调用setBaseUrl()")
        }
        
        val path = getApiPath(key)
        return "$baseUrl$path"
    }
    
    /**
     * 🎯 根据key获取API路径
     */
    private fun getApiPath(key: String): String {
        return when (key) {
            // 首页相关
            "API_COMIC_PROMOTE" -> ApiPaths.API_COMIC_PROMOTE
            "API_COMIC_LATEST" -> ApiPaths.API_COMIC_LATEST
            "API_COMIC_PROMOTE_LIST" -> ApiPaths.API_COMIC_PROMOTE_LIST
            "API_COMIC_SER_MORE_LIST" -> ApiPaths.API_COMIC_SER_MORE_LIST
            
            // 广告相关
            "API_ADVERTISE_ALL" -> ApiPaths.API_ADVERTISE_ALL
            "API_ADVERTISE_CONTENT_COVER" -> ApiPaths.API_ADVERTISE_CONTENT_COVER
            
            // 搜索相关
            "API_COMIC_SEARCH" -> ApiPaths.API_COMIC_SEARCH
            "API_COMIC_HOT_TAGS" -> ApiPaths.API_COMIC_HOT_TAGS
            "API_COMIC_RANDOM_RECOMMEND" -> ApiPaths.API_COMIC_RANDOM_RECOMMEND
            
            // 漫画详情
            "API_COMIC_DETAIL" -> ApiPaths.API_COMIC_DETAIL
            "API_COMIC_CHAPTER" -> ApiPaths.API_COMIC_CHAPTER
            "API_COMIC_READ" -> ApiPaths.API_COMIC_READ
            
            // 会员相关
            "API_MEMBER_LOGIN" -> ApiPaths.API_MEMBER_LOGIN
            "API_MEMBER_LOGOUT" -> ApiPaths.API_MEMBER_LOGOUT
            "API_MEMBER_REGISTER" -> ApiPaths.API_MEMBER_REGISTER
            "API_MEMBER_FORGOT" -> ApiPaths.API_MEMBER_FORGOT
            
            // 分类相关
            "API_CATEGORIES_LIST" -> ApiPaths.API_CATEGORIES_LIST
            "API_CATEGORIES_FILTER_LIST" -> ApiPaths.API_CATEGORIES_FILTER_LIST
            
            // 收藏相关
            "API_FAVORITE_LIST" -> ApiPaths.API_FAVORITE_LIST
            "API_LIKE_DATA" -> ApiPaths.API_LIKE_DATA
            
            // 历史记录
            "API_HISTORY_LIST" -> ApiPaths.API_HISTORY_LIST
            
            // 其他API...
            "API_APP_SETTING" -> ApiPaths.API_APP_SETTING
            "API_WEEK" -> ApiPaths.API_WEEK
            "API_DAILY" -> ApiPaths.API_DAILY
            "API_GAMES_LIST" -> ApiPaths.API_GAMES_LIST
            "API_VIDEOS_LIST" -> ApiPaths.API_VIDEOS_LIST
            "API_BLOGS_LIST" -> ApiPaths.API_BLOGS_LIST
            
            else -> throw IllegalArgumentException("未知的API key: $key")
        }
    }
    
    /**
     * 🎯 从主机数据设置全局主机
     * 📋 对应React Native的setGlobalHostFromData
     */
    fun setGlobalHostFromData(data: HostData): String {
        // 使用现有的FunctionUtils随机选择服务器
        val randomResult = FunctionUtils.getRandomItems(data.servers, 1)
        val selectedServer = randomResult.items.firstOrNull() ?: data.servers.first()
        
        val apiUrl = "https://$selectedServer/"
        setBaseUrl(apiUrl)
        
        // 更新全局配置
        GlobalConfigManager.updateApiUrl(apiUrl)
        GlobalConfigManager.updateHostServer(data.jm3Server)
        
        AppLogger.d("Global host set to: $apiUrl")
        return apiUrl
    }
    
    /**
     * 🎯 获取主机数据
     * 📋 对应React Native的FETCH_HOST函数
     */
    suspend fun fetchHostData(): HostData {
        val primaryUrl = BuildConfig.HOST_PRIMARY
        val backupUrl = BuildConfig.HOST_BACKUP
        val secondaryBackup = BuildConfig.HOST_BACKUP_SECONDARY
        val hostCode = BuildConfig.HOST_BACKUP_CODE
        
        val urls = listOfNotNull(primaryUrl, backupUrl, secondaryBackup)
        
        // 尝试所有URL
        for (url in urls) {
            try {
                val data = tryFetchAndDecrypt(url)
                if (data != null) {
                    // 保存到本地存储，表示使用网络获取
                    saveFetchMethod("network")
                    AppLogger.d("Host data fetched from network: $url")
                    return data
                }
            } catch (e: Exception) {
                AppLogger.w("Request $url failed: ${e.message}")
            }
        }
        
        // 所有URL都失败，使用hostCode
        AppLogger.w("All URL requests failed, using backup hostCode...")
        try {
            val backupData = decryptHostCode(hostCode)
            saveFetchMethod("backup")
            AppLogger.d("Host data fetched from backup code")
            return backupData
        } catch (e: Exception) {
            AppLogger.e("Backup hostCode decryption failed", e)
            throw IllegalStateException("无法取得任何有效主机信息")
        }
    }
    
    /**
     * 🎯 尝试获取并解密主机数据
     */
    private suspend fun tryFetchAndDecrypt(url: String): HostData? {
        return withContext(Dispatchers.IO) {
            try {
                val response = HttpUtil.simpleGet(url)
                if (response.isSuccessful) {
                    val encryptedData = response.body?.string() ?: return@withContext null
                    decryptHostData(encryptedData)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * 🎯 解密主机数据
     */
    private fun decryptHostData(encryptedData: String): HostData {
        // 使用现有的EncryptionUtil进行解密
        val decryptedJson = EncryptionUtil.tryMultipleKeysDecrypt(encryptedData, "host")
        return Gson().fromJson(decryptedJson, HostData::class.java)
    }
    
    /**
     * 🎯 解密备用主机代码
     */
    private fun decryptHostCode(hostCode: String?): HostData {
        if (hostCode.isNullOrEmpty()) {
            throw IllegalArgumentException("Host code is null or empty")
        }
        
        val decryptedJson = EncryptionUtil.tryMultipleKeysDecrypt(hostCode, "host_backup")
        return Gson().fromJson(decryptedJson, HostData::class.java)
    }
    
    /**
     * 🎯 保存获取方式到本地存储
     */
    private fun saveFetchMethod(method: String) {
        val prefs = App.instance.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        prefs.edit().putString("fetch_method", method).apply()
    }
}

/**
 * 🎯 主机数据模型
 * 📋 对应React Native中的主机数据结构
 */
data class HostData(
    @SerializedName("Server") val servers: List<String>,
    @SerializedName("jm3_Server") val jm3Server: String
)
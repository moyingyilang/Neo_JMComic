// HostManager.kt
object HostManager {
    
    suspend fun fetchHost(): HostData {
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
                    setGlobalHostFromData(data)
                    AnalyticsManager.logEvent("host_fetch_success", mapOf("source" to "url"))
                    return data
                }
            } catch (e: Exception) {
                Log.w("HostManager", "Request $url failed: ${e.message}")
            }
        }
        
        // 所有URL都失败，使用hostCode
        Log.w("HostManager", "All URL requests failed, using backup hostCode...")
        try {
            val backupData = decryptData(hostCode)
            setGlobalHostFromData(backupData)
            AnalyticsManager.logEvent("host_fetch_success", mapOf("source" to "backup_code"))
            return backupData
        } catch (e: Exception) {
            Log.e("HostManager", "Backup hostCode decryption failed", e)
            throw IllegalStateException("无法取得任何有效主机信息")
        }
    }
    
    private suspend fun tryFetchAndDecrypt(url: String): HostData? {
        return withContext(Dispatchers.IO) {
            try {
                val response = HttpClient.get(url)
                if (response.isSuccessful) {
                    val encryptedData = response.body
                    decryptData(encryptedData)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun setGlobalHostFromData(data: HostData): String {
        val randomServer = data.servers.random()
        val apiUrl = "https://$randomServer/"
        GlobalStore.updateApiUrl(apiUrl)
        GlobalStore.updateHostServer(data.jm3Server)
        return apiUrl
    }
    
    private fun decryptData(encrypted: String): HostData {
        // 实现解密逻辑 - 需要您提供具体的解密算法
        val decryptedJson = EncryptionUtil.decrypt(encrypted)
        return Gson().fromJson(decryptedJson, HostData::class.java)
    }
}

data class HostData(
    val servers: List<String>,
    @SerializedName("jm3_Server") val jm3Server: String
)
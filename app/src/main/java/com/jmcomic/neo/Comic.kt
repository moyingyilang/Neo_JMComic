// Comic.kt
data class Comic(
    val id: String,
    val title: String,
    val cover: String,
    val author: String,
    val tags: List<String>,
    val lastUpdate: String,
    val isNew: Boolean = false,
    val isHot: Boolean = false
)

// GlobalConfig.kt
data class GlobalConfig(
    var host: String = "",
    var hostReady: Boolean = false,
    var version: String = "",
    var isLoggedIn: Boolean = false
) {
    fun updateHost(apiUrl: ApiResponse) {
        // 随机选择服务器逻辑
        val servers = apiUrl.servers
        val randomServer = servers.random()
        host = "https://$randomServer/"
        hostReady = true
    }
}
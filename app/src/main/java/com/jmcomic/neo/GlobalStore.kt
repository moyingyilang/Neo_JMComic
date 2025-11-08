// GlobalStore.kt
object GlobalStore {
    private const val PREFS_NAME = "global_store"
    private const val KEY_API_URL = "api_url"
    private const val KEY_HOST_SERVER = "host_server"
    
    var apiUrl: String
        get() = getPreference(KEY_API_URL, "")
        set(value) = setPreference(KEY_API_URL, value)
    
    var hostServer: String
        get() = getPreference(KEY_HOST_SERVER, "")
        set(value) = setPreference(KEY_HOST_SERVER, value)
    
    fun updateApiUrl(newApiUrl: String) {
        apiUrl = newApiUrl
        ApiEndpoints.setBaseUrl(newApiUrl)
    }
    
    fun updateHostServer(newHostServer: String) {
        hostServer = newHostServer
    }
    
    private fun getPreference(key: String, defaultValue: String): String {
        val prefs = App.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
    
    private fun setPreference(key: String, value: String) {
        val prefs = App.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }
}
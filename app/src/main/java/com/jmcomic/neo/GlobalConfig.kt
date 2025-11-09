// GlobalConfig.kt - 对应GlobalConfig.js和GlobalStore.ts
object GlobalConfigManager {
    
    private const val PREFS_GLOBAL = "global_config"
    private const val KEY_API_URL = "api_url"
    private const val KEY_HOST_SERVER = "host_server"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_LANGUAGE = "language"
    
    private val _config = MutableStateFlow(GlobalConfig())
    val config: StateFlow<GlobalConfig> = _config.asStateFlow()
    
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_GLOBAL, Context.MODE_PRIVATE)
        
        val apiUrl = prefs.getString(KEY_API_URL, "") ?: ""
        val hostServerJson = prefs.getString(KEY_HOST_SERVER, "[]") ?: "[]"
        val hostServer = try {
            Gson().fromJson(hostServerJson, Array<String>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
        
        _config.value = GlobalConfig(
            apiUrl = apiUrl,
            hostServer = hostServer,
            isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false),
            darkMode = prefs.getBoolean(KEY_DARK_MODE, false),
            language = prefs.getString(KEY_LANGUAGE, "zh-TW") ?: "zh-TW"
        )
    }
    
    fun updateApiUrl(newApiUrl: String) {
        _config.value = _config.value.copy(apiUrl = newApiUrl)
        saveToPrefs(KEY_API_URL, newApiUrl)
    }
    
    fun updateHostServer(newHostServer: List<String>) {
        _config.value = _config.value.copy(hostServer = newHostServer)
        val json = Gson().toJson(newHostServer)
        saveToPrefs(KEY_HOST_SERVER, json)
    }
    
    fun updateLoginStatus(isLoggedIn: Boolean) {
        _config.value = _config.value.copy(isLoggedIn = isLoggedIn)
        saveToPrefs(KEY_IS_LOGGED_IN, isLoggedIn)
    }
    
    fun updateTheme(darkMode: Boolean) {
        _config.value = _config.value.copy(darkMode = darkMode)
        saveToPrefs(KEY_DARK_MODE, darkMode)
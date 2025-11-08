// AuthManager.kt
object AuthManager {
    
    private const val PREFS_AUTH = "auth_prefs"
    private const val KEY_JWT_TOKEN = "jwttoken"
    private const val KEY_MEMBER_INFO = "memberInfo"
    private const val KEY_MEMBER_ACCOUNT = "memberAccount"
    private const val KEY_AUTH_EXPIRY = "authExpiry"
    private const val KEY_DONT_SHOW_EXPIRY = "dontShowExpiry"
    
    // 保存认证数据
    fun saveAuthData(token: String, memberData: MemberInfo) {
        val prefs = getAuthPreferences()
        val expiryTime = System.currentTimeMillis() + 60 * 60 * 1000 // 1小时过期
        
        prefs.edit().apply {
            putString(KEY_JWT_TOKEN, token)
            putString(KEY_MEMBER_INFO, Gson().toJson(memberData))
            putLong(KEY_AUTH_EXPIRY, expiryTime)
        }.apply()
    }
    
    // 检查认证过期
    fun checkAuthExpiry(): Boolean {
        val prefs = getAuthPreferences()
        val expiry = prefs.getLong(KEY_AUTH_EXPIRY, 0)
        return System.currentTimeMillis() > expiry
    }
    
    // 清除认证数据
    fun clearAuth(clearAll: Boolean = false) {
        val prefs = getAuthPreferences()
        prefs.edit().apply {
            remove(KEY_JWT_TOKEN)
            remove(KEY_AUTH_EXPIRY)
            if (clearAll) {
                remove(KEY_MEMBER_INFO)
                remove(KEY_MEMBER_ACCOUNT)
            }
        }.apply()
    }
    
    // 获取当前token
    fun getToken(): String? {
        val prefs = getAuthPreferences()
        return prefs.getString(KEY_JWT_TOKEN, null)
    }
    
    // 获取会员信息
    fun getMemberInfo(): MemberInfo? {
        val prefs = getAuthPreferences()
        val json = prefs.getString(KEY_MEMBER_INFO, null)
        return if (json != null) {
            Gson().fromJson(json, MemberInfo::class.java)
        } else {
            null
        }
    }
    
    // 三天免打扰检查
    fun checkThreeDaysExpiry() {
        val prefs = getAuthPreferences()
        val expiry = prefs.getLong(KEY_DONT_SHOW_EXPIRY, 0)
        if (expiry > 0 && System.currentTimeMillis() > expiry) {
            prefs.edit().remove(KEY_DONT_SHOW_EXPIRY).apply()
            // 可以触发重新登录或其他操作
        }
    }
    
    private fun getAuthPreferences(): SharedPreferences {
        return App.context.getSharedPreferences(PREFS_AUTH, Context.MODE_PRIVATE)
    }
}

data class MemberInfo(
    val id: String,
    val username: String,
    val email: String,
    val gender: String,
    val adult: Boolean,
    val avatar: String? = null
)
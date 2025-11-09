// auth/AuthChecker.kt
@Composable
fun AuthChecker(
    onAuthStateChange: (AuthState) -> Unit
) {
    val authState by AuthManager.authState.collectAsState()
    
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn -> {
                onAuthStateChange(authState)
                // 加载用户数据
                loadUserData((authState as AuthState.LoggedIn).userInfo)
            }
            is AuthState.LoggedOut -> {
                onAuthStateChange(authState)
                // 清理用户相关数据
                clearUserData()
            }
            is AuthState.Expired -> {
                // 显示重新登录对话框
                showReLoginDialog()
            }
            else -> {}
        }
    }
}
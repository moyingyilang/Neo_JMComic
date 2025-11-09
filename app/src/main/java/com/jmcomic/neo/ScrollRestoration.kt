// utils/ScrollRestoration.kt
@Composable
fun rememberScrollRestoration(key: String): ScrollState {
    val scrollState = rememberScrollState()
    
    LaunchedEffect(Unit) {
        // 从保存的状态恢复滚动位置
        val savedPosition = ScrollStateManager.getSavedPosition(key)
        if (savedPosition > 0) {
            scrollState.scrollTo(savedPosition)
        }
    }
    
    DisposableEffect(key) {
        onDispose {
            // 保存当前滚动位置
            ScrollStateManager.savePosition(key, scrollState.value)
        }
    }
    
    return scrollState
}

object ScrollStateManager {
    private val scrollPositions = mutableMapOf<String, Int>()
    
    fun savePosition(key: String, position: Int) {
        scrollPositions[key] = position
    }
    
    fun getSavedPosition(key: String): Int {
        return scrollPositions[key] ?: 0
    }
}
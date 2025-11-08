// CommonUtil.kt
object CommonUtil {
    
    // 格式化浮点数显示
    fun formatFloat(src: Int, pos: Int = 1): String {
        var value = src.toDouble()
        var unitStr = ""
        
        if (src < 1000) {
            return src.toString()
        }
        
        value /= 1000
        unitStr = "K"
        
        if (value > 100) {
            value /= 100
            unitStr = "M"
        }
        
        val factor = Math.pow(10.0, pos.toDouble())
        val rounded = Math.round(value * factor) / factor
        return "$rounded$unitStr"
    }
    
    // 修改数据结构（用于网格布局和广告插入）
    fun <T> modifyData(
        data: List<T>?,
        column: Int = 3,
        bannerRow: Int = 6,
        banners: List<Any> = emptyList()
    ): List<Any> {
        val numColumns = column
        val addBannerIndex = bannerRow
        val section = mutableListOf<Any>()
        var items = mutableListOf<Any>()
        
        data?.forEachIndexed { index, value ->
            if (index % numColumns == 0 && index != 0) {
                section.add(items.toList())
                items = mutableListOf()
            }
            
            if (banners.isNotEmpty() && index % addBannerIndex == 0 && index != 0) {
                section.add(BannerItem(banners))
                items = mutableListOf()
            }
            
            items.add(value)
        }
        
        if (items.isNotEmpty()) {
            section.add(items)
        }
        
        return section
    }
    
    // 重定向到屏幕（Android 版本）
    fun redirectToScreen(originUrl: String, context: android.content.Context) {
        val urlParts = originUrl.split("/")
        if (urlParts.size >= 3) {
            val screen = urlParts[2]
            val params = mutableMapOf<String, String>()
            
            // 解析参数
            for (i in 3 until urlParts.size step 2) {
                if (i + 1 < urlParts.size) {
                    params[urlParts[i]] = urlParts[i + 1]
                }
            }
            
            // 根据screen进行导航
            navigateToScreen(screen, params, context)
        }
    }
    
    private fun navigateToScreen(screen: String, params: Map<String, String>, context: android.content.Context) {
        // 实现具体的导航逻辑
        when (screen) {
            "comic" -> {
                val comicId = params["id"]
                if (comicId != null) {
                    // 跳转到漫画详情页
                    // ComicDetailActivity.start(context, comicId)
                }
            }
            "search" -> {
                val query = params["query"]
                // 跳转到搜索页
                // SearchActivity.start(context, query)
            }
            // 其他屏幕...
        }
    }
}

// 广告项数据类
data class BannerItem(val banners: List<Any>)
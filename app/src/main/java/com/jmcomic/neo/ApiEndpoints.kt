// ApiEndpoints.kt
object ApiEndpoints {
    const val API_COMIC_PROMOTE = "/comic/promote"
    const val API_COMIC_LATEST = "/comic/latest" 
    const val API_COMIC_PROMOTE_LIST = "/comic/promote/list"
    const val API_COMIC_SER_MORE_LIST = "/comic/ser/more"
    const val API_COIN_BUY_COMICS = "/coin/buy/comics"
    const val API_ADVERTISE_CONTENT_COVER = "/advertise/content/cover"
    const val API_ADVERTISE_ALL = "/advertise/all"
    
    private var baseUrl = ""
    
    fun setBaseUrl(url: String) {
        baseUrl = url
    }
    
    fun getEndpoint(key: String): String {
        if (baseUrl.isEmpty()) {
            throw IllegalStateException("API endpoints 尚未初始化")
        }
        return "$baseUrl${getPath(key)}"
    }
    
    private fun getPath(key: String): String {
        return when (key) {
            "API_COMIC_PROMOTE" -> API_COMIC_PROMOTE
            "API_COMIC_LATEST" -> API_COMIC_LATEST
            "API_COMIC_PROMOTE_LIST" -> API_COMIC_PROMOTE_LIST
            "API_COMIC_SER_MORE_LIST" -> API_COMIC_SER_MORE_LIST
            "API_COIN_BUY_COMICS" -> API_COIN_BUY_COMICS
            "API_ADVERTISE_CONTENT_COVER" -> API_ADVERTISE_CONTENT_COVER
            "API_ADVERTISE_ALL" -> API_ADVERTISE_ALL
            else -> throw IllegalArgumentException("Unknown API key: $key")
        }
    }
}
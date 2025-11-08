// network/ApiService.kt
interface ApiService {
    
    // 首页相关
    @GET(ApiPaths.API_COMIC_PROMOTE)
    suspend fun getComicPromote(): ApiResponse<List<ComicPromote>>
    
    @GET(ApiPaths.API_COMIC_LATEST)
    suspend fun getComicLatest(@Query("page") page: Int): ApiResponse<List<ComicLatest>>
    
    @GET(ApiPaths.API_COMIC_PROMOTE_LIST)
    suspend fun getComicPromoteList(
        @Query("id") id: String,
        @Query("page") page: Int
    ): ApiResponse<ComicMoreResponse>
    
    // 广告相关
    @GET(ApiPaths.API_ADVERTISE_CONTENT_COVER)
    suspend fun getCoverAds(
        @Query("lang") lang: String,
        @Query("ipcountry") ipCountry: String
    ): ApiResponse<List<AdContent>>
    
    @GET(ApiPaths.API_ADVERTISE_ALL)
    suspend fun getAllAds(
        @Query("lang") lang: String,
        @Query("ipcountry") ipCountry: String,
        @Query("v") version: String
    ): ApiResponse<Map<String, Any>>
    
    // 设置相关
    @GET(ApiPaths.API_APP_SETTING)
    suspend fun getSettings(
        @Query("app_img_shunt") imgShunt: String,
        @Query("express") express: String
    ): ApiResponse<SettingData>
    
    // 会员相关
    @POST(ApiPaths.API_MEMBER_LOGIN)
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>
    
    @GET(ApiPaths.API_MEMBER_LOGOUT)
    suspend fun logout(): ApiResponse<Any>
    
    // 漫画详情
    @GET(ApiPaths.API_COMIC_DETAIL)
    suspend fun getComicDetail(@Query("id") id: String): ApiResponse<ComicDetail>
    
    @GET(ApiPaths.API_COMIC_CHAPTER)
    suspend fun getComicChapters(@Query("id") id: String): ApiResponse<List<Chapter>>
    
    // 搜索
    @GET(ApiPaths.API_COMIC_SEARCH)
    suspend fun searchComics(@Query("query") query: String): ApiResponse<SearchResponse>
    
    @GET(ApiPaths.API_COMIC_HOT_TAGS)
    suspend fun getHotTags(): ApiResponse<List<String>>
    
    // 更多API接口...
}

// 数据模型
data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val memberInfo: MemberInfo
)

data class SearchResponse(
    val searchQuery: String,
    val total: String,
    val content: List<ComicContent>
)
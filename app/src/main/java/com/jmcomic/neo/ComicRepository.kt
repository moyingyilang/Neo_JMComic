// ComicRepository.kt
class ComicRepository {
    
    private val apiService = RetrofitClient.createService(ComicApiService::class.java)
    
    suspend fun fetchMainList(): Result<List<ComicPromote>> {
        return try {
            val response = apiService.getComicPromote()
            if (response.isSuccess) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "数据获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchLatestList(page: Int): Result<List<ComicLatest>> {
        return try {
            val response = apiService.getComicLatest(page)
            if (response.isSuccess) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "数据获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchMoreList(id: String, page: Int): Result<ComicMore> {
        return try {
            val response = apiService.getComicPromoteList(id, page)
            if (response.isSuccess) {
                Result.success(response.data ?: throw Exception("数据为空"))
            } else {
                Result.failure(Exception(response.message ?: "数据获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchCoverAds(lang: String = "TW", ipCountry: String = "TW"): Result<List<AdContent>> {
        return try {
            val response = apiService.getCoverAds(lang, ipCountry)
            if (response.isSuccess) {
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "广告获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchAllAds(adKey: String, lang: String = "TW", ipCountry: String = "TW", version: String = ""): Result<AdsData> {
        return try {
            val response = apiService.getAllAds(lang, ipCountry, version)
            if (response.isSuccess) {
                val adsData = response.data ?: throw Exception("广告数据为空")
                // 根据adKey提取特定广告位数据
                val extractedAds = extractAdsByKey(adsData, adKey)
                Result.success(extractedAds)
            } else {
                Result.failure(Exception(response.message ?: "广告获取失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractAdsByKey(adsData: Map<String, Any>, adKey: String): AdsData {
        // 实现广告数据提取逻辑
        return Gson().fromJson(Gson().toJson(adsData[adKey]), AdsData::class.java)
    }
}

// API接口定义
interface ComicApiService {
    
    @GET(ApiEndpoints.API_COMIC_PROMOTE)
    suspend fun getComicPromote(): ApiResponse<List<ComicPromote>>
    
    @GET(ApiEndpoints.API_COMIC_LATEST)
    suspend fun getComicLatest(@Query("page") page: Int): ApiResponse<List<ComicLatest>>
    
    @GET(ApiEndpoints.API_COMIC_PROMOTE_LIST)
    suspend fun getComicPromoteList(
        @Query("id") id: String,
        @Query("page") page: Int
    ): ApiResponse<ComicMore>
    
    @GET(ApiEndpoints.API_ADVERTISE_CONTENT_COVER)
    suspend fun getCoverAds(
        @Query("lang") lang: String,
        @Query("ipcountry") ipCountry: String
    ): ApiResponse<List<AdContent>>
    
    @GET(ApiEndpoints.API_ADVERTISE_ALL)
    suspend fun getAllAds(
        @Query("lang") lang: String,
        @Query("ipcountry") ipCountry: String,
        @Query("v") version: String
    ): ApiResponse<Map<String, Any>>
}
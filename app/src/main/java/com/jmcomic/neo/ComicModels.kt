// ComicModels.kt
data class ComicPromote(
    val id: String,
    val title: String,
    val slug: String,
    val type: String,
    val filterVal: String,
    val content: List<ComicContent>
)

data class ComicContent(
    val id: String,
    val author: String,
    val category: ComicCategory?,
    val categorySub: ComicCategory?,
    val image: String,
    val isFavorite: Boolean,
    val liked: Boolean,
    val name: String,
    val updateAt: Long
)

data class ComicCategory(
    val id: String?,
    val title: String?
)

data class ComicLatest(
    val id: String,
    val author: String,
    val description: String?,
    val name: String,
    val image: String,
    val category: ComicCategory,
    val categorySub: ComicCategory?,
    val liked: Boolean,
    val isFavorite: Boolean,
    val updateAt: Long
)

data class SettingData(
    val logoPath: String,
    val mainWebHost: String,
    val imgHost: String,
    val baseUrl: String,
    val isCn: Int,
    val cnBaseUrl: String,
    val version: String,
    val testVersion: String,
    val storeLink: String,
    val iosVersion: String,
    val iosTestVersion: String,
    val iosStoreLink: String,
    val adCacheVersion: Int,
    val bundleUrl: String,
    val isHotUpdate: Boolean,
    val apiBannerPath: String,
    val versionInfo: String,
    val appShunts: List<AppShunt>,
    val downloadUrl: String,
    val appLandingPage: String,
    val floatAd: Boolean,
    val newYearEvent: Boolean,
    val foolsDayEvent: Boolean,
    val dateYmdHis: String
)

data class AppShunt(
    val key: Int,
    val title: String
)

data class ApiResponse<T>(
    val code: Int,
    val data: T? = null,
    val dateYmdHis: String? = null
) {
    val isSuccess: Boolean get() = code == 200
}
// Banner.kt
data class Banner(
    val id: String,
    val imageUrl: String,
    val title: String,
    val targetUrl: String? = null,
    val targetType: BannerTargetType = BannerTargetType.EXTERNAL
)

enum class BannerTargetType {
    EXTERNAL, COMIC, CATEGORY, GAME, MOVIE
}
data class NavItem(
    val iconRes: Int,
    val labelRes: Int,
    val target: NavigationTarget,
    val params: Map<String, String> = emptyMap()
)

enum class NavigationTarget {
    CATEGORIES, CATEGORIES_HOT, CATEGORIES_HANMAN, CATEGORIES_SINGLE, 
    GAMES, MOVIES, LIBRARY, WEEK
}
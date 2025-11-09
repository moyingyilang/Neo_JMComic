// NavItem.kt - 导航项数据类
data class NavItem(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val target: NavigationTarget
)
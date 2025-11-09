// navigation/NavGraph.kt
@Composable
fun ComicAppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "main") {
        // 主页面
        composable("main") { MainScreen(navController) }
        // 博客相关
        composable("blogs") { BlogsScreen(navController) }
        composable("blogs/detail") { BlogsDetailScreen(navController) }
        // 漫画相关
        composable("comic/detail/{comicId}") { backStackEntry ->
            val comicId = backStackEntry.arguments?.getString("comicId") ?: ""
            ComicDetailScreen(navController, comicId)
        }
        composable("comic/read/{comicId}/{chapterId}") { backStackEntry ->
            val comicId = backStackEntry.arguments?.getString("comicId") ?: ""
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
            ReadScreen(navController, comicId, chapterId)
        }
        // 搜索
        composable("search") { SearchScreen(navController) }
        // 分类
        composable("categories") { CategoriesScreen(navController) }
        // 会员
        composable("member") { MemberScreen(navController) }
        // 错误页面
        composable("error") { ErrorScreen(navController) }
    }
}
// analytics/PageTracker.kt
@Composable
fun PageTracker(navController: NavController) {
    val currentDestination by navController.currentBackStackEntryAsState()
    
    LaunchedEffect(currentDestination) {
        currentDestination?.destination?.route?.let { route ->
            AnalyticsManager.trackPageView(route)
        }
    }
}
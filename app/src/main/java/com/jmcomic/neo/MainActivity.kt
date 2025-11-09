// MainFragment.kt - 修复版本
class MainFragment : Fragment() {
    
    private lateinit var binding: FragmentMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    // 使用现有的BannerAdapter和ComicListAdapter
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var comicListAdapter: ComicListAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAdapters()
        setupObservers()
        setupRefreshLayout()
        loadInitialData()
    }
    
    private fun setupAdapters() {
        // 使用现有的BannerAdapter
        bannerAdapter = BannerAdapter(
            onBannerClick = { banner ->
                handleBannerClick(banner)
            },
            onNavItemClick = { navItem ->
                handleNavItemClick(navItem)
            }
        )
        
        // 使用现有的ComicListAdapter
        comicListAdapter = ComicListAdapter(
            onComicClick = { comic ->
                navigateToComicDetail(comic.id)
            },
            onLikeClick = { comic ->
                viewModel.toggleLike(comic.id)
            },
            onBookmarkClick = { comic ->
                viewModel.toggleBookmark(comic.id)
            }
        )
        
        // 设置RecyclerView
        binding.comicRecyclerView.apply {
            adapter = comicListAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }
    }
    
    private fun setupObservers() {
        // 观察banner数据 - 使用现有的Resource模式
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bannerState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // 可以显示banner加载状态
                    }
                    is Resource.Success -> {
                        bannerAdapter.submitList(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        // 静默处理banner加载错误
                        Log.e("MainFragment", "Banner load failed: ${resource.message}")
                    }
                    else -> {}
                }
            }
        }
        
        // 观察漫画列表数据
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.latestListState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (comicListAdapter.itemCount == 0) {
                            showLoading()
                        }
                    }
                    is Resource.Success -> {
                        hideLoading()
                        comicListAdapter.submitList(resource.data ?: emptyList())
                        binding.emptyState.isVisible = resource.data.isNullOrEmpty()
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError(resource.message ?: "加载失败")
                        binding.emptyState.isVisible = true
                    }
                    else -> {}
                }
            }
        }
        
        // 观察加载更多状态
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadMoreState.collect { isLoading ->
                binding.refreshLayout.finishLoadMore(!isLoading)
            }
        }
    }
    
    private fun setupRefreshLayout() {
        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener {
            viewModel.refreshAllData()
            binding.refreshLayout.finishRefresh()
        }
        
        // 上拉加载更多
        binding.refreshLayout.setOnLoadMoreListener {
            viewModel.loadMoreLatestList()
        }
        
        // 设置自动加载更多
        binding.comicRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                layoutManager?.let {
                    val visibleItemCount = it.childCount
                    val totalItemCount = it.itemCount
                    val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                    
                    if (!viewModel.loadMoreState.value && 
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                        viewModel.loadMoreLatestList()
                    }
                }
            }
        })
    }
    
    private fun loadInitialData() {
        viewModel.loadInitialData()
    }
    
    private fun handleBannerClick(banner: Banner) {
        when (banner.targetType) {
            BannerTargetType.COMIC -> navigateToComicDetail(banner.id)
            BannerTargetType.EXTERNAL -> openExternalUrl(banner.targetUrl)
            else -> {
                // 使用现有的CommonUtil处理重定向
                banner.targetUrl?.let { url ->
                    CommonUtil.redirectToScreen(url, requireContext())
                }
            }
        }
    }
    
    private fun handleNavItemClick(navItem: NavItem) {
        when (navItem.target) {
            NavigationTarget.CATEGORIES -> navigateToCategories()
            NavigationTarget.LIBRARY -> navigateToLibrary()
            NavigationTarget.GAMES -> navigateToGames()
            NavigationTarget.MOVIES -> navigateToMovies()
            NavigationTarget.WEEK -> navigateToWeek()
            else -> {
                Toast.makeText(requireContext(), "功能开发中", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToComicDetail(comicId: String) {
        // 使用您已完成的ReadActivity
        val intent = Intent(requireContext(), ReadActivity::class.java).apply {
            putExtra("comicId", comicId)
        }
        startActivity(intent)
    }
    
    private fun openExternalUrl(url: String?) {
        url?.let {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "无法打开链接", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToCategories() {
        findNavController().navigate(R.id.action_mainFragment_to_categoriesFragment)
    }
    
    private fun navigateToLibrary() {
        findNavController().navigate(R.id.action_mainFragment_to_libraryFragment)
    }
    
    private fun navigateToGames() {
        findNavController().navigate(R.id.action_mainFragment_to_gamesFragment)
    }
    
    private fun navigateToMovies() {
        findNavController().navigate(R.id.action_mainFragment_to_moviesFragment)
    }
    
    private fun navigateToWeek() {
        findNavController().navigate(R.id.action_mainFragment_to_weekFragment)
    }
    
    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.emptyState.isVisible = false
    }
    
    private fun hideLoading() {
        binding.progressBar.isVisible = false
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
/**
 * 🎯 主Activity - 完善版本
 * 📋 对应React Native的路由配置
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    // 返回按钮处理器
    private lateinit var backButtonHandler: SecurityManager.BackButtonHandler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
        setupBottomNavigation()
        setupObservers()
        initializeSecurity()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // 配置AppBar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainFragment,
                R.id.libraryFragment, 
                R.id.searchFragment,
                R.id.memberFragment
            )
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // 页面追踪
        setupPageTracking()
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNav.setupWithNavController(navController)
        
        // 监听导航变化
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavigationVisibility(destination.id)
            updateActionBarTitle(destination.id)
        }
    }
    
    private fun setupObservers() {
        // 监听全局配置变化
        GlobalConfigManager.config.observe(this) { config ->
            updateTheme(config.darkMode)
            updateLanguage(config.language)
        }
        
        // 监听认证状态变化
        AuthManager.authState.observe(this) { authState ->
            handleAuthStateChange(authState)
        }
    }
    
    private fun initializeSecurity() {
        backButtonHandler = SecurityManager.BackButtonHandler(this)
        
        // 开发者工具检测
        SecurityManager.enableDevToolsBlocker(this)
    }
    
    private fun setupPageTracking() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 页面访问统计
            AnalyticsManager.trackPageView(destination.route ?: destination.label.toString())
        }
    }
    
    private fun updateBottomNavigationVisibility(destinationId: Int) {
        // 在某些页面隐藏底部导航
        val shouldShowBottomNav = when (destinationId) {
            R.id.mainFragment, R.id.libraryFragment, R.id.searchFragment, R.id.memberFragment -> true
            else -> false
        }
        
        binding.bottomNav.isVisible = shouldShowBottomNav
    }
    
    private fun updateActionBarTitle(destinationId: Int) {
        val title = when (destinationId) {
            R.id.mainFragment -> "首页"
            R.id.searchFragment -> "搜索"
            R.id.libraryFragment -> "书库"
            R.id.memberFragment -> "我的"
            R.id.categoriesFragment -> "分类"
            R.id.blogsFragment -> "博客"
            R.id.weekFragment -> "每周必看"
            R.id.gamesFragment -> "游戏"
            R.id.moviesFragment -> "视频"
            R.id.dailyFragment -> "每日签到"
            R.id.forumFragment -> "论坛"
            else -> "漫画APP"
        }
        
        supportActionBar?.title = title
    }
    
    private fun handleAuthStateChange(authState: AuthState) {
        when (authState) {
            is AuthState.LoggedIn -> {
                // 用户已登录，加载用户数据
                loadUserData(authState.userInfo)
            }
            is AuthState.LoggedOut -> {
                // 用户已登出，清理用户数据
                clearUserData()
            }
            is AuthState.Expired -> {
                // 认证过期，显示重新登录对话框
                showReLoginDialog()
            }
            else -> {}
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    
    override fun onBackPressed() {
        if (!backButtonHandler.handleBackPress()) {
            super.onBackPressed()
        }
    }
    
    private fun updateTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun updateLanguage(language: String) {
        val locale = when (language) {
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            else -> Locale.getDefault()
        }
        
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    
    private fun loadUserData(userInfo: MemberInfo) {
        // 加载用户相关数据
        AppLogger.d("Loading user data for: ${userInfo.username}")
    }
    
    private fun clearUserData() {
        // 清理用户数据
        AppLogger.d("Clearing user data")
    }
    
    private fun showReLoginDialog() {
        // 显示重新登录对话框
        val dialog = ReLoginDialogFragment()
        dialog.show(supportFragmentManager, "re_login_dialog")
    }
}

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // 必须在super.onCreate之前应用主题
        applyDynamicTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupThemeAwareComponents()
        initializeThemeListener()
        setupBottomNavigation()
    }
    
    private fun applyDynamicTheme() {
        val themeRes = if (ThemeManager.isDarkTheme()) {
            R.style.Theme_ComicApp_Dark
        } else {
            R.style.Theme_ComicApp
        }
        setTheme(themeRes)
    }
    
    private fun setupThemeAwareComponents() {
        // 应用主题色到状态栏
        window.statusBarColor = ThemeManager.resolveColor(this, R.attr.colorPrimary)
        
        // 设置导航栏颜色
        window.navigationBarColor = ThemeManager.resolveColor(this, R.attr.colorBackground)
        
        // 初始化主题相关的组件
        setupThemedToolbar()
        setupThemedBottomNav()
    }
    
    private fun setupThemedBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.apply {
            backgroundTintList = ColorStateList.valueOf(
                ThemeManager.resolveColor(context, R.attr.colorSurface)
            )
            itemIconTintList = createBottomNavColorStateList()
            itemTextColor = createBottomNavColorStateList()
        }
    }
    
    private fun createBottomNavColorStateList(): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ThemeManager.resolveColor(this, R.attr.colorPrimary),
                ThemeManager.resolveColor(this, R.attr.colorOnSurfaceVariant)
            )
        )
    }
    
    private fun initializeThemeListener() {
        ThemeManager.addThemeListener { newTheme ->
            // 重新创建Activity应用新主题
            recreate()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ThemeManager.removeThemeListener { }
    }
}
// 主题解析工具 - 对应React的主题解析功能
object ThemeResolver {
    
    fun resolveColor(context: Context, @AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }
    
    fun resolveDimension(context: Context, @AttrRes attrRes: Int): Float {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return TypedValue.complexToDimension(typedValue.data, context.resources.displayMetrics)
    }
    
    fun resolveBoolean(context: Context, @AttrRes attrRes: Int): Boolean {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data != 0
    }
}

// 主题切换动画
class ThemeTransitionHelper {
    companion object {
        fun applyThemeChangeTransition(activity: Activity) {
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            val transition = TransitionSet().apply {
                addTransition(Fade().apply { duration = 250 })
                addTransition(ChangeBounds().apply { duration = 350 })
                addTransition(ChangeImageTransform().apply { duration = 300 })
            }
            TransitionManager.beginDelayedTransition(rootView, transition)
        }
    }
}
class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupBottomNavigation()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // 设置底部导航与导航控制器的连接
        bottomNavigation.setupWithNavController(navController)
        
        // 应用主题到底部导航
        applyThemeToBottomNav()
    }
    
    private fun applyThemeToBottomNav() {
        // 设置主题颜色
        bottomNavigation.backgroundTintList = ColorStateList.valueOf(
            ThemeManager.resolveColor(this, R.attr.colorSurface)
        )
    }
}
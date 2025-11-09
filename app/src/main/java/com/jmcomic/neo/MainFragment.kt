// MainFragment.kt - 修复版本
class MainFragment : Fragment() {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: FragmentMainBinding
    
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
        
        setupViewModels()
        setupObservers()
        loadInitialData()
    }
    
    private fun setupViewModels() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }
    
    private fun setupObservers() {
        // 观察banner数据
        viewModel.bannerState.collectLatest { resource ->
            when (resource) {
                is Resource.Loading -> showLoading()
                is Resource.Success -> {
                    hideLoading()
                    setupBannerAdapter(resource.data ?: emptyList())
                }
                is Resource.Error -> {
                    hideLoading()
                    showError(resource.message ?: "加载失败")
                }
                is Resource.Idle -> {}
            }
        }
        
        // 观察最新漫画列表
        viewModel.latestListState.collectLatest { resource ->
            when (resource) {
                is Resource.Success -> {
                    setupComicListAdapter(resource.data ?: emptyList())
                }
                is Resource.Error -> {
                    showError(resource.message ?: "加载失败")
                }
                else -> {}
            }
        }
        
        // 观察加载更多状态
        viewModel.loadMoreState.collectLatest { isLoading ->
            binding.refreshLayout.finishLoadMore(!isLoading)
        }
    }
    
    private fun setupBannerAdapter(banners: List<Banner>) {
        val bannerAdapter = BannerAdapter(
            onBannerClick = { banner ->
                handleBannerClick(banner)
            },
            onNavItemClick = { navItem ->
                handleNavItemClick(navItem)
            }
        )
        
        binding.bannerRecyclerView.adapter = bannerAdapter
        bannerAdapter.submitList(banners)
    }
    
    private fun setupComicListAdapter(comics: List<Comic>) {
        val comicListAdapter = ComicListAdapter { comic ->
            navigateToComicDetail(comic.id)
        }
        
        binding.comicListRecyclerView.apply {
            adapter = comicListAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
        
        comicListAdapter.submitList(comics)
    }
    
    private fun handleBannerClick(banner: Banner) {
        when (banner.targetType) {
            BannerTargetType.COMIC -> navigateToComicDetail(banner.id)
            BannerTargetType.EXTERNAL -> openExternalUrl(banner.targetUrl)
            else -> {
                // 处理其他类型的点击
            }
        }
    }
    
    private fun handleNavItemClick(navItem: NavItem) {
        when (navItem.target) {
            NavigationTarget.CATEGORIES -> navigateToCategories()
            NavigationTarget.LIBRARY -> navigateToLibrary()
            NavigationTarget.GAMES -> navigateToGames()
            NavigationTarget.MOVIES -> navigateToMovies()
            else -> {
                // 处理其他导航目标
            }
        }
    }
    
    private fun loadInitialData() {
        viewModel.loadInitialData()
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
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }
    
    private fun navigateToCategories() {
        // TODO: 实现分类页面导航
        Toast.makeText(requireContext(), "跳转到分类", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToLibrary() {
        // TODO: 实现书库页面导航
        Toast.makeText(requireContext(), "跳转到书库", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToGames() {
        // TODO: 实现游戏页面导航
        Toast.makeText(requireContext(), "跳转到游戏", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToMovies() {
        // TODO: 实现视频页面导航
        Toast.makeText(requireContext(), "跳转到视频", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.refreshLayout.finishRefresh()
    }
    
    private fun showError(message: String) {
        hideLoading()
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    // 扩展函数用于收集StateFlow
    private fun <T> StateFlow<T>.collectLatest(collector: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            collectLatest { collector(it) }
        }
    }
}
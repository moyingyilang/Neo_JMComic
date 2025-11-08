class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>() {
    
    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var comicCarouselAdapter: ComicCarouselAdapter
    private lateinit var comicListAdapter: ComicListAdapter
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater, container, false)
    }
    
    override fun getViewModel(): MainViewModel {
        return ViewModelProvider(this)[MainViewModel::class.java]
    }
    
    override fun setupUI() {
        setupBanner()
        setupComicCarousel()
        setupComicList()
        setupSwipeRefresh()
        setupHeader()
    }
    
    override fun setupObservers() {
        // 观察轮播图数据
        viewModel.bannerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> bannerAdapter.submitList(state.data)
                is Resource.Error -> showSnackbar(state.message)
                is Resource.Loading -> {/* 显示加载状态 */}
                else -> {}
            }
        }
        
        // 观察漫画推荐数据
        viewModel.mainListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> comicCarouselAdapter.submitList(state.data)
                is Resource.Error -> showSnackbar(state.message)
                is Resource.Loading -> {/* 显示加载状态 */}
                else -> {}
            }
        }
        
        // 观察最新漫画数据
        viewModel.latestListState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> comicListAdapter.submitList(state.data)
                is Resource.Error -> showSnackbar(state.message)
                is Resource.Loading -> binding.swipeRefresh.isRefreshing = true
                else -> binding.swipeRefresh.isRefreshing = false
            }
        }
        
        // 观察加载更多状态
        viewModel.loadMoreState.observe(viewLifecycleOwner) { hasMore ->
            binding.loadMoreProgress.isVisible = hasMore
            binding.noMoreText.isVisible = !hasMore
        }
    }
    
    override fun loadData() {
        viewModel.loadBannerData()
        viewModel.loadMainList()
        viewModel.loadLatestList()
    }
    
    private fun setupHeader() {
        binding.header.apply {
            setOnSearchClickListener {
                findNavController().navigate(R.id.action_main_to_search)
            }
            setOnMenuClickListener {
                // 打开侧边菜单或其他功能
            }
        }
    }
    
    private fun setupBanner() {
        bannerAdapter = BannerAdapter { banner ->
            // 处理轮播图点击
            navigateToBannerTarget(banner)
        }
        
        binding.bannerViewPager.apply {
            adapter = bannerAdapter
            offscreenPageLimit = 1
            setPageTransformer(ScaleInTransformer())
            
            // 自动轮播
            startAutoScroll(3000L)
        }
        
        // 设置指示器
        binding.bannerIndicator.setViewPager(binding.bannerViewPager)
    }
    
    private fun setupComicCarousel() {
        comicCarouselAdapter = ComicCarouselAdapter(
            onItemClick = { comic ->
                navigateToComicDetail(comic.id)
            },
            onLikeClick = { comic ->
                handleLikeAction(comic)
            },
            onBookmarkClick = { comic ->
                handleBookmarkAction(comic)
            }
        )
        
        binding.comicCarousel.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = comicCarouselAdapter
            addItemDecoration(LinearHorizontalSpacingDecoration(16))
        }
    }
    
    private fun setupComicList() {
        comicListAdapter = ComicListAdapter(
            onItemClick = { comic ->
                navigateToComicDetail(comic.id)
            },
            onLikeClick = { comic ->
                handleLikeAction(comic)
            },
            onBookmarkClick = { comic ->
                handleBookmarkAction(comic)
            }
        )
        
        binding.comicList.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = comicListAdapter
            addItemDecoration(GridSpacingItemDecoration(3, 16, true))
        }
        
        // 加载更多监听
        binding.comicList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= 30
                ) {
                    viewModel.loadMoreLatestList()
                }
            }
        })
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.color_primary)
            setOnRefreshListener {
                viewModel.refreshAllData()
            }
        }
    }
    
    private fun handleLikeAction(comic: Comic) {
        if (AuthManager.isLoggedIn()) {
            viewModel.toggleLike(comic.id)
        } else {
            showSnackbar("请先登录")
            findNavController().navigate(R.id.action_main_to_login)
        }
    }
    
    private fun handleBookmarkAction(comic: Comic) {
        if (AuthManager.isLoggedIn()) {
            viewModel.toggleBookmark(comic.id)
        } else {
            showSnackbar("请先登录")
            findNavController().navigate(R.id.action_main_to_login)
        }
    }
    
    private fun navigateToBannerTarget(banner: Banner) {
        when (banner.type) {
            "comic" -> navigateToComicDetail(banner.targetId)
            "url" -> openWebView(banner.url)
            "category" -> navigateToCategory(banner.targetId)
        }
    }
    
    private fun navigateToComicDetail(comicId: String) {
        val direction = MainFragmentDirections.actionMainToComicDetail(comicId)
        findNavController().navigate(direction)
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        binding.bannerViewPager.stopAutoScroll()
        super.onDestroyView()
    }
}
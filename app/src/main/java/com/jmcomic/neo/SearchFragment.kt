class SearchFragment : Fragment() {
    
    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()
    
    // 使用现有的ComicListAdapter
    private lateinit var searchResultAdapter: ComicListAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var hotKeywordsAdapter: HotKeywordsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAdapters()
        setupSearchView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAdapters() {
        // 使用现有的ComicListAdapter显示搜索结果
        searchResultAdapter = ComicListAdapter(
            onItemClick = { comic ->
                navigateToComicDetail(comic.id)
            },
            onLikeClick = { comicId ->
                // 处理喜欢点击
            },
            onBookmarkClick = { comicId ->
                // 处理收藏点击
            }
        )
        binding.searchResultsRecyclerView.adapter = searchResultAdapter

        // 搜索历史适配器
        searchHistoryAdapter = SearchHistoryAdapter(
            onHistoryClick = { query ->
                binding.searchView.setQuery(query, true)
            },
            onHistoryDelete = { query ->
                // 从历史中删除
            }
        )
        binding.searchHistoryRecyclerView.adapter = searchHistoryAdapter

        // 热门关键词适配器
        hotKeywordsAdapter = HotKeywordsAdapter { keyword ->
            binding.searchView.setQuery(keyword, true)
        }
        binding.hotKeywordsRecyclerView.adapter = hotKeywordsAdapter
    }

    private fun setupSearchView() {
        // 设置搜索框监听
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query.isNotBlank()) {
                    viewModel.search(query)
                    hideSuggestions()
                    showResults()
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isBlank()) {
                    showSuggestions()
                    hideResults()
                }
                return true
            }
        })

        // 设置搜索框打开/关闭监听
        binding.searchView.setOnSearchClickListener {
            showSuggestions()
        }

        binding.searchView.setOnCloseListener {
            hideSuggestions()
            hideResults()
            false
        }
    }

    private fun setupObservers() {
        // 观察搜索结果
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading()
                    is Resource.Success -> {
                        hideLoading()
                        searchResultAdapter.submitList(resource.data ?: emptyList())
                        if (resource.data.isNullOrEmpty()) {
                            showEmptyState()
                        } else {
                            showResults()
                        }
                    }
                    is Resource.Error -> {
                        hideLoading()
                        showError(resource.message ?: "搜索失败")
                    }
                    is Resource.Idle -> {
                        hideLoading()
                        hideResults()
                    }
                }
            }
        }

        // 观察搜索历史
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchHistory.collect { history ->
                searchHistoryAdapter.submitList(history)
                binding.searchHistorySection.isVisible = history.isNotEmpty()
            }
        }

        // 观察热门关键词
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hotKeywords.collect { keywords ->
                hotKeywordsAdapter.submitList(keywords)
            }
        }
    }

    private fun setupClickListeners() {
        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }

        binding.retryButton.setOnClickListener {
            val currentQuery = binding.searchView.query.toString()
            if (currentQuery.isNotBlank()) {
                viewModel.search(currentQuery)
            }
        }
    }

    private fun showSuggestions() {
        binding.suggestionsLayout.isVisible = true
        binding.resultsLayout.isVisible = false
        binding.emptyStateLayout.isVisible = false
    }

    private fun showResults() {
        binding.suggestionsLayout.isVisible = false
        binding.resultsLayout.isVisible = true
        binding.emptyStateLayout.isVisible = false
    }

    private fun showEmptyState() {
        binding.suggestionsLayout.isVisible = false
        binding.resultsLayout.isVisible = false
        binding.emptyStateLayout.isVisible = true
    }

    private fun hideSuggestions() {
        binding.suggestionsLayout.isVisible = false
    }

    private fun hideResults() {
        binding.resultsLayout.isVisible = false
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
    }

    private fun hideLoading() {
        binding.progressBar.isVisible = false
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        showEmptyState()
    }

    private fun navigateToComicDetail(comicId: String) {
        // 使用现有的导航到详情页逻辑
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToComicDetailFragment(comicId)
        )
    }
}
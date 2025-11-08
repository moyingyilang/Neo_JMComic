@HiltViewModel
class MainViewModel @Inject constructor(
    private val comicRepository: ComicRepository,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _bannerState = MutableStateFlow<Resource<List<Banner>>>(Resource.Idle)
    val bannerState: StateFlow<Resource<List<Banner>>> = _bannerState.asStateFlow()
    
    private val _mainListState = MutableStateFlow<Resource<List<ComicSection>>>(Resource.Idle)
    val mainListState: StateFlow<Resource<List<ComicSection>>> = _mainListState.asStateFlow()
    
    private val _latestListState = MutableStateFlow<Resource<List<Comic>>>(Resource.Idle)
    val latestListState: StateFlow<Resource<List<Comic>>> = _latestListState.asStateFlow()
    
    private val _loadMoreState = MutableStateFlow(false)
    val loadMoreState: StateFlow<Boolean> = _loadMoreState.asStateFlow()
    
    private var currentPage = 0
    private var hasMore = true
    
    init {
        loadInitialData()
    }
    
    fun loadInitialData() {
        loadBannerData()
        loadMainList()
        loadLatestList()
    }
    
    fun loadBannerData() {
        viewModelScope.launch {
            _bannerState.value = Resource.Loading
            try {
                val banners = comicRepository.getBanners()
                _bannerState.value = Resource.Success(banners)
            } catch (e: Exception) {
                _bannerState.value = Resource.Error(e.message ?: "加载失败")
            }
        }
    }
    
    fun loadMainList() {
        viewModelScope.launch {
            _mainListState.value = Resource.Loading
            try {
                val mainList = comicRepository.getMainList()
                _mainListState.value = Resource.Success(mainList)
            } catch (e: Exception) {
                _mainListState.value = Resource.Error(e.message ?: "加载失败")
            }
        }
    }
    
    fun loadLatestList() {
        viewModelScope.launch {
            _latestListState.value = Resource.Loading
            try {
                val latestList = comicRepository.getLatestList(page = 0)
                _latestListState.value = Resource.Success(latestList)
                currentPage = 0
                hasMore = latestList.isNotEmpty()
            } catch (e: Exception) {
                _latestListState.value = Resource.Error(e.message ?: "加载失败")
            }
        }
    }
    
    fun loadMoreLatestList() {
        if (!hasMore) return
        
        viewModelScope.launch {
            _loadMoreState.value = true
            try {
                val nextPage = currentPage + 1
                val moreList = comicRepository.getLatestList(page = nextPage)
                
                if (moreList.isNotEmpty()) {
                    val currentList = (_latestListState.value as? Resource.Success)?.data ?: emptyList()
                    _latestListState.value = Resource.Success(currentList + moreList)
                    currentPage = nextPage
                } else {
                    hasMore = false
                }
            } catch (e: Exception) {
                // 静默处理加载更多错误
                Log.e("MainViewModel", "Load more failed", e)
            } finally {
                _loadMoreState.value = false
            }
        }
    }
    
    fun refreshAllData() {
        currentPage = 0
        hasMore = true
        loadInitialData()
    }
    
    fun toggleLike(comicId: String) {
        viewModelScope.launch {
            try {
                comicRepository.toggleLike(comicId)
                // 更新本地状态
                updateComicLikeState(comicId)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    fun toggleBookmark(comicId: String) {
        viewModelScope.launch {
            try {
                comicRepository.toggleBookmark(comicId)
                // 更新本地状态
                updateComicBookmarkState(comicId)
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    private fun updateComicLikeState(comicId: String) {
        // 更新对应漫画的喜欢状态
        val currentList = (_latestListState.value as? Resource.Success)?.data
        currentList?.find { it.id == comicId }?.let { comic ->
            comic.isLiked = !comic.isLiked
            _latestListState.value = Resource.Success(currentList)
        }
    }
    
    private fun updateComicBookmarkState(comicId: String) {
        // 更新对应漫画的收藏状态
        val currentList = (_latestListState.value as? Resource.Success)?.data
        currentList?.find { it.id == comicId }?.let { comic ->
            comic.isBookmarked = !comic.isBookmarked
            _latestListState.value = Resource.Success(currentList)
        }
    }
}
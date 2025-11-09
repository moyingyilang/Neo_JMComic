// MainViewModel.kt - 整合版本
@HiltViewModel
class MainViewModel @Inject constructor(
    private val comicRepository: ComicRepository,
    private val authManager: AuthManager
) : ViewModel() {
    
    // 使用现有的Resource模式
    private val _bannerState = MutableStateFlow<Resource<List<Banner>>>(Resource.Idle)
    val bannerState: StateFlow<Resource<List<Banner>>> = _bannerState.asStateFlow()
    
    private val _latestListState = MutableStateFlow<Resource<List<Comic>>>(Resource.Idle)
    val latestListState: StateFlow<Resource<List<Comic>>> = _latestListState.asStateFlow()
    
    private val _loadMoreState = MutableStateFlow(false)
    val loadMoreState: StateFlow<Boolean> = _loadMoreState.asStateFlow()
    
    private var currentPage = 1
    private var hasMore = true
    
    init {
        loadInitialData()
    }
    
    fun loadInitialData() {
        loadBannerData()
        loadLatestList()
    }
    
    fun loadBannerData() {
        viewModelScope.launch {
            _bannerState.value = Resource.Loading
            try {
                // 使用现有的ComicRepository获取banner数据
                val banners = comicRepository.fetchCoverAds().getOrElse { emptyList() }
                _bannerState.value = Resource.Success(banners)
            } catch (e: Exception) {
                _bannerState.value = Resource.Error(e.message ?: "Banner加载失败")
            }
        }
    }
    
    fun loadLatestList() {
        viewModelScope.launch {
            _latestListState.value = Resource.Loading
            try {
                val latestList = comicRepository.fetchLatestList(page = 1).getOrElse { emptyList() }
                _latestListState.value = Resource.Success(latestList)
                currentPage = 1
                hasMore = latestList.isNotEmpty()
            } catch (e: Exception) {
                _latestListState.value = Resource.Error(e.message ?: "列表加载失败")
            }
        }
    }
    
    fun loadMoreLatestList() {
        if (!hasMore || _loadMoreState.value) return
        
        viewModelScope.launch {
            _loadMoreState.value = true
            try {
                val nextPage = currentPage + 1
                val moreList = comicRepository.fetchLatestList(page = nextPage).getOrElse { emptyList() }
                
                val currentList = (_latestListState.value as? Resource.Success)?.data ?: emptyList()
                _latestListState.value = Resource.Success(currentList + moreList)
                currentPage = nextPage
                hasMore = moreList.isNotEmpty()
            } catch (e: Exception) {
                // 静默处理加载更多错误
                Log.e("MainViewModel", "Load more failed", e)
            } finally {
                _loadMoreState.value = false
            }
        }
    }
    
    fun refreshAllData() {
        currentPage = 1
        hasMore = true
        loadInitialData()
    }
    
    fun toggleLike(comicId: String) {
        viewModelScope.launch {
            try {
                // 使用现有的喜欢/收藏逻辑
                val success = comicRepository.toggleLike(comicId)
                if (success) {
                    updateComicLikeState(comicId)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Toggle like failed", e)
            }
        }
    }
    
    fun toggleBookmark(comicId: String) {
        viewModelScope.launch {
            try {
                val success = comicRepository.toggleBookmark(comicId)
                if (success) {
                    updateComicBookmarkState(comicId)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Toggle bookmark failed", e)
            }
        }
    }
    
    private fun updateComicLikeState(comicId: String) {
        val currentList = (_latestListState.value as? Resource.Success)?.data
        currentList?.find { it.id == comicId }?.let { comic ->
            // 更新喜欢状态
            val updatedComic = comic.copy(isLiked = !comic.isLiked)
            val updatedList = currentList.map { if (it.id == comicId) updatedComic else it }
            _latestListState.value = Resource.Success(updatedList)
        }
    }
    
    private fun updateComicBookmarkState(comicId: String) {
        val currentList = (_latestListState.value as? Resource.Success)?.data
        currentList?.find { it.id == comicId }?.let { comic ->
            // 更新收藏状态
            val updatedComic = comic.copy(isBookmarked = !comic.isBookmarked)
            val updatedList = currentList.map { if (it.id == comicId) updatedComic else it }
            _latestListState.value = Resource.Success(updatedList)
        }
    }
}
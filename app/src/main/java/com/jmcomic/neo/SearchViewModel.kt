@HiltViewModel
class SearchViewModel @Inject constructor(
    private val comicRepository: ComicRepository, // 使用现有的Repository
    private val authManager: AuthManager // 使用现有的AuthManager
) : ViewModel() {

    // 使用现有的Resource模式
    private val _searchState = MutableStateFlow<Resource<List<Comic>>>(Resource.Idle)
    val searchState: StateFlow<Resource<List<Comic>>> = _searchState.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _hotKeywords = MutableStateFlow<List<String>>(emptyList())
    val hotKeywords: StateFlow<List<String>> = _hotKeywords.asStateFlow()

    private var currentQuery: String = ""

    init {
        loadSearchHistory()
        loadHotKeywords()
    }

    fun search(query: String) {
        currentQuery = query
        _searchState.value = Resource.Loading
        
        viewModelScope.launch {
            try {
                // 使用现有的ComicRepository搜索逻辑
                val result = comicRepository.searchComics(query)
                _searchState.value = Resource.Success(result)
                
                // 保存搜索历史
                saveToSearchHistory(query)
            } catch (e: Exception) {
                _searchState.value = Resource.Error(e.message ?: "搜索失败")
            }
        }
    }

    fun clearSearch() {
        currentQuery = ""
        _searchState.value = Resource.Idle
    }

    fun loadMore() {
        // 基于现有的分页逻辑
        viewModelScope.launch {
            val currentList = (_searchState.value as? Resource.Success)?.data ?: emptyList()
            // 实现分页加载...
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            // 从SharedPreferences加载搜索历史
            val history = loadSearchHistoryFromStorage()
            _searchHistory.value = history
        }
    }

    private fun loadHotKeywords() {
        viewModelScope.launch {
            try {
                // 从API获取热门关键词
                val keywords = comicRepository.getHotSearchKeywords()
                _hotKeywords.value = keywords
            } catch (e: Exception) {
                // 使用默认热门关键词
                _hotKeywords.value = listOf("热门", "推荐", "新作", "经典")
            }
        }
    }

    private fun saveToSearchHistory(query: String) {
        viewModelScope.launch {
            val currentHistory = _searchHistory.value.toMutableList()
            // 移除重复项
            currentHistory.remove(query)
            // 添加到开头
            currentHistory.add(0, query)
            // 限制历史记录数量
            if (currentHistory.size > 10) {
                currentHistory.removeLast()
            }
            _searchHistory.value = currentHistory
            saveSearchHistoryToStorage(currentHistory)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            _searchHistory.value = emptyList()
            clearSearchHistoryFromStorage()
        }
    }

    private suspend fun loadSearchHistoryFromStorage(): List<String> {
        // 使用现有的SharedPreferences工具
        return emptyList() // 实际实现从存储加载
    }

    private suspend fun saveSearchHistoryToStorage(history: List<String>) {
        // 使用现有的存储工具
    }

    private suspend fun clearSearchHistoryFromStorage() {
        // 使用现有的存储工具
    }
}
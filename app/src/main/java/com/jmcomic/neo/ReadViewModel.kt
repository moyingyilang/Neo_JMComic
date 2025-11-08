// ReadViewModel.kt
class ReadViewModel : ViewModel() {
    
    private val repository = ComicRepository()
    
    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage
    
    private val _chapterPages = MutableLiveData<List<ComicPage>>()
    val chapterPages: LiveData<List<ComicPage>> = _chapterPages
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _readingMode = MutableLiveData(ReadingMode.HORIZONTAL)
    val readingMode: LiveData<ReadingMode> = _readingMode
    
    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }
    
    fun setReadingMode(mode: ReadingMode) {
        _readingMode.value = mode
    }
    
    fun loadChapter(comicId: String, chapterId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pages = repository.getChapterPages(comicId, chapterId)
                _chapterPages.value = pages
                
                // 恢复阅读进度
                val progress = ReadingProgressManager.getProgress(comicId, chapterId)
                _currentPage.value = progress
                
            } catch (e: Exception) {
                Log.e("ReadViewModel", "Load chapter failed", e)
                // 处理错误
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadNextChapter(comicId: String, currentChapterId: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val nextChapter = repository.getNextChapter(comicId, currentChapterId)
                callback(nextChapter?.id)
            } catch (e: Exception) {
                Log.e("ReadViewModel", "Load next chapter failed", e)
                callback(null)
            }
        }
    }
}

// 漫画页面数据类
data class ComicPage(
    val id: String,
    val imageUrl: String,
    val width: Int,
    val height: Int,
    val pageNumber: Int
)
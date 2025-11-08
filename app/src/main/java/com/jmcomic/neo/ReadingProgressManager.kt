// ReadingProgressManager.kt
object ReadingProgressManager {
    
    private const val PREF_NAME = "reading_progress"
    private const val KEY_PROGRESS = "progress_"
    
    fun saveProgress(comicId: String, chapterId: String, page: Int, timestamp: Long) {
        val prefs = getSharedPreferences()
        val key = "$KEY_PROGRESS${comicId}_$chapterId"
        val progress = ReadingProgress(page, timestamp)
        prefs.edit().putString(key, Gson().toJson(progress)).apply()
    }
    
    fun getProgress(comicId: String, chapterId: String): Int {
        val prefs = getSharedPreferences()
        val key = "$KEY_PROGRESS${comicId}_$chapterId"
        val json = prefs.getString(key, null)
        return if (json != null) {
            Gson().fromJson(json, ReadingProgress::class.java).page
        } else {
            0
        }
    }
    
    fun getRecentRead(): List<ReadingHistory> {
        // 获取最近阅读记录
        val prefs = getSharedPreferences()
        val allEntries = prefs.all
        val histories = mutableListOf<ReadingHistory>()
        
        allEntries.forEach { (key, value) ->
            if (key.startsWith(KEY_PROGRESS)) {
                val progress = Gson().fromJson(value as String, ReadingProgress::class.java)
                val ids = key.removePrefix(KEY_PROGRESS).split("_")
                if (ids.size == 2) {
                    histories.add(ReadingHistory(ids[0], ids[1], progress))
                }
            }
        }
        
        return histories.sortedByDescending { it.progress.timestamp }
    }
    
    private fun getSharedPreferences(): SharedPreferences {
        return App.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}

data class ReadingProgress(
    val page: Int,
    val timestamp: Long
)

data class ReadingHistory(
    val comicId: String,
    val chapterId: String,
    val progress: ReadingProgress
)
// ReadActivity.kt
class ReadActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReadBinding
    private lateinit var viewModel: ReadViewModel
    private lateinit var adapter: ComicPageAdapter
    private var comicId: String = ""
    private var chapterId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 隐藏系统UI，全屏阅读
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        initData()
        setupViewModel()
        setupViewPager()
        setupControls()
        loadChapterData()
    }
    
    private fun initData() {
        comicId = intent.getStringExtra("comicId") ?: ""
        chapterId = intent.getStringExtra("chapterId") ?: ""
        
        if (comicId.isEmpty() || chapterId.isEmpty()) {
            Toast.makeText(this, "漫画数据错误", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ReadViewModel::class.java]
        
        viewModel.currentPage.observe(this) { page ->
            updatePageIndicator(page)
        }
        
        viewModel.chapterPages.observe(this) { pages ->
            adapter.submitList(pages)
            binding.progressBar.isVisible = false
        }
        
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.isVisible = loading
        }
        
        viewModel.readingMode.observe(this) { mode ->
            updateReadingMode(mode)
        }
    }
    
    private fun setupViewPager() {
        adapter = ComicPageAdapter { page ->
            toggleControls()
        }
        
        binding.viewPager.apply {
            this.adapter = adapter
            offscreenPageLimit = 2
            
            // 添加页面变化监听
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    viewModel.setCurrentPage(position)
                    saveReadingProgress(position)
                }
            })
        }
    }
    
    private fun setupControls() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // 目录按钮
        binding.btnChapters.setOnClickListener {
            showChapterList()
        }
        
        // 设置按钮
        binding.btnSettings.setOnClickListener {
            showReadingSettings()
        }
        
        // 上一页/下一页
        binding.btnPrev.setOnClickListener {
            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.currentItem = binding.viewPager.currentItem - 1
            }
        }
        
        binding.btnNext.setOnClickListener {
            val totalPages = viewModel.chapterPages.value?.size ?: 0
            if (binding.viewPager.currentItem < totalPages - 1) {
                binding.viewPager.currentItem = binding.viewPager.currentItem + 1
            } else {
                loadNextChapter()
            }
        }
        
        // 点击区域控制显示/隐藏
        binding.controlOverlay.setOnClickListener {
            toggleControls()
        }
    }
    
    private fun toggleControls() {
        val isVisible = binding.controlLayout.isVisible
        binding.controlLayout.isVisible = !isVisible
        
        if (!isVisible) {
            // 3秒后自动隐藏控制栏
            binding.controlLayout.postDelayed({
                binding.controlLayout.isVisible = false
            }, 3000)
        }
    }
    
    private fun updatePageIndicator(currentPage: Int) {
        val totalPages = viewModel.chapterPages.value?.size ?: 0
        binding.tvPageIndicator.text = "${currentPage + 1}/$totalPages"
    }
    
    private fun updateReadingMode(mode: ReadingMode) {
        when (mode) {
            ReadingMode.HORIZONTAL -> {
                binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            }
            ReadingMode.VERTICAL -> {
                binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            }
        }
    }
    
    private fun loadChapterData() {
        viewModel.loadChapter(comicId, chapterId)
    }
    
    private fun saveReadingProgress(position: Int) {
        ReadingProgressManager.saveProgress(
            comicId, 
            chapterId, 
            position,
            System.currentTimeMillis()
        )
    }
    
    private fun loadNextChapter() {
        viewModel.loadNextChapter(comicId, chapterId) { nextChapterId ->
            if (nextChapterId != null) {
                this.chapterId = nextChapterId
                loadChapterData()
                binding.viewPager.currentItem = 0
            } else {
                Toast.makeText(this, "已经是最后一章", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showChapterList() {
        val dialog = ChapterListDialogFragment.newInstance(comicId, chapterId) { selectedChapterId ->
            this.chapterId = selectedChapterId
            loadChapterData()
            binding.viewPager.currentItem = 0
        }
        dialog.show(supportFragmentManager, "chapter_list")
    }
    
    private fun showReadingSettings() {
        val dialog = ReadingSettingsDialogFragment(
            currentMode = viewModel.readingMode.value ?: ReadingMode.HORIZONTAL
        ) { newMode ->
            viewModel.setReadingMode(newMode)
        }
        dialog.show(supportFragmentManager, "reading_settings")
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 音量键翻页
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                binding.btnPrev.performClick()
                true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                binding.btnNext.performClick()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

// 阅读模式枚举
enum class ReadingMode {
    HORIZONTAL, VERTICAL
}
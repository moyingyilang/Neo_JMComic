// ChapterListDialogFragment.kt
class ChapterListDialogFragment : DialogFragment() {
    
    private var comicId: String = ""
    private var currentChapterId: String = ""
    private var onChapterSelected: ((String) -> Unit)? = null
    
    private lateinit var binding: DialogChapterListBinding
    private lateinit var adapter: ChapterListAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogChapterListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadChapterList()
    }
    
    private fun setupRecyclerView() {
        adapter = ChapterListAdapter(currentChapterId) { chapter ->
            onChapterSelected?.invoke(chapter.id)
            dismiss()
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChapterListDialogFragment.adapter
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
    }
    
    private fun loadChapterList() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.isVisible = true
            try {
                val chapters = ComicRepository().getChapterList(comicId)
                adapter.submitList(chapters)
            } catch (e: Exception) {
                Log.e("ChapterListDialog", "Load chapters failed", e)
                Toast.makeText(requireContext(), "加载章节列表失败", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }
    
    companion object {
        fun newInstance(
            comicId: String, 
            currentChapterId: String, 
            onChapterSelected: (String) -> Unit
        ): ChapterListDialogFragment {
            return ChapterListDialogFragment().apply {
                this.comicId = comicId
                this.currentChapterId = currentChapterId
                this.onChapterSelected = onChapterSelected
            }
        }
    }
}
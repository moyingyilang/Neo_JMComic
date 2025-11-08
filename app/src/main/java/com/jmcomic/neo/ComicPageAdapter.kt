// ComicPageAdapter.kt
class ComicPageAdapter(
    private val onPageClick: (Int) -> Unit
) : ListAdapter<ComicPage, ComicPageAdapter.PageViewHolder>(DiffCallback) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val binding = ItemComicPageBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return PageViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val page = getItem(position)
        holder.bind(page)
    }
    
    inner class PageViewHolder(
        private val binding: ItemComicPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.photoView.setOnClickListener {
                onPageClick(adapterPosition)
            }
        }
        
        fun bind(page: ComicPage) {
            // 使用Glide加载图片
            Glide.with(binding.root.context)
                .load(page.imageUrl)
                .placeholder(R.drawable.placeholder_comic)
                .error(R.drawable.error_comic)
                .into(binding.photoView)
            
            // 设置图片缩放和双击手势
            setupPhotoView(binding.photoView)
        }
        
        private fun setupPhotoView(photoView: PhotoView) {
            photoView.apply {
                maximumScale = 5.0f
                mediumScale = 2.0f
                minimumScale = 1.0f
                scale = 1.0f
                
                setOnDoubleTapListener(object : DefaultOnDoubleTapListener(this) {
                    override fun onDoubleTap(e: MotionEvent?): Boolean {
                        if (scale > 1.0f) {
                            setScale(1.0f, true)
                        } else {
                            setScale(2.0f, e?.x ?: 0f, e?.y ?: 0f, true)
                        }
                        return true
                    }
                })
            }
        }
    }
    
    object DiffCallback : DiffUtil.ItemCallback<ComicPage>() {
        override fun areItemsTheSame(oldItem: ComicPage, newItem: ComicPage): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ComicPage, newItem: ComicPage): Boolean {
            return oldItem == newItem
        }
    }
}
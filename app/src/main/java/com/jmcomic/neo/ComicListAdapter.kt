// ComicListAdapter.kt - 基于Comic.tsx重写
class ComicListAdapter(
    private val onComicClick: (Comic) -> Unit,
    private val onLikeClick: (Comic) -> Unit,
    private val onBookmarkClick: (Comic) -> Unit
) : ListAdapter<Comic, ComicListAdapter.ComicViewHolder>(ComicDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicViewHolder {
        val binding = ItemComicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ComicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComicViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ComicViewHolder(private val binding: ItemComicBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comic: Comic) {
            binding.root.setOnClickListener { onComicClick(comic) }
            
            // 加载漫画封面
            Glide.with(binding.root)
                .load(comic.coverUrl)
                .placeholder(R.drawable.comic_placeholder)
                .error(R.drawable.comic_error)
                .into(binding.comicCover)
            
            binding.comicTitle.text = comic.title
            binding.comicAuthor.text = comic.author
            
            // 设置标签
            setupTags(comic.tags)
            
            // 设置喜欢和收藏状态
            binding.likeButton.apply {
                setImageResource(
                    if (comic.isLiked) R.drawable.ic_favorite_filled 
                    else R.drawable.ic_favorite_border
                )
                setOnClickListener { onLikeClick(comic) }
            }
            
            binding.bookmarkButton.apply {
                setImageResource(
                    if (comic.isBookmarked) R.drawable.ic_bookmark_filled 
                    else R.drawable.ic_bookmark_border
                )
                setOnClickListener { onBookmarkClick(comic) }
            }
            
            // 设置更新时间和状态
            binding.updateTime.text = formatUpdateTime(comic.lastUpdate)
            binding.newBadge.visibility = if (comic.isNew) View.VISIBLE else View.GONE
            binding.hotBadge.visibility = if (comic.isHot) View.VISIBLE else View.GONE
        }
        
        private fun setupTags(tags: List<String>) {
            binding.tagsContainer.removeAllViews()
            tags.take(3).forEach { tag ->
                val tagView = TextView(binding.root.context).apply {
                    text = tag
                    setTextColor(ContextCompat.getColor(context, R.color.tag_text))
                    setBackgroundResource(R.drawable.tag_background)
                    setPadding(8.dp, 4.dp, 8.dp, 4.dp)
                    textSize = 10f
                }
                binding.tagsContainer.addView(tagView)
            }
        }
        
        private fun formatUpdateTime(timestamp: Long): String {
            return DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        }
        
        private val Int.dp: Int get() = (this * binding.root.context.resources.displayMetrics.density).toInt()
    }

    object ComicDiffCallback : DiffUtil.ItemCallback<Comic>() {
        override fun areItemsTheSame(oldItem: Comic, newItem: Comic): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comic, newItem: Comic): Boolean {
            return oldItem == newItem
        }
    }
}
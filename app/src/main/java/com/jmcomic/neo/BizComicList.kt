package com.yourpackage.neojmcomic.utils.biz

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.yourpackage.neojmcomic.utils.persistence.PersistenceStorage
import kotlinx.android.synthetic.main.activity_comic_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 漫画列表页（支持分类、分页、收藏状态、下拉刷新） */
class BizComicList : AppCompatActivity() {
    private lateinit var adapter: ComicListAdapter
    private val comicList = mutableListOf<ComicModel>()
    private var page = 1
    private val pageSize = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_list)

        // 初始化RecyclerView（6列网格布局）
        recycler_view.layoutManager = GridLayoutManager(this, 6)
        adapter = ComicListAdapter(comicList) { comic ->
            // 跳转阅读页
            val intent = android.content.Intent(this, BizComicReader::class.java)
            intent.putExtra("comicId", comic.id)
            intent.putExtra("chapterId", comic.firstChapterId)
            intent.putStringArrayListExtra("imageUrls", ArrayList(comic.imageUrls))
            startActivity(intent)
        }
        recycler_view.adapter = adapter

        // 下拉刷新
        swipe_refresh.setOnRefreshListener {
            page = 1
            loadComicList(true)
        }

        // 加载更多
        btn_load_more.setOnClickListener {
            loadComicList(false)
        }

        // 初始加载
        loadComicList(true)
    }

    /** 加载漫画列表 */
    private fun loadComicList(isRefresh: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 模拟网络请求
            val newList = mockComicList(page, pageSize)
            withContext(Dispatchers.Main) {
                if (isRefresh) {
                    comicList.clear()
                    swipe_refresh.isRefreshing = false
                }
                comicList.addAll(newList)
                adapter.notifyDataSetChanged()
                page++
            }
        }
    }

    /** 模拟漫画数据 */
    private fun mockComicList(page: Int, pageSize: Int): List<ComicModel> {
        val list = mutableListOf<ComicModel>()
        for (i in (page - 1) * pageSize until page * pageSize) {
            val id = "comic_$i"
            list.add(
                ComicModel(
                    id = id,
                    name = "漫画标题$i",
                    author = "作者$i",
                    coverUrl = "https://picsum.photos/200/300?random=$i",
                    category = "分类${i % 5}",
                    isCollected = PersistenceStorage.getBoolean("collected_$id", false),
                    firstChapterId = "chapter_${i}_1",
                    imageUrls = listOf(
                        "https://picsum.photos/800/1200?random=${i}_1",
                        "https://picsum.photos/800/1200?random=${i}_2",
                        "https://picsum.photos/800/1200?random=${i}_3"
                    )
                )
            )
        }
        return list
    }

    /** 漫画数据模型 */
    data class ComicModel(
        val id: String,
        val name: String,
        val author: String,
        val coverUrl: String,
        val category: String,
        val isCollected: Boolean,
        val firstChapterId: String,
        val imageUrls: List<String>
    )

    /** 漫画列表适配器 */
    class ComicListAdapter(
        private val list: List<ComicModel>,
        private val onItemClick: (ComicModel) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ComicListAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val ivCover = itemView.findViewById<android.widget.ImageView>(R.id.iv_cover)
            val tvName = itemView.findViewById<android.widget.TextView>(R.id.tv_name)
            val tvAuthor = itemView.findViewById<android.widget.TextView>(R.id.tv_author)
            val tvCategory = itemView.findViewById<android.widget.TextView>(R.id.tv_category)
            val ivCollect = itemView.findViewById<android.widget.ImageView>(R.id.iv_collect)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comic, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val comic = list[position]
            // 加载封面
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(comic.coverUrl)
                .into(holder.ivCover)
            holder.tvName.text = comic.name
            holder.tvAuthor.text = comic.author
            holder.tvCategory.text = comic.category
            holder.ivCollect.setImageResource(
                if (comic.isCollected) R.drawable.ic_collected else R.drawable.ic_uncollect
            )

            // 点击事件
            holder.itemView.setOnClickListener { onItemClick(comic) }
            holder.ivCollect.setOnClickListener {
                PersistenceStorage.putBoolean("collected_${comic.id}", !comic.isCollected)
                holder.ivCollect.setImageResource(
                    if (!comic.isCollected) R.drawable.ic_collected else R.drawable.ic_uncollect
                )
            }
        }

        override fun getItemCount() = list.size
    }
}

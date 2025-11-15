package com.yourpackage.neojmcomic.utils.biz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourpackage.neojmcomic.utils.persistence.PersistenceStorage
import kotlinx.android.synthetic.main.activity_library.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 收藏库页面（创作者/作品分类展示） */
class BizLibrary : AppCompatActivity() {
    private val creatorList = mutableListOf<CreatorModel>()
    private lateinit var creatorAdapter: CreatorAdapter
    private var currentTab = 0 // 0=创作者，1=作品

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        // 初始化Tab切换
        tab_layout.addTab(tab_layout.newTab().setText("创作者"))
        tab_layout.addTab(tab_layout.newTab().setText("作品"))
        tab_layout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                currentTab = tab.position
                loadData()
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        // 初始化适配器
        creatorAdapter = CreatorAdapter(creatorList) { creator ->
            // 跳转创作者作品列表
            val intent = android.content.Intent(this, BizCreatorWorkList::class.java)
            intent.putExtra("creatorId", creator.id)
            startActivity(intent)
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = creatorAdapter

        // 初始加载
        loadData()
    }

    /** 加载数据（根据当前Tab切换） */
    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val data = if (currentTab == 0) {
                mockCreatorList() // 创作者列表
            } else {
                mockWorkList() // 作品列表
            }
            withContext(Dispatchers.Main) {
                creatorList.clear()
                creatorList.addAll(data)
                creatorAdapter.notifyDataSetChanged()
            }
        }
    }

    /** 模拟创作者数据 */
    private fun mockCreatorList(): List<CreatorModel> {
        val list = mutableListOf<CreatorModel>()
        for (i in 0 until 10) {
            list.add(
                CreatorModel(
                    id = "creator_$i",
                    name = "创作者$i",
                    avatarUrl = "https://picsum.photos/100/100?random=$i",
                    workCount = (10..30).random()
                )
            )
        }
        return list
    }

    /** 模拟作品数据 */
    private fun mockWorkList(): List<CreatorModel> {
        val collectedIds = PersistenceStorage.getStringSet("collected_comic_ids", mutableSetOf()) ?: mutableSetOf()
        val list = mutableListOf<CreatorModel>()
        collectedIds.forEachIndexed { index, id ->
            list.add(
                CreatorModel(
                    id = id,
                    name = "收藏作品$index",
                    avatarUrl = "https://picsum.photos/200/300?random=$index",
                    workCount = 0
                )
            )
        }
        return list
    }

    /** 创作者/作品数据模型 */
    data class CreatorModel(
        val id: String,
        val name: String,
        val avatarUrl: String,
        val workCount: Int
    )

    /** 适配器 */
    class CreatorAdapter(
        private val list: List<CreatorModel>,
        private val onItemClick: (CreatorModel) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<CreatorAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val ivAvatar = itemView.findViewById<android.widget.ImageView>(R.id.iv_avatar)
            val tvName = itemView.findViewById<android.widget.TextView>(R.id.tv_name)
            val tvCount = itemView.findViewById<android.widget.TextView>(R.id.tv_count)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_library_creator, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val model = list[position]
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(model.avatarUrl)
                .into(holder.ivAvatar)
            holder.tvName.text = model.name
            holder.tvCount.text = if (model.workCount > 0) "作品${model.workCount}部" else "已收藏"
            holder.itemView.setOnClickListener { onItemClick(model) }
        }

        override fun getItemCount() = list.size
    }
}

package com.yourpackage.neojmcomic.utils.biz

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.slider.Slider
import com.yourpackage.neojmcomic.utils.anim.AnimCore
import com.yourpackage.neojmcomic.utils.persistence.PersistenceStorage
import kotlinx.android.synthetic.main.activity_comic_reader.*

/** 漫画阅读页（支持横竖屏、翻页、章节切换、进度保存） */
class BizComicReader : AppCompatActivity() {
    private lateinit var comicId: String
    private lateinit var chapterId: String
    private var isVertical = true // 默认竖屏
    private val imageUrls = mutableListOf<String>()
    private lateinit var adapter: ComicReaderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic_reader)

        // 获取参数
        comicId = intent.getStringExtra("comicId") ?: ""
        chapterId = intent.getStringExtra("chapterId") ?: ""
        imageUrls.addAll(intent.getStringArrayListExtra("imageUrls") ?: mutableListOf())

        // 初始化适配器
        adapter = ComicReaderAdapter(imageUrls, isVertical)
        view_pager.adapter = adapter
        view_pager.orientation = if (isVertical) ViewPager2.ORIENTATION_VERTICAL else ViewPager2.ORIENTATION_HORIZONTAL

        // 恢复阅读进度
        val lastPosition = PersistenceStorage.getInt("read_progress_$comicId-$chapterId", 0)
        view_pager.currentItem = lastPosition

        // 进度条监听
        slider.max = imageUrls.size.toFloat()
        slider.value = lastPosition.toFloat()
        slider.addOnChangeListener { _, value, _ ->
            view_pager.currentItem = value.toInt()
        }

        // 视图切换（横竖屏）
        btn_orientation.setOnClickListener {
            isVertical = !isVertical
            view_pager.orientation = if (isVertical) ViewPager2.ORIENTATION_VERTICAL else ViewPager2.ORIENTATION_HORIZONTAL
            adapter.isVertical = isVertical
            adapter.notifyDataSetChanged()
            AnimCore.animateView(it, "scaleX", 1f, 1.2f, 200) {
                AnimCore.animateView(it, "scaleX", 1.2f, 1f, 200)
            }
        }

        // 章节切换
        btn_prev_chapter.setOnClickListener {
            if (view_pager.currentItem == 0) {
                Toast.makeText(this, "已是第一章", Toast.LENGTH_SHORT).show()
            } else {
                view_pager.currentItem -= 1
            }
        }

        btn_next_chapter.setOnClickListener {
            if (view_pager.currentItem == imageUrls.size - 1) {
                Toast.makeText(this, "已是最后一章", Toast.LENGTH_SHORT).show()
            } else {
                view_pager.currentItem += 1
            }
        }

        // 收藏
        btn_collect.setOnClickListener {
            PersistenceStorage.putBoolean("collected_$comicId", true)
            Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show()
            AnimCore.animateView(it, "scaleX", 1f, 1.2f, 200) {
                AnimCore.animateView(it, "scaleX", 1.2f, 1f, 200)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 保存阅读进度
        PersistenceStorage.putInt("read_progress_$comicId-$chapterId", view_pager.currentItem)
    }

    /** 漫画阅读适配器 */
    class ComicReaderAdapter(
        private val urls: List<String>,
        var isVertical: Boolean
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ComicReaderAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val ivComic = itemView.findViewById<android.widget.ImageView>(R.id.iv_comic)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val layoutId = if (isVertical) R.layout.item_comic_vertical else R.layout.item_comic_horizontal
            val view = layoutInflater.inflate(layoutId, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // 加载图片（使用Glide/Coil）
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(urls[position])
                .into(holder.ivComic)
        }

        override fun getItemCount() = urls.size
    }
}

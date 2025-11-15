package com.yourpackage.neojmcomic.utils.biz

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.yourpackage.neojmcomic.utils.media.MediaHlsPlayer
import kotlinx.android.synthetic.main.activity_movie_player.*

/** 影视播放页（整合Media模块，支持播放控制、系列影片、相关推荐） */
class BizMoviePlayer : AppCompatActivity() {
    private lateinit var movieId: String
    private lateinit var videoUrl: String
    private lateinit var mediaPlayer: MediaHlsPlayer
    private val relatedMovies = mutableListOf<MovieModel>()
    private lateinit var relatedAdapter: RelatedMovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_player)

        // 获取参数
        movieId = intent.getStringExtra("movieId") ?: ""
        videoUrl = intent.getStringExtra("videoUrl") ?: ""
        relatedMovies.addAll(intent.getParcelableArrayListExtra("relatedMovies") ?: mutableListOf())

        // 初始化播放器
        mediaPlayer = MediaHlsPlayer(this)
        mediaPlayer.setSurfaceView(player_view)
        mediaPlayer.setSource(videoUrl)

        // 播放控制
        btn_play_pause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                btn_play_pause.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer.play()
                btn_play_pause.setImageResource(R.drawable.ic_pause)
            }
        }

        // 全屏切换
        btn_fullscreen.setOnClickListener {
            val isFullScreen = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            if (isFullScreen) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                btn_fullscreen.setImageResource(R.drawable.ic_fullscreen)
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                btn_fullscreen.setImageResource(R.drawable.ic_exit_fullscreen)
            }
        }

        // 相关影片列表
        relatedAdapter = RelatedMovieAdapter(relatedMovies) { movie ->
            // 跳转播放相关影片
            val intent = android.content.Intent(this, BizMoviePlayer::class.java)
            intent.putExtra("movieId", movie.id)
            intent.putExtra("videoUrl", movie.videoUrl)
            startActivity(intent)
        }
        recycler_related.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        recycler_related.adapter = relatedAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    /** 影视数据模型 */
    data class MovieModel(
        val id: String,
        val title: String,
        val coverUrl: String,
        val videoUrl: String
    )

    /** 相关影视适配器 */
    class RelatedMovieAdapter(
        private val list: List<MovieModel>,
        private val onItemClick: (MovieModel) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<RelatedMovieAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val ivCover = itemView.findViewById<android.widget.ImageView>(R.id.iv_related_cover)
            val tvTitle = itemView.findViewById<android.widget.TextView>(R.id.tv_related_title)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_related_movie, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val movie = list[position]
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(movie.coverUrl)
                .into(holder.ivCover)
            holder.tvTitle.text = movie.title
            holder.itemView.setOnClickListener { onItemClick(movie) }
        }

        override fun getItemCount() = list.size
    }
}

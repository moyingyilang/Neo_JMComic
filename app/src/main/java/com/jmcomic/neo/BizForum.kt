package com.yourpackage.neojmcomic.utils.biz

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_forum.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 论坛页面（发帖/回复/列表展示） */
class BizForum : AppCompatActivity() {
    private val postList = mutableListOf<PostModel>()
    private lateinit var postAdapter: PostAdapter
    private var page = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)

        // 初始化列表
        postAdapter = PostAdapter(postList) { post, replyContent ->
            // 回复帖子
            replyToPost(post.id, replyContent)
        }
        recycler_posts.layoutManager = LinearLayoutManager(this)
        recycler_posts.adapter = postAdapter

        // 发帖按钮
        btn_send_post.setOnClickListener {
            val content = et_post_content.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendPost(content)
        }

        // 加载帖子
        loadPosts()
    }

    /** 发送帖子 */
    private fun sendPost(content: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // 模拟发帖请求
            val newPost = PostModel(
                id = "post_${System.currentTimeMillis()}",
                author = "当前用户",
                avatarUrl = "https://picsum.photos/50/50?random=${System.currentTimeMillis()}",
                content = content,
                time = "刚刚",
                likeCount = 0,
                replyCount = 0,
                replies = mutableListOf()
            )
            withContext(Dispatchers.Main) {
                postList.add(0, newPost)
                postAdapter.notifyItemInserted(0)
                et_post_content.setText("")
                Toast.makeText(this@BizForum, "发帖成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 回复帖子 */
    private fun replyToPost(postId: String, content: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val reply = ReplyModel(
                id = "reply_${System.currentTimeMillis()}",
                author = "当前用户",
                avatarUrl = "https://picsum.photos/50/50?random=${System.currentTimeMillis()}",
                content = content,
                time = "刚刚"
            )
            withContext(Dispatchers.Main) {
                val index = postList.indexOfFirst { it.id == postId }
                if (index != -1) {
                    postList[index].replies.add(reply)
                    postList[index].replyCount++
                    postAdapter.notifyItemChanged(index)
                    Toast.makeText(this@BizForum, "回复成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 加载帖子列表 */
    private fun loadPosts() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 模拟加载
            val newPosts = mutableListOf<PostModel>()
            for (i in (page - 1) * 10 until page * 10) {
                newPosts.add(
                    PostModel(
                        id = "post_$i",
                        author = "用户$i",
                        avatarUrl = "https://picsum.photos/50/50?random=$i",
                        content = "论坛帖子内容$i，测试论坛功能...",
                        time = "${(1..30).random()}分钟前",
                        likeCount = (0..100).random(),
                        replyCount = (0..20).random(),
                        replies = mutableListOf()
                    )
                )
            }
            withContext(Dispatchers.Main) {
                postList.addAll(newPosts)
                postAdapter.notifyDataSetChanged()
                page++
            }
        }
    }

    /** 帖子数据模型 */
    data class PostModel(
        val id: String,
        val author: String,
        val avatarUrl: String,
        val content: String,
        val time: String,
        var likeCount: Int,
        var replyCount: Int,
        val replies: MutableList<ReplyModel>
    )

    /** 回复数据模型 */
    data class ReplyModel(
        val id: String,
        val author: String,
        val avatarUrl: String,
        val content: String,
        val time: String
    )

    /** 帖子适配器 */
    class PostAdapter(
        private val list: List<PostModel>,
        private val onReply: (PostModel, String) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<PostAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val ivAvatar = itemView.findViewById<android.widget.ImageView>(R.id.iv_author_avatar)
            val tvAuthor = itemView.findViewById<android.widget.TextView>(R.id.tv_author)
            val tvTime = itemView.findViewById<android.widget.TextView>(R.id.tv_time)
            val tvContent = itemView.findViewById<android.widget.TextView>(R.id.tv_content)
            val tvLikeCount = itemView.findViewById<android.widget.TextView>(R.id.tv_like_count)
            val tvReplyCount = itemView.findViewById<android.widget.TextView>(R.id.tv_reply_count)
            val recyclerReplies = itemView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_replies)
            val etReply = itemView.findViewById<android.widget.EditText>(R.id.et_reply)
            val btnSendReply = itemView.findViewById<android.widget.Button>(R.id.btn_send_reply)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_forum_post, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = list[position]
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(post.avatarUrl)
                .into(holder.ivAvatar)
            holder.tvAuthor.text = post.author
            holder.tvTime.text = post.time
            holder.tvContent.text = post.content
            holder.tvLikeCount.text = post.likeCount.toString()
            holder.tvReplyCount.text = post.replyCount.toString()

            // 回复列表
            val replyAdapter = ReplyAdapter(post.replies)
            holder.recyclerReplies.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.recyclerReplies.adapter = replyAdapter

            // 发送回复
            holder.btnSendReply.setOnClickListener {
                val content = holder.etReply.text.toString().trim()
                if (content.isEmpty()) {
                    Toast.makeText(holder.itemView.context, "请输入回复内容", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                onReply(post, content)
                holder.etReply.setText("")
            }
        }

        override fun getItemCount() = list.size
    }

    /** 回复适配器 */
    class ReplyAdapter(private val list: List<ReplyModel>) : androidx.recyclerview.widget.RecyclerView.Adapter<ReplyAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val ivAvatar = itemView.findViewById<android.widget.ImageView>(R.id.iv_reply_avatar)
            val tvAuthor = itemView.findViewById<android.widget.TextView>(R.id.tv_reply_author)
            val tvContent = itemView.findViewById<android.widget.TextView>(R.id.tv_reply_content)
            val tvTime = itemView.findViewById<android.widget.TextView>(R.id.tv_reply_time)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_forum_reply, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val reply = list[position]
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(reply.avatarUrl)
                .into(holder.ivAvatar)
            holder.tvAuthor.text = reply.author
            holder.tvContent.text = reply.content
            holder.tvTime.text = reply.time
        }

        override fun getItemCount() = list.size
    }
}

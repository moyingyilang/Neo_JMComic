package com.yourpackage.neojmcomic.utils.share

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yourpackage.neojmcomic.R

/** 分享底部弹窗（模仿原文件UI） */
class ShareBottomSheet(
    context: Context,
    private val shareUrl: String,
    private val title: String,
    private val imageUrl: String = "",
    private val onDismiss: () -> Unit
) : Dialog(context, R.style.BottomSheetDialogStyle) {

    // 分享平台数据
    private val sharePlatforms = listOf(
        SharePlatformItem(SharePlatform.FACEBOOK, "Facebook", R.drawable.ic_facebook),
        SharePlatformItem(SharePlatform.WHATSAPP, "WhatsApp", R.drawable.ic_whatsapp),
        SharePlatformItem(SharePlatform.TELEGRAM, "Telegram", R.drawable.ic_telegram),
        SharePlatformItem(SharePlatform.TWITTER, "Twitter", R.drawable.ic_twitter),
        SharePlatformItem(SharePlatform.WEIBO, "微博", R.drawable.ic_weibo),
        SharePlatformItem(SharePlatform.EMAIL, "邮件", R.drawable.ic_email),
        SharePlatformItem(SharePlatform.COPY_LINK, "复制链接", R.drawable.ic_copy)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_share_bottom_sheet, null)
        setContentView(view)

        // 配置弹窗位置（底部）
        val window = window ?: return
        window.setGravity(Gravity.BOTTOM)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.decorView.setPadding(0, 0, 0, 0)

        // 关闭弹窗
        view.findViewById<View>(R.id.iv_close).setOnClickListener { dismiss() }

        // 分享链接显示
        view.findViewById<TextView>(R.id.tv_share_url).text = shareUrl

        // 分享平台列表
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_share_platforms)
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        val adapter = SharePlatformAdapter(sharePlatforms) { platform ->
            ShareCore.share(context, platform, shareUrl, title, imageUrl = imageUrl)
            dismiss()
        }
        recyclerView.adapter = adapter
    }

    override fun dismiss() {
        super.dismiss()
        onDismiss()
    }

    /** 分享平台数据类 */
    data class SharePlatformItem(
        val platform: SharePlatform,
        val name: String,
        val iconRes: Int
    )

    /** 分享平台适配器 */
    inner class SharePlatformAdapter(
        private val items: List<SharePlatformItem>,
        private val onPlatformClick: (SharePlatform) -> Unit
    ) : RecyclerView.Adapter<SharePlatformAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.iv_platform_icon)
            val tvName: TextView = itemView.findViewById(R.id.tv_platform_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_share_platform, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            Glide.with(context).load(item.iconRes).into(holder.ivIcon)
            holder.tvName.text = item.name
            holder.itemView.setOnClickListener { onPlatformClick(item.platform) }
        }

        override fun getItemCount() = items.size
    }
}

// 弹窗样式（res/values/styles.xml）
/*
<style name="BottomSheetDialogStyle" parent="Theme.AppCompat.Dialog">
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:windowContentOverlay">@null</item>
    <item name="android:windowIsFloating">true</item>
    <item name="android:windowFrame">@null</item>
    <item name="android:backgroundDimEnabled">true</item>
    <item name="android:backgroundDimAmount">0.5</item>
</style>
*/

package com.yourpackage.neojmcomic.utils.share

import android.content.Context
import android.view.View
import android.widget.Button
import com.yourpackage.neojmcomic.utils.anim.AnimFade

/** 分享按钮工厂类（对应各平台ShareButton） */
object ShareButtonFactory {
    /** 创建Facebook分享按钮 */
    fun createFacebookButton(
        context: Context,
        data: ShareData,
        callback: ((Boolean, String?) -> Unit)? = null
    ): Button {
        return createShareButton(context, "Facebook", SharePlatform.FACEBOOK, data, callback)
    }

    /** 创建Twitter分享按钮 */
    fun createTwitterButton(
        context: Context,
        data: ShareData,
        callback: ((Boolean, String?) -> Unit)? = null
    ): Button {
        return createShareButton(context, "Twitter", SharePlatform.TWITTER, data, callback)
    }

    /** 创建微博分享按钮 */
    fun createWeiboButton(
        context: Context,
        data: ShareData,
        callback: ((Boolean, String?) -> Unit)? = null
    ): Button {
        return createShareButton(context, "Weibo", SharePlatform.WEIBO, data, callback)
    }

    /** 创建系统分享按钮 */
    fun createSystemShareButton(
        context: Context,
        data: ShareData,
        callback: ((Boolean, String?) -> Unit)? = null
    ): Button {
        return createShareButton(context, "Share", SharePlatform.SYSTEM, data, callback)
    }

    /** 基础分享按钮创建 */
    private fun createShareButton(
        context: Context,
        text: String,
        platform: SharePlatform,
        data: ShareData,
        callback: ((Boolean, String?) -> Unit)? = null
    ): Button {
        return Button(context).apply {
            this.text = text
            setOnClickListener {
                AnimFade.inView(this, duration = 200) // 添加入场动画
                ShareCore.share(context, ShareCore.ShareType.URL, platform, data, callback)
            }
            // 基础样式配置（可根据项目主题调整）
            setPadding(16.dpToPx(), 8.dpToPx(), 16.dpToPx(), 8.dpToPx())
            background = context.getDrawable(R.drawable.shape_share_button)
            setTextColor(context.getColor(android.R.color.white))
        }
    }

    /** DP转PX扩展函数 */
    private fun Int.dpToPx(): Int {
        return (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
    }
}

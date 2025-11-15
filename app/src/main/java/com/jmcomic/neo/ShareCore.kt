package com.yourpackage.neojmcomic.utils.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.yourpackage.neojmcomic.utils.tool.ToolDebounce
import java.net.URLEncoder

/** 分享核心工具（支持所有平台分享、复制链接） */
object ShareCore {
    private const val SHARE_DEBOUNCE_KEY = "share_action"
    private const val ENCODING = "UTF-8"

    /**
     * 执行分享
     * @param context 上下文
     * @param platform 分享平台
     * @param url 分享链接
     * @param title 分享标题
     * @param summary 分享摘要（仅部分平台支持）
     * @param imageUrl 分享图片（仅部分平台支持）
     * @param hashtags 话题标签（仅Twitter等支持）
     */
    fun share(
        context: Context,
        platform: SharePlatform,
        url: String,
        title: String = "",
        summary: String = "",
        imageUrl: String = "",
        hashtags: List<String> = emptyList()
    ) {
        ToolDebounce.debounce(SHARE_DEBOUNCE_KEY) {
            val encodedUrl = URLEncoder.encode(url, ENCODING)
            val encodedTitle = URLEncoder.encode(title, ENCODING)
            val encodedSummary = URLEncoder.encode(summary, ENCODING)
            val encodedImageUrl = URLEncoder.encode(imageUrl, ENCODING)
            val encodedHashtags = hashtags.joinToString(",").let { URLEncoder.encode(it, ENCODING) }

            when (platform) {
                SharePlatform.COPY_LINK -> copyToClipboard(context, url)
                SharePlatform.FACEBOOK -> launchShareUrl(context, ShareConfig.FACEBOOK_URL.format(encodedUrl, encodedTitle))
                SharePlatform.FACEBOOK_MESSENGER -> {
                    val shareUrl = ShareConfig.FACEBOOK_MESSENGER_URL.format(
                        encodedUrl,
                        ShareConfig.FACEBOOK_APP_ID,
                        URLEncoder.encode(ShareConfig.REDIRECT_URI, ENCODING)
                    )
                    launchShareUrl(context, shareUrl)
                }
                SharePlatform.WHATSAPP -> {
                    val text = if (title.isEmpty()) encodedUrl else "$encodedTitle ${encodedUrl}"
                    launchShareUrl(context, ShareConfig.WHATSAPP_URL.format(text))
                }
                SharePlatform.TELEGRAM -> {
                    launchShareUrl(context, ShareConfig.TELEGRAM_URL.format(encodedUrl, encodedTitle))
                }
                SharePlatform.EMAIL -> {
                    val body = if (summary.isEmpty()) encodedUrl else "$encodedSummary\n\n$encodedUrl"
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse(ShareConfig.EMAIL_URL.format(encodedTitle, body))
                    }
                    launchIntent(context, emailIntent, "选择邮箱应用")
                }
                SharePlatform.TWITTER -> {
                    val shareUrl = ShareConfig.TWITTER_URL.format(encodedUrl, encodedTitle, encodedHashtags)
                    launchShareUrl(context, shareUrl)
                }
                SharePlatform.WEIBO -> {
                    val shareUrl = ShareConfig.WEIBO_URL.format(encodedUrl, encodedTitle, encodedImageUrl)
                    launchShareUrl(context, shareUrl)
                }
                SharePlatform.LINE -> {
                    launchShareUrl(context, ShareConfig.LINE_URL.format(encodedUrl, encodedTitle))
                }
                SharePlatform.LINKEDIN -> {
                    val shareUrl = ShareConfig.LINKEDIN_URL.format(encodedUrl, encodedTitle, encodedSummary, encodedTitle)
                    launchShareUrl(context, shareUrl)
                }
                SharePlatform.PINTEREST -> {
                    if (imageUrl.isEmpty()) {
                        Toast.makeText(context, "Pinterest分享需传入图片链接", Toast.LENGTH_SHORT).show()
                        return@debounce
                    }
                    val shareUrl = ShareConfig.PINTEREST_URL.format(encodedUrl, encodedImageUrl, encodedTitle)
                    launchShareUrl(context, shareUrl)
                }
                SharePlatform.REDDIT -> {
                    launchShareUrl(context, ShareConfig.REDDIT_URL.format(encodedUrl, encodedTitle))
                }
            }
        }
    }

    /** 复制链接到剪贴板 */
    private fun copyToClipboard(context: Context, url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("分享链接", url)
        clipboard.setPrimaryClip(clipData)
        Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
    }

    /** 启动网页链接分享（跳转第三方平台网页版） */
    private fun launchShareUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        launchIntent(context, intent, "选择分享平台")
    }

    /** 启动Intent（适配无对应APP的情况） */
    private fun launchIntent(context: Context, intent: Intent, chooserTitle: String) {
        try {
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, "未找到支持该分享方式的应用", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.yourpackage.neojmcomic.utils.share

/** 分享平台枚举 */
enum class SharePlatform {
    FACEBOOK,
    FACEBOOK_MESSENGER,
    WHATSAPP,
    TELEGRAM,
    EMAIL,
    TWITTER,
    WEIBO,
    LINE,
    LINKEDIN,
    PINTEREST,
    REDDIT,
    COPY_LINK
}

/** 分享平台配置 */
object ShareConfig {
    // 各平台分享链接模板
    const val FACEBOOK_URL = "https://www.facebook.com/sharer/sharer.php?u=%s&title=%s"
    const val FACEBOOK_MESSENGER_URL = "https://www.facebook.com/dialog/send?link=%s&app_id=%s&redirect_uri=%s"
    const val WHATSAPP_URL = "https://api.whatsapp.com/send?text=%s"
    const val TELEGRAM_URL = "https://telegram.me/share/url?url=%s&text=%s"
    const val EMAIL_URL = "mailto:?subject=%s&body=%s"
    const val TWITTER_URL = "https://twitter.com/intent/tweet?url=%s&text=%s&hashtags=%s"
    const val WEIBO_URL = "http://service.weibo.com/share/share.php?url=%s&title=%s&pic=%s"
    const val LINE_URL = "https://social-plugins.line.me/lineit/share?url=%s&text=%s"
    const val LINKEDIN_URL = "https://linkedin.com/shareArticle?url=%s&title=%s&summary=%s&source=%s"
    const val PINTEREST_URL = "https://pinterest.com/pin/create/button/?url=%s&media=%s&description=%s"
    const val REDDIT_URL = "https://www.reddit.com/submit?url=%s&title=%s"

    // 默认APP ID（需替换为实际项目APP ID）
    const val FACEBOOK_APP_ID = "your_facebook_app_id"
    const val REDIRECT_URI = "https://your-app-host.com"
}

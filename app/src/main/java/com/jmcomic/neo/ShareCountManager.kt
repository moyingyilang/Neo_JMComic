package com.yourpackage.neojmcomic.utils.share

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** 分享计数管理（查询各平台分享次数） */
object ShareCountManager {
    private const val ENCODING = "UTF-8"

    /**
     * 查询分享次数
     * @param platform 分享平台
     * @param url 分享链接
     * @param callback 结果回调
     */
    fun getShareCount(
        context: Context,
        platform: SharePlatform,
        url: String,
        callback: (count: Int?) -> Unit
    ) {
        if (platform == SharePlatform.COPY_LINK || platform == SharePlatform.EMAIL) {
            callback(null) // 无分享计数接口
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val encodedUrl = URLEncoder.encode(url, ENCODING)
            val count = try {
                when (platform) {
                    SharePlatform.FACEBOOK -> getFacebookShareCount(encodedUrl)
                    SharePlatform.PINTEREST -> getPinterestShareCount(encodedUrl)
                    SharePlatform.REDDIT -> getRedditShareCount(encodedUrl)
                    SharePlatform.VK -> getVkShareCount(encodedUrl)
                    SharePlatform.TUMBLR -> getTumblrShareCount(encodedUrl)
                    SharePlatform.OK -> getOkShareCount(encodedUrl)
                    SharePlatform.HATENA -> getHatenaShareCount(encodedUrl)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
            withContext(Dispatchers.Main) {
                callback(count)
            }
        }
    }

    /** Facebook分享计数 */
    private fun getFacebookShareCount(encodedUrl: String): Int? {
        val url = "https://graph.facebook.com/?id=$encodedUrl&fields=og_object{engagement}"
        val response = getHttpResponse(url)
        val engagementMatch = Regex("\"engagement\":\\{\"count\":(\\d+)").find(response)
        return engagementMatch?.groupValues?.get(1)?.toInt()
    }

    /** Pinterest分享计数 */
    private fun getPinterestShareCount(encodedUrl: String): Int? {
        val url = "https://api.pinterest.com/v1/urls/count.json?url=$encodedUrl"
        val response = getHttpResponse(url)
        val countMatch = Regex("\"count\":(\\d+)").find(response)
        return countMatch?.groupValues?.get(1)?.toInt()
    }

    /** Reddit分享计数 */
    private fun getRedditShareCount(encodedUrl: String): Int? {
        val url = "https://www.reddit.com/api/info.json?limit=1&url=$encodedUrl"
        val response = getHttpResponse(url)
        val scoreMatch = Regex("\"score\":(\\d+)").find(response)
        return scoreMatch?.groupValues?.get(1)?.toInt()
    }

    /** VK分享计数 */
    private fun getVkShareCount(encodedUrl: String): Int? {
        val url = "https://vk.com/share.php?act=count&index=0&url=$encodedUrl"
        val response = getHttpResponse(url)
        val countMatch = Regex("VK\\.Share\\.count\\(0,(\\d+)\\)").find(response)
        return countMatch?.groupValues?.get(1)?.toInt()
    }

    /** Tumblr分享计数 */
    private fun getTumblrShareCount(encodedUrl: String): Int? {
        val url = "https://api.tumblr.com/v2/share/stats?url=$encodedUrl"
        val response = getHttpResponse(url)
        val noteCountMatch = Regex("\"note_count\":(\\d+)").find(response)
        return noteCountMatch?.groupValues?.get(1)?.toInt()
    }

    /** OK分享计数 */
    private fun getOkShareCount(encodedUrl: String): Int? {
        val url = "https://connect.ok.ru/dk?st.cmd=extLike&uid=react-share-0&ref=$encodedUrl"
        val response = getHttpResponse(url)
        val countMatch = Regex("ODKL\\.updateCount\\(\"react-share-0\",\"(\\d+)\"\\)").find(response)
        return countMatch?.groupValues?.get(1)?.toInt()
    }

    /** Hatena分享计数 */
    private fun getHatenaShareCount(encodedUrl: String): Int? {
        val url = "https://bookmark.hatenaapis.com/count/entry?url=$encodedUrl"
        val response = getHttpResponse(url)
        return if (response.isNotEmpty() && response.all { it.isDigit() }) response.toInt() else null
    }

    /** 发送HTTP请求获取响应 */
    private fun getHttpResponse(urlStr: String): String {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val inputStream = connection.inputStream
            inputStream.bufferedReader().readText()
        } finally {
            connection.disconnect()
        }
    }
}

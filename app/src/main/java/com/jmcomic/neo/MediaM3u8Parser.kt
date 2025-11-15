package com.yourpackage.neojmcomic.utils.media

import android.net.Uri
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** M3U8播放列表解析器 */
class MediaM3u8Parser {
    private val TAG_EXT_M3U = "#EXTM3U"
    private val TAG_EXT_X_STREAM_INF = "#EXT-X-STREAM-INF:"
    private val TAG_EXT_X_TARGETDURATION = "#EXT-X-TARGETDURATION:"
    private val TAG_EXTINF = "#EXTINF:"

    /** 解析M3U8主播放列表（多码率） */
    fun parseMasterPlaylist(url: String): List<QualityItem> {
        val qualityList = mutableListOf<QualityItem>()
        var currentBandwidth = 0
        var currentResolution = ""

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.trim()?.let {
                        when {
                            it.startsWith(TAG_EXT_X_STREAM_INF) -> {
                                // 解析带宽和分辨率
                                val bandwidthMatch = Regex("BANDWIDTH=(\\d+)").find(it)
                                currentBandwidth = bandwidthMatch?.groupValues?.get(1)?.toInt() ?: 0
                                val resolutionMatch = Regex("RESOLUTION=(\\d+x\\d+)").find(it)
                                currentResolution = resolutionMatch?.groupValues?.get(1) ?: "unknown"
                            }
                            it.startsWith("http") -> {
                                // 解析子播放列表URL
                                qualityList.add(
                                    QualityItem(
                                        url = it,
                                        bandwidth = currentBandwidth,
                                        resolution = currentResolution
                                    )
                                )
                            }
                        }
                    }
                }
            }
            connection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return qualityList
    }

    /** 清晰度项模型 */
    data class QualityItem(
        val url: String,
        val bandwidth: Int,
        val resolution: String
    )
}

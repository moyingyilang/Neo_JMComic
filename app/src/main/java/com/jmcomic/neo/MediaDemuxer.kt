package com.yourpackage.neojmcomic.utils.media

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.FileDescriptor
import java.io.IOException

/** 音视频解复用器（适配TS/MP4/AAC格式） */
class MediaDemuxer {
    private var extractor: MediaExtractor? = null
    private val trackFormats = mutableMapOf<Int, MediaFormat>() // trackId -> MediaFormat
    private val trackTypes = mutableMapOf<Int, String>() // trackId -> 类型（audio/video）

    /** 初始化解复用器 */
    fun init(fd: FileDescriptor, startOffset: Long = 0, length: Long = -1): Boolean {
        try {
            extractor = MediaExtractor().apply {
                setDataSource(fd, startOffset, length)
            }
            // 遍历所有轨道，记录格式和类型
            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                trackFormats[i] = format
                trackTypes[i] = when {
                    mime.startsWith("audio/") -> "audio"
                    mime.startsWith("video/") -> "video"
                    else -> continue
                }
            }
            return trackFormats.isNotEmpty()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /** 获取音频轨道ID */
    fun getAudioTrackId(): Int? = trackTypes.entries.find { it.value == "audio" }?.key

    /** 获取视频轨道ID */
    fun getVideoTrackId(): Int? = trackTypes.entries.find { it.value == "video" }?.key

    /** 选择轨道 */
    fun selectTrack(trackId: Int) = extractor?.selectTrack(trackId)

    /** 读取样本数据 */
    fun readSampleData(buffer: ByteArray): Int {
        return extractor?.readSampleData(buffer, 0) ?: -1
    }

    /** 前进到下一个样本 */
    fun advance(): Boolean = extractor?.advance() ?: false

    /** 获取当前样本时间戳（微秒） */
    fun getSampleTime(): Long = extractor?.sampleTime ?: -1

    /** 释放资源 */
    fun release() {
        extractor?.release()
        extractor = null
        trackFormats.clear()
        trackTypes.clear()
    }
}

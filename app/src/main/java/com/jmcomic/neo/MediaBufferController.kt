package com.yourpackage.neojmcomic.utils.media

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.upstream.BandwidthMeter

/** 缓冲控制器（管理缓冲状态、清理、溢出处理） */
class MediaBufferController(
    private val player: ExoPlayer,
    private val bandwidthMeter: BandwidthMeter,
    private val bufferListener: BufferListener? = null
) : Player.Listener {
    private var isBuffering = false
    private var bufferUnderflowCount = 0
    private val MIN_BUFFER_DURATION_MS = 3000L // 最小缓冲时长（3秒）
    private val MAX_BUFFER_DURATION_MS = 30000L // 最大缓冲时长（30秒）

    init {
        player.addListener(this)
    }

    /** 清理指定时间范围的缓冲 */
    fun clearBuffer(startMs: Long, endMs: Long) {
        player.clearVideoBuffer(startMs, endMs)
        player.clearAudioBuffer(startMs, endMs)
        bufferListener?.onBufferCleared(startMs, endMs)
    }

    /** 清理所有缓冲 */
    fun clearAllBuffer() {
        player.clearVideoBuffer(0, player.duration)
        player.clearAudioBuffer(0, player.duration)
        bufferListener?.onBufferCleared(0, player.duration)
    }

    /** 处理缓冲不足（触发降级或重试） */
    private fun handleBufferUnderflow() {
        bufferUnderflowCount++
        bufferListener?.onBufferUnderflow(bufferUnderflowCount)
        // 缓冲不足超过3次，触发ABR降级
        if (bufferUnderflowCount >= 3) {
            bufferListener?.onNeedQualityDowngrade()
            bufferUnderflowCount = 0
        }
    }

    /** 监听缓冲状态变化 */
    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                isBuffering = true
                bufferListener?.onBufferingStart()
            }
            Player.STATE_READY -> {
                isBuffering = false
                val bufferedDuration = player.bufferedPosition - player.currentPosition
                bufferListener?.onBufferingEnd(bufferedDuration)
                // 缓冲时长超过最大限制，清理部分缓冲
                if (bufferedDuration > MAX_BUFFER_DURATION_MS) {
                    val clearStartMs = player.currentPosition + MIN_BUFFER_DURATION_MS
                    clearBuffer(clearStartMs, player.bufferedPosition)
                }
            }
            Player.STATE_ENDED -> {
                bufferListener?.onBufferEOS()
            }
        }
    }

    /** 监听播放错误（处理缓冲相关错误） */
    override fun onPlayerError(error: Player.ExoPlaybackException) {
        when (error.type) {
            Player.ExoPlaybackException.TYPE_SOURCE -> {
                if (error.cause is BehindLiveWindowException) {
                    // 直播窗口过期，清理缓冲并重试
                    clearAllBuffer()
                    player.seekToDefaultPosition()
                    player.prepare()
                    bufferListener?.onBufferLiveExpired()
                } else {
                    bufferListener?.onBufferError(error.message ?: "Source buffer error")
                }
            }
            Player.ExoPlaybackException.TYPE_RENDERER -> {
                bufferListener?.onBufferError(error.message ?: "Renderer buffer error")
            }
        }
    }

    /** 监听缓冲更新 */
    override fun onBufferedPositionChanged(bufferedPosition: Long) {
        val currentPosition = player.currentPosition
        val bufferedDuration = bufferedPosition - currentPosition
        bufferListener?.onBufferUpdated(bufferedDuration)
        // 缓冲不足触发回调
        if (bufferedDuration < MIN_BUFFER_DURATION_MS && player.playWhenReady) {
            handleBufferUnderflow()
        }
    }

    /** 释放资源 */
    fun release() {
        player.removeListener(this)
    }

    /** 缓冲状态回调接口 */
    interface BufferListener {
        fun onBufferingStart() {}
        fun onBufferingEnd(bufferedDurationMs: Long) {}
        fun onBufferUpdated(bufferedDurationMs: Long) {}
        fun onBufferCleared(startMs: Long, endMs: Long) {}
        fun onBufferUnderflow(count: Int) {}
        fun onNeedQualityDowngrade() {}
        fun onBufferEOS() {}
        fun onBufferLiveExpired() {}
        fun onBufferError(message: String) {}
    }
}

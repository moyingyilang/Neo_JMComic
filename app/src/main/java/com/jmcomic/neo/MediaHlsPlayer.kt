package com.yourpackage.neojmcomic.utils.media

import android.content.Context
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

/** HLS核心播放器（适配ExoPlayer，支持M3U8/MP4） */
class MediaHlsPlayer(context: Context) {
    private val exoPlayer: ExoPlayer
    private var surfaceView: SurfaceView? = null

    init {
        // 初始化ExoPlayer
        exoPlayer = ExoPlayer.Builder(context).apply {
            val dataSourceFactory = DefaultHttpDataSourceFactory(
                "NeoJMComic",
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
            )
            setMediaSourceFactory(
                DefaultMediaSourceFactory(dataSourceFactory)
                    .setHlsMediaSourceFactory(HlsMediaSource.Factory(dataSourceFactory(H
            )
        }.build()
    }

    /** 设置播放容器 */
    fun setSurfaceView(surfaceView: SurfaceView) {
        this.surfaceView = surfaceView
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                exoPlayer.setVideoSurface(holder.surface)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                exoPlayer.setVideoSurface(null)
            }
        })
    }

    /** 设置播放源（支持M3U8/MP4） */
    fun setSource(url: String) {
        val uri = Uri.parse(url)
        val mediaItem = MediaItem.fromUri(uri)
        val mediaSource = if (url.contains("m3u8")) {
            HlsMediaSource.Factory(exoPlayer.mediaSourceFactory.dataSourceFactory)
                .createMediaSource(mediaItem)
        } else {
            exoPlayer.mediaSourceFactory.createMediaSource(mediaItem)
        }
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    /** 播放控制 */
    fun play() = exoPlayer.play()
    fun pause() = exoPlayer.pause()
    fun seekTo(positionMs: Long) = exoPlayer.seekTo(positionMs)
    fun stop() = exoPlayer.stop()
    fun release() = exoPlayer.release()

    /** 获取播放状态 */
    val isPlaying: Boolean get() = exoPlayer.playWhenReady
    val currentPosition: Long get() = exoPlayer.currentPosition
    val duration: Long get() = exoPlayer.duration
}

package com.yourpackage.neojmcomic.utils.media

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.upstream.BandwidthMeter

/** 自适应码率控制器（根据带宽切换清晰度） */
class MediaAbrController(
    private val player: ExoPlayer,
    private val trackSelector: DefaultTrackSelector,
    private val bandwidthMeter: BandwidthMeter
) {
    private var currentQualityIndex = 0 // 当前清晰度索引（0=最低，递增）

    /** 切换清晰度 */
    fun switchQuality(qualityIndex: Int) {
        val videoTrackGroupArray = trackSelector.currentMappedTrackInfo?.videoTrackGroups
        videoTrackGroupArray?.let { trackGroups ->
            for (i in 0 until trackGroups.length) {
                val trackGroup = trackGroups.get(i)
                if (qualityIndex >= 0 && qualityIndex < trackGroup.length) {
                    val override = TrackSelectionOverride(trackGroup, qualityIndex)
                    trackSelector.setSelectionOverride(0, trackGroup, override)
                    currentQualityIndex = qualityIndex
                }
            }
        }
    }

    /** 自动根据带宽调整清晰度 */
    fun autoAdjustQuality() {
        val bandwidth = bandwidthMeter.bitrateEstimate // 当前带宽估算值（bps）
        val videoTrackGroupArray = trackSelector.currentMappedTrackInfo?.videoTrackGroups
        videoTrackGroupArray?.let { trackGroups ->
            for (i in 0 until trackGroups.length) {
                val trackGroup = trackGroups.get(i)
                for (j in trackGroup.length - 1 downTo 0) {
                    val format = trackGroup.getFormat(j)
                    val bitrate = format.bitrate ?: 0
                    if (bandwidth >= bitrate * 0.8) { // 带宽足够当前码率的80%
                        switchQuality(j)
                        break
                    }
                }
            }
        }
    }

    /** 获取当前清晰度索引 */
    fun getCurrentQualityIndex() = currentQualityIndex
}

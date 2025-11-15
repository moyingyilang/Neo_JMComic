package com.yourpackage.neojmcomic.utils.media

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride

/** 音视频+字幕轨道控制器 */
class MediaTrackController(
    private val player: ExoPlayer,
    private val trackSelector: DefaultTrackSelector
) {
    private val audioTracks = mutableListOf<TrackInfo>() // 音频轨道列表
    private val subtitleTracks = mutableListOf<TrackInfo>() // 字幕轨道列表

    /** 初始化轨道信息 */
    fun initTracks() {
        val tracks = player.currentTracks
        tracks.groups.forEachIndexed { groupIndex, group ->
            for (trackIndex in 0 until group.length) {
                val trackFormat = group.getTrackFormat(trackIndex)
                when (group.type) {
                    Tracks.Group.TYPE_AUDIO -> {
                        audioTracks.add(
                            TrackInfo(
                                trackId = "${groupIndex}_$trackIndex",
                                name = trackFormat.label ?: "Audio Track $trackIndex",
                                language = trackFormat.language ?: "unknown",
                                groupIndex = groupIndex,
                                trackIndex = trackIndex
                            )
                        )
                    }
                    Tracks.Group.TYPE_TEXT -> {
                        subtitleTracks.add(
                            TrackInfo(
                                trackId = "${groupIndex}_$trackIndex",
                                name = trackFormat.label ?: "Subtitle Track $trackIndex",
                                language = trackFormat.language ?: "unknown",
                                groupIndex = groupIndex,
                                trackIndex = trackIndex
                            )
                        )
                    }
                }
            }
        }
    }

    /** 切换音频轨道 */
    fun switchAudioTrack(trackId: String) {
        val trackInfo = audioTracks.find { it.trackId == trackId } ?: return
        val override = TrackSelectionOverride(
            player.currentTracks.groups[trackInfo.groupIndex],
            trackInfo.trackIndex
        )
        trackSelector.setSelectionOverride(0, player.currentTracks.groups[trackInfo.groupIndex], override)
    }

    /** 切换字幕轨道（null=关闭字幕） */
    fun switchSubtitleTrack(trackId: String?) {
        val trackInfo = trackId?.let { subtitleTracks.find { it.trackId == trackId } }
        if (trackInfo != null) {
            val override = TrackSelectionOverride(
                player.currentTracks.groups[trackInfo.groupIndex],
                trackInfo.trackIndex
            )
            trackSelector.setSelectionOverride(0, player.currentTracks.groups[trackInfo.groupIndex], override)
        } else {
            // 关闭字幕
            player.currentTracks.groups.forEach { group ->
                if (group.type == Tracks.Group.TYPE_TEXT) {
                    trackSelector.clearSelectionOverrides(0, group)
                }
            }
        }
    }

    /** 获取音频轨道列表 */
    fun getAudioTracks() = audioTracks.toList()

    /** 获取字幕轨道列表 */
    fun getSubtitleTracks() = subtitleTracks.toList()

    /** 轨道信息模型 */
    data class TrackInfo(
        val trackId: String,
        val name: String,
        val language: String,
        val groupIndex: Int,
        val trackIndex: Int
    )
}

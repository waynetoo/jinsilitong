package com.waynetoo.videotv

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    private val TAG = "waynetoo"
    private var currentUrl = ""
    private lateinit var simpleExoPlayer: SimpleExoPlayer

    companion object {
        const val STREAM_URL = "http://vimg.zijinshan.org/portal/news/video/1554103229199.mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializePlayer()
        setSource(STREAM_URL)
    }

    override fun onResume() {
        super.onResume()
        start()
    }

    private fun initializePlayer() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        simpleExoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                Log.d(TAG, "onPlaybackParametersChanged: ")
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
                Log.d(TAG, "onTracksChanged: ")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Log.d(TAG, "onPlayerError: ")
            }

            /** 4 playbackState exists */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_BUFFERING")
                        toast("onPlayerStateChanged - STATE_BUFFERING")
                    }
                    Player.STATE_READY -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_READY")
                        toast("onPlayerStateChanged - STATE_READY")
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_IDLE")
                        toast("onPlayerStateChanged - STATE_IDLE")
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_ENDED")
                        toast("onPlayerStateChanged - STATE_ENDED")
                    }
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.d(TAG, "onLoadingChanged: ")
            }

            override fun onPositionDiscontinuity(reason: Int) {
                Log.d(TAG, "onPositionDiscontinuity: ")
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                Log.d(TAG, "onRepeatModeChanged: ")
                Toast.makeText(baseContext, "repeat mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                Log.d(TAG, "onTimelineChanged: ")
            }
        })
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = simpleExoPlayer
        playerView.requestFocus()
    }

    /**
     * 设置播放地址
     */
    fun setSource(url: String = "") {
        currentUrl = url
        try {
            simpleExoPlayer.prepare(buildMediaSource(url))
        } catch (e: IllegalStateException) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildMediaSource(url: String): MediaSource {
        val uri = Uri.parse(url)
        val dataSourceFactory = DefaultDataSourceFactory(this,Util.getUserAgent(this, "mediaPlayerSample"))
        println("type ="+Util.inferContentType(uri))
        return when (val type = Util.inferContentType(uri)) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
            else -> throw IllegalStateException("媒体类型不支持: $type")
        }.createMediaSource(uri)
    }

    /**
     * 播放
     */
    fun start() {
        simpleExoPlayer.playWhenReady = true
    }
    /**
     * 暂停
     */
    fun pause() {
        simpleExoPlayer.playWhenReady = false
    }
    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.release()
    }

}

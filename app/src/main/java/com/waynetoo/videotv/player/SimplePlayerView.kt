package com.waynetoo.videotv.player

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.waynetoo.lib_common.extentions.toast
import com.waynetoo.videotv.utils.Logger


/**
 * Created by Howshea
 * on 2018/11/21
 */
open class SimplePlayerView : PlayerView, IPlayer {
    private val TAG_VIDEO = "PlayerView"

    //手动暂停标记
    private var pausedManually = false
    var isPlaying = false
        get() = player?.playWhenReady ?: false
    var onStop: (() -> Unit)? = null
    open var onShow: (() -> Unit)? = null
    open var onHide: (() -> Unit)? = null
    var onVideoStateReady: (() -> Unit)? = null
    var playerCallback: ((playWhenReady: Boolean, state: Int) -> Unit)? = null
    var positionCallback: ((currentPos: Long, duration: Long, isPlaying: Boolean) -> Unit)? = null
    var currentPosition = 0L
        get() = if (player?.currentPosition == null) 0L else player?.currentPosition!!

    private var currentUrl = ""
    private lateinit var simpleExoPlayer: SimpleExoPlayer

    init {
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onResume(owner: LifecycleOwner) {
        //非手动暂停的情况才恢复播放
        if (!pausedManually) {
            start()
        }
        pausedManually = false
    }

    override fun onPause(owner: LifecycleOwner) {
        if (!isPlaying) {
            pausedManually = true
        }
        pause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        player?.release()
    }

    private fun buildMediaSource(url: String): MediaSource {
        val uri = Uri.parse(url)
        val dataSourceFactory =
            FileDataSourceFactory()
        Logger.log("Util.inferContentType(uri) =" + Util.inferContentType(uri))
        return when (val type = Util.inferContentType(uri)) {
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
            C.TYPE_SS -> SsMediaSource.Factory(dataSourceFactory)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory)
            else -> throw IllegalStateException("媒体类型不支持: $type")
        }.createMediaSource(uri)
    }

    open fun initPlayer() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context).apply {
            playWhenReady = true
        }
        setShutterBackgroundColor(Color.TRANSPARENT)
        player = simpleExoPlayer
        requestFocus()
        simpleExoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG_VIDEO, "onPlayerStateChanged - STATE_BUFFERING")
                    }
                    Player.STATE_READY -> {
                        Log.d(TAG_VIDEO, "onPlayerStateChanged - STATE_READY")
                        // 暂停/播放/拖拽进度条，都会走到这里
                        onVideoStateReady?.invoke()
                        positionCallback?.invoke(
                            player.currentPosition,
                            player.duration,
                            player.playWhenReady
                        )
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG_VIDEO, "onPlayerStateChanged - STATE_IDLE")
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG_VIDEO, "onPlayerStateChanged - STATE_ENDED")
                        onStop?.invoke()
                    }
                }
                playerCallback?.invoke(playWhenReady, playbackState)
//                println("---- playbackState" + playbackState + " playWhenReady:" + player.playWhenReady)
            }
        })
    }

    /**
     * 设置播放地址
     */
    fun setSource(url: String = "") {
        currentUrl = url
        try {
            simpleExoPlayer.prepare(buildMediaSource(url))
        } catch (e: IllegalStateException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 播放
     */
    fun start() {
        player?.playWhenReady = true
    }

    fun restart() {
        player?.seekTo(0)
        //当视频的状态变为STATE_ENDED时，播放键变成暂停状态，但是player的playWhenReady并不会发生变化，就是之前是false就还是false，之前是true还是true
        start()
    }

    /**
     * 暂停
     */
    fun pause() {
        player?.playWhenReady = false
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    open fun stopAndRelease() {
        pause()
        player?.release()
    }

    fun getPlayerStatus(): Int {
        return player?.playbackState ?: 0
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            Logger.log(" isReady :" + player?.playWhenReady)
        }
    }
}

package com.example.myApplication

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.myApplication.player.EventLogger
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.custom_controls.*
import kotlinx.android.synthetic.main.player_activity.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Created by Nagendra.P on 1/25/2018.
 */
class PlayerActivity : AppCompatActivity() {


    private var simpleExoPlayerView: SimpleExoPlayerView? = null
    private var mainHandler: Handler? = null
    private var bandwidthMeter: BandwidthMeter? = null
    private var videoTrackSelectionFactory: TrackSelection.Factory? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var context: Context? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var videoSource: MediaSource? = null
    private var eventLogger: EventLogger? = null
    private var player: SimpleExoPlayer? = null
    private var displayMetrics: DisplayMetrics? = null
    private var height: Int? = 0

    private var normalLayoutParams: LinearLayout.LayoutParams? = null
    private var fullScreenLayoutParams: LinearLayout.LayoutParams? = null
    private var width: Int? = 0
    private var playerHeight: Double? = 0.00
    private var requiredHeight: Int? = 35
    private var calculatedHeight: Double? = 0.00
    private var playingUrl: String? = ""
    private var fullScreenImageView: ImageView? = null


    override fun onDestroy() {
        super.onDestroy()
        player!!.release()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setContentView(R.layout.player_activity)
        simpleExoPlayerView = findViewById(R.id.simple_exoPlayer_view)
        fullScreenImageView = findViewById(R.id.exo_full)


        displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        height = displayMetrics!!.heightPixels
        width = displayMetrics!!.widthPixels

        calculatedHeight = requiredHeight!!.div(100.00)

        playerHeight = height!!.times(calculatedHeight!!)

        normalLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, playerHeight!!.toInt())
        fullScreenLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)


        verticalScreenPlayer()

        playingUrl = intent.getStringExtra("playingUrl")


        fullScreenImageView!!.setOnClickListener({
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                fullScreenImageView!!.setImageResource(R.mipmap.u_open)
                verticalScreenPlayer()
            } else {
                fullScreenImageView!!.setImageResource(R.mipmap.u_close)
                horizontalScreenPlayer()
            }
        })


        backOnPlayer.setOnClickListener( {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                verticalScreenPlayer()
            } else {
                finish()
            }
        })

        createPlayer()
    }


    override fun onBackPressed() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            verticalScreenPlayer()
        } else {
            finish()
        }
    }


    private fun horizontalScreenPlayer() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        frameLayout.layoutParams = fullScreenLayoutParams
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun verticalScreenPlayer() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        frameLayout.layoutParams = normalLayoutParams
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    private fun createPlayer() {
        mainHandler = Handler()
        bandwidthMeter = DefaultBandwidthMeter()
        videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        eventLogger = EventLogger(trackSelector!!)

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)
        simpleExoPlayerView!!.player = (player)

        val bandwidthMeter = DefaultBandwidthMeter()
        dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, getString(R.string.app_name)), bandwidthMeter)

        val myUri = Uri.parse(playingUrl)
        videoSource = buildMediaSource(myUri, getExtension(playingUrl!!), mainHandler, eventLogger)
        player!!.prepare(videoSource)
        player!!.playWhenReady = (true)



        player!!.addListener(object : Player.EventListener {


            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {

            }

            override fun onLoadingChanged(isLoading: Boolean) {

            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        player!!.seekTo(0)
                        player!!.playWhenReady = (true)
                    }
                    Player.STATE_BUFFERING -> {
                        progress.visibility = View.VISIBLE
                        playerText.visibility = View.GONE
                    }
                    Player.STATE_IDLE -> {
                        progress.visibility = View.GONE
                        playerText.visibility = View.VISIBLE
                    }
                    else -> {
                        progress.visibility = View.GONE
                        playerText.visibility = View.GONE
                    }
                }

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }

            override fun onPlayerError(error: ExoPlaybackException) {

            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {

            }

            override fun onSeekProcessed() {

            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {

            }
        })
    }


    private fun buildMediaSource(uri: Uri, overrideExtension: String, handler: Handler?, listener: MediaSourceEventListener?): MediaSource {
        @C.ContentType val type = if (TextUtils.isEmpty(overrideExtension)) Util.inferContentType(uri)
        else Util.inferContentType("." + overrideExtension)
        return when (type) {

            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri, handler, listener)
            C.TYPE_OTHER -> ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri, handler, listener)
            else -> {
                throw IllegalStateException("Unsupported type: " + type)
            }
        }
    }

    private fun getExtension(fileName: String): String {
        val encoded: String
        encoded = try {
            URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        } catch (e: UnsupportedEncodingException) {
            fileName
        }

        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase()
    }
}
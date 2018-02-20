package com.example.myApplication

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.myApplication.model.MovieDetailsData
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

    private var normalLayoutParams: ConstraintLayout.LayoutParams? = null
    private var fullScreenLayoutParams: ConstraintLayout.LayoutParams? = null

    private var normalPlayerLayoutParams: RelativeLayout.LayoutParams? = null
    private var fullScreenPlayerLayoutParams: RelativeLayout.LayoutParams? = null

    private var width: Int? = 0
    private var playerHeight: Double? = 0.00
    private var requiredHeight: Int? = 38
    private var calculatedHeight: Double? = 0.00
    private var playingUrl: String? = ""
    private var fullScreenImageView: ImageView? = null
    private var movieDetailsData: MovieDetailsData? = null


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

        movieDetailsData = intent.extras.getParcelable("selectedObject")

        normalLayoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, playerHeight!!.toInt())
        fullScreenLayoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

        normalPlayerLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight!!.toInt())
        fullScreenPlayerLayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)


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


        backOnPlayer.setOnClickListener({
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                verticalScreenPlayer()
            } else {
                finish()
            }
        })

        createPlayer()
        displaySynopsisData(movieDetailsData!!)
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
        simpleExoPlayerView!!.layoutParams=fullScreenPlayerLayoutParams
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun verticalScreenPlayer() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        frameLayout.layoutParams = normalLayoutParams
        simpleExoPlayerView!!.layoutParams=normalPlayerLayoutParams
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
                        progress.visibility = VISIBLE
                        playerText.visibility = GONE
                    }
                    Player.STATE_IDLE -> {
                        progress.visibility = GONE
                        playerText.visibility = VISIBLE
                    }
                    else -> {
                        progress.visibility = GONE
                        playerText.visibility = GONE
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


    private fun displaySynopsisData(movieDetailsData: MovieDetailsData) {
        movieName.text = movieDetailsData.name
        viewCount.text = movieDetailsData.viewCount


        if (movieDetailsData.castCrew == "" && movieDetailsData.director == "" && movieDetailsData.writtenBy == "" && movieDetailsData.synopsis == "") {
            noSynopsisInfo.visibility = VISIBLE
            movieCastCrew.visibility = GONE
            movieWriters.visibility = GONE
            movieDirector.visibility = GONE
            synopsisData.visibility = GONE

        } else {
            noSynopsisInfo.visibility = GONE
            if (movieDetailsData.castCrew != "") {
                val castCrew: String = formatTextView(getString(R.string.castCrew), movieDetailsData.castCrew)
                movieCastCrew.text = castCrew
                movieCastCrew.visibility = VISIBLE
            } else {
                movieCastCrew.visibility = GONE
            }

            if (movieDetailsData.writtenBy != "") {
                val writtenBy: String = formatTextView(getString(R.string.writtenBy) , movieDetailsData.writtenBy)
                movieWriters.text = writtenBy
                movieWriters.visibility = VISIBLE
            } else {
                movieWriters.visibility = GONE
            }


            if (movieDetailsData.director != "") {
                val directedBy: String = getString(R.string.directedBy) + movieDetailsData.director
                movieDirector.text = directedBy
                movieDirector.visibility = VISIBLE
            } else {
                movieDirector.visibility = GONE
            }
            if (movieDetailsData.synopsis != "") {
                movieSynopsisText.text = movieDetailsData.synopsis
                movieSynopsisText.visibility = VISIBLE
            } else {
                movieSynopsisText.visibility = GONE
            }
        }


        synopsisViewLayout.setOnClickListener {
            if (!synopsisVisibility!!) {
                synopsisVisibility = true
                imageViewMoreData.setImageResource(R.drawable.up)
                synopsisData.visibility = VISIBLE
            } else {
                synopsisVisibility = false
                imageViewMoreData.setImageResource(R.drawable.down)
                synopsisData.visibility = GONE
            }
        }


    }


    private fun formatTextView(firstString: String, secondString: String): String {

        val builder: SpannableStringBuilder? = SpannableStringBuilder()

        val str1: SpannableString? = SpannableString(firstString)
        str1!!.setSpan(ForegroundColorSpan(Color.RED), 0, firstString.length, 0)
        builder!!.append(str1)

        val str2: SpannableString? = SpannableString(secondString)
        str2!!.setSpan(ForegroundColorSpan(Color.WHITE), 0, secondString.length, 0)
        builder.append(str2)

        return builder.toString()
    }


    private var synopsisVisibility: Boolean? = true


}
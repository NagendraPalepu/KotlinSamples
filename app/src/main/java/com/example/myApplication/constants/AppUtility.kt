package com.example.myApplication.constants

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import com.example.myApplication.R
import com.example.myApplication.model.MovieDetailsData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import org.json.JSONObject

/**
 * Created by Nagendra.P on 1/10/2018.
 */
class AppUtility {

    companion object {
        const val carouselImageUrl = "http://13.250.51.20/uprtc/rest/50001/homePageCarouselContentWithOffer"
        const val homeContentUrl = "http://13.250.51.20/uprtc/rest/50001/homePageContent?deviceType=OTHER_DEVICE"
        const val totalContentUrl = "http://13.250.51.20/uprtc/rest/50001/vod/movies"


        const val get = "GET"
        const val yes = true
        const val no = false

        //HomePage Calls
        const val carousels = "carousels"
        const val homePageContent = "homePageContent"
        const val contentTypeMovie = "MOVIE"
        const val contentTypeAD = "AD"
        const val contentTypeVideo = "VIDEO"


        const val moreMovies="Movies"
        const val moreVideos="Videos"

        fun getDeviceDimension(context: Context?): Point {

            if (context == null) {
                return Point()
            }
            val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            val display = wm.defaultDisplay

            val appSize = Point()
            display.getSize(appSize)

            return appSize
        }

        fun getHeightOfIcon(widthOfIcon: Int): Int {
            return widthOfIcon * 30 / 100
        }


        fun rectAngularBannerView(context: Context, adId: String): AdView {

            val mAdViewBigTitleBanner = AdView(context)
            mAdViewBigTitleBanner.adSize = AdSize.MEDIUM_RECTANGLE
            mAdViewBigTitleBanner.adUnitId = adId
            val adRequest = AdRequest.Builder().build()
            mAdViewBigTitleBanner.loadAd(adRequest)
            return mAdViewBigTitleBanner
        }

        fun largeBannerView(context: Context, adId: String): AdView {

            val mAdViewBigTitleBanner = AdView(context)
            mAdViewBigTitleBanner.adSize = AdSize.LARGE_BANNER
            mAdViewBigTitleBanner.adUnitId = adId
            val adRequest = AdRequest.Builder().build()
            mAdViewBigTitleBanner.loadAd(adRequest)

            return mAdViewBigTitleBanner
        }


        fun getVideoDimension(activity: Context): Point {
            val point = getDeviceDimension(activity)

            val imgWidth = (point.x / 2.75).toInt()
            val imgHeight = (imgWidth * 0.5625).toInt()
            point.x = imgWidth
            point.y = imgHeight

            return point
        }

        fun getGridMovieDimension(activity: Context): Point {
            val point = getDeviceDimension(activity)

            val imgWidth = point.x / 2
            val imgHeight = (imgWidth * 1.3).toInt()
            point.x = imgWidth
            point.y = imgHeight

            return point
        }


        fun getGridVideoDimension(activity: Context): Point {
            val point = getDeviceDimension(activity)

            val imgWidth = (point.x / 2)
            val imgHeight = (imgWidth * 1)
            point.x = imgWidth
            point.y = imgHeight

            return point
        }

        fun getMovieDimension(activity: Context): Point {
            val point = getDeviceDimension(activity)

            val imgWidth = point.x / 3
            val imgHeight = (imgWidth * 1.5).toInt()
            point.x = imgWidth
            point.y = imgHeight

            return point
        }


        fun parseJson(contentString: String, context: Context, contentType: String): ArrayList<MovieDetailsData> {
            val homeContentList: ArrayList<MovieDetailsData> = arrayListOf()
            homeContentList.clear()

            if (contentString != "") {

                val js = JSONObject(contentString)
                val responseCode = js.optJSONObject(context.getString(R.string.responseStatus)).optString(context.getString(R.string.statusCode))
                if (responseCode == ("200")) {
                    val jsonDetailsObject = js.optJSONArray("movie")
                    for (j in 0 until jsonDetailsObject.length()) {
                        val name = try {
                            jsonDetailsObject.getJSONObject(j).optString(context.getString(R.string.name))
                        } catch (e: Exception) {
                            ""
                        }
                        var icon = ""
                        var poster = ""

                        val imageJsonArray = try {
                            jsonDetailsObject.getJSONObject(j).getJSONArray("image")
                        } catch (e: Exception) {
                            null
                        }
                        if (imageJsonArray != null) {
                            for (k in 0 until imageJsonArray.length()) {
                                if (imageJsonArray.getJSONObject(k).optString("name") == "Icon") {
                                    icon = imageJsonArray.getJSONObject(k).optString("url")
                                } else if (imageJsonArray.getJSONObject(k).optString("name") == "Poster") {
                                    poster = imageJsonArray.getJSONObject(k).optString("url")
                                }
                            }
                        }

                        val imagePath = if (icon == "") {
                            poster
                        } else {
                            icon
                        }

                        val synopsis = try {
                            jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("SYNOPSIS")
                        } catch (e: Exception) {
                            ""
                        }
                        val director = try {
                            jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("DIRECTORS")
                        } catch (e: Exception) {
                            ""
                        }
                        val castCrew = try {
                            jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("CASTCREW")
                        } catch (e: Exception) {
                            ""
                        }
                        val writtenBy = try {
                            jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("WRITTENBY")
                        } catch (e: Exception) {
                            ""
                        }
                        val released = try {
                            jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("RELEASED")
                        } catch (e: Exception) {
                            ""
                        }
                        val runTime = try {
                            jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("RUNTIME")
                        } catch (e: Exception) {
                            ""
                        }
                        val viewCount = try {
                            jsonDetailsObject.getJSONObject(j).optString("viewCount")
                        } catch (e: Exception) {
                            ""
                        }

                        var genre = ""
                        if (jsonDetailsObject.getJSONObject(j).getJSONArray("genres").length() > 0) {
                            genre = jsonDetailsObject.getJSONObject(j).getJSONArray("genres").optJSONObject(0).optString("id")
                        }

                        val contentTypeObject = when {
                            jsonDetailsObject.getJSONObject(j).optString("videoType") == AppUtility.contentTypeMovie -> AppUtility.contentTypeMovie
                            else -> AppUtility.contentTypeVideo
                        }


                        val genresJson = try {
                            jsonDetailsObject.getJSONObject(j).getJSONArray("genres").toString()
                        } catch (e: Exception) {
                            ""
                        }


                        var playableUrl = ""
                        val playableJsonArray = jsonDetailsObject.getJSONObject(j).getJSONArray("playbackStreamProfile")
                        if (playableJsonArray.length() > 0) {
                            playableUrl = playableJsonArray.optJSONObject(0).optJSONArray("urltype").optJSONObject(0).optString("value")
                        }

                        val movieDerailsData = MovieDetailsData(name, imagePath, synopsis, playableUrl, released, director, castCrew, writtenBy, runTime, viewCount, genre, contentTypeObject, genresJson)

                        if (contentType == contentTypeObject) {
                            homeContentList.add(movieDerailsData)
                        }
                    }
                }
            }
            return homeContentList
        }

    }
}
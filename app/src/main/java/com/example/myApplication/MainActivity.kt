package com.example.myApplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.myApplication.adapters.RecyclerViewBaseAdapter
import com.example.myApplication.adapters.RecyclerViewVodNewlyAddedAdapter
import com.example.myApplication.constants.AppUtility
import com.example.myApplication.constants.CustomLinearLayoutManager
import com.example.myApplication.constants.SpacesItemDecoration
import com.example.myApplication.model.CarouselData
import com.example.myApplication.model.MovieDetailsData
import com.example.myApplication.networkinfo.CommonAsynTask
import com.example.myApplication.networkinfo.NetworkInfoResponse
import kotlinx.android.synthetic.main.app_bar_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, NetworkInfoResponse {

    private lateinit var toolBar: Toolbar
    private lateinit var searchIcon: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var carouselImagesLayout: LinearLayout
    private lateinit var commonAsynTask: CommonAsynTask
    private lateinit var carouselList: ArrayList<CarouselData>
    private lateinit var screenWidth: Number
    private lateinit var screenHeight: Number
    private lateinit var viewFlipper: ViewPager
    private lateinit var adsIndicatorLayout: TabLayout
    private lateinit var context: Context
    private lateinit var myPagerAdapter: ViewPagerAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var progressContent: ProgressBar
    private lateinit var homepageLayout: LinearLayout
    private lateinit var spacesItemDecoration: SpacesItemDecoration
    private var mVisible: Boolean = false
    private var handler: Handler? = null
    private var sharedPreferences: SharedPreferences? = null
    private var bundle: Bundle? = null


    override fun onDestroy() {
        super.onDestroy()
        mVisible = false
        handler!!.removeCallbacks(mFlipRunnable)
    }


    private fun startFlipping() {
        handler!!.removeCallbacks(mFlipRunnable)
        handler!!.postDelayed(mFlipRunnable, 5000)
    }

    private val mFlipRunnable = Runnable {
        if (viewFlipper.currentItem < carouselList.size - 1) {
            viewFlipper.currentItem = viewFlipper.currentItem.plus(1)
        } else {
            viewFlipper.currentItem = 0
        }
        startFlipping()
    }


    override fun onResume() {
        super.onResume()
        mVisible = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        setupToolbar()
        carouselList = arrayListOf()

        handler = Handler()


        spacesItemDecoration = SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.u_video__recycler_view_Separator_padding_inner))


        drawerLayout = findViewById(R.id.drawer_layout)


        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)


        val toggle = ActionBarDrawerToggle(context as MainActivity, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView = findViewById(R.id.nav_view)

        navView.setNavigationItemSelectedListener(context as MainActivity)


        val point = AppUtility.getDeviceDimension(context as MainActivity)
        screenWidth = point.x
        screenHeight = point.y

        progressBar = findViewById(R.id.progress)
        carouselImagesLayout = findViewById(R.id.carouselImagesLayout)
        progressContent = findViewById(R.id.progressContent)
        homepageLayout = findViewById(R.id.homepageLayout)

        commonAsynTask = CommonAsynTask(AppUtility.carouselImageUrl, context as MainActivity, AppUtility.get, AppUtility.carousels)
        commonAsynTask.execute()


        commonAsynTask = CommonAsynTask(AppUtility.totalContentUrl, context as MainActivity, AppUtility.get, AppUtility.contentTypeMovie)
        commonAsynTask.execute()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    private fun setupToolbar() {
        toolBar = findViewById(R.id.toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(context as MainActivity, android.R.color.background_dark))
        searchIcon = toolbar.findViewById(R.id.search)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.title = ""
            val ab = supportActionBar
            ab!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {

            }
            R.id.nav_movies -> {
                val int = Intent(context, MoreVideosActivity::class.java)
                int.putExtra(getString(R.string.contentType), AppUtility.contentTypeMovie)
                startActivity(int)
            }
            R.id.nav_videos -> {
                val int = Intent(context, MoreVideosActivity::class.java)
                int.putExtra(getString(R.string.contentType), AppUtility.contentTypeVideo)
                startActivity(int)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private var genre: String? = null

    override fun networkInfoResponse(response: String, from: String) {

        if (from == AppUtility.carousels) {

            Log.e("--------", "=---------------->" + response)
            if (response == "") {
                Toast.makeText(context as MainActivity, "Please check internet settings", Toast.LENGTH_SHORT).show()
            } else {
                val js = JSONObject(response)
                carouselList.clear()
                val responseCode = js.optJSONObject(getString(R.string.responseStatus)).optString(getString(R.string.statusCode))
                if (responseCode == ("200")) {
                    val jsonArray = js.optJSONObject(getString(R.string.data)).optJSONArray("content")
                    (0 until jsonArray.length()).asSequence().map {
                        CarouselData(jsonArray.getJSONObject(it).optString(getString(R.string.mobileCarouselImages)), jsonArray.getJSONObject(it).optString("title"), jsonArray.getJSONObject(it).optString("carouselImageUrl"))
                    }.forEach {
                                carouselList.add(it)
                            }

                    progressBar.visibility = GONE

                    if (carouselList.size > 0) {
                        headerView()
                    }
                }
            }
        } else if (from == AppUtility.homePageContent) {
            progressContent.visibility = GONE
            val js = JSONObject(response)
            val responseCode = js.optJSONObject(getString(R.string.responseStatus)).optString(getString(R.string.statusCode))
            if (responseCode == "200") {
                val jsonArray = js.optJSONArray("content")


                for (i in 0 until jsonArray.length()) {

                    val contentType = when {
                        jsonArray.getJSONObject(i).optString("contentType") == AppUtility.contentTypeMovie -> AppUtility.contentTypeMovie
                        jsonArray.getJSONObject(i).optString("contentType") == AppUtility.contentTypeAD -> "AD"
                        else -> AppUtility.contentTypeVideo
                    }

                    val nameContentType = jsonArray.getJSONObject(i).optString("title")
                    val jsonDetailsObject = jsonArray.getJSONObject(i).optJSONArray("contentList")

                    if (contentType == "AD") {
                        assert(jsonDetailsObject != null)
                        for (j in 0 until jsonDetailsObject.length()) {
                            try {
                                if (jsonDetailsObject.getJSONObject(j).getString("adSlotType").equals("BigBoxBannerAD", ignoreCase = true)) {
                                    try {
                                        homepageLayout.addView(rectAngularAdsDisplay(jsonDetailsObject.getJSONObject(j).getString("leaderBoardValue"), AppUtility.yes))
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }

                                } else if (jsonDetailsObject.getJSONObject(j).getString("adSlotType").equals("LEADERBOARDAD", ignoreCase = true)) {
                                    try {
                                        homepageLayout.addView(rectAngularAdsDisplay(jsonDetailsObject.getJSONObject(j).getString("leaderBoardValue"), AppUtility.no))
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }

                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        }
                    } else {
                        val homeContentList: ArrayList<MovieDetailsData> = arrayListOf()
                        homeContentList.clear()
                        for (j in 0 until jsonDetailsObject.length()) {
                            val name = jsonDetailsObject.getJSONObject(j).optString(getString(R.string.name))
                            var icon = ""
                            var poster = ""

                            val imageJsonArray = jsonDetailsObject.getJSONObject(j).optJSONArray("image")

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
                            val released = try {
                                jsonDetailsObject.getJSONObject(j).getJSONObject("metaData").optString("RELEASED")
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

                            genre = ""
                            try {

                                if (jsonDetailsObject.getJSONObject(j).getJSONArray("genres").length() > 0) {
                                    genre = try {
                                        jsonDetailsObject.getJSONObject(j).getJSONArray("genres").optJSONObject(0).optString("id")
                                    } catch (e: Exception) {
                                        ""
                                    }
                                }
                            } catch (e: Exception) {
                                genre = ""
                            }


                            val contentTypeObject = try {
                                jsonDetailsObject.getJSONObject(j).optString("videoType")
                            } catch (e: Exception) {
                                ""
                            }
                            val genresJson = try {
                                jsonDetailsObject.getJSONObject(j).getJSONArray("genres").toString()
                            } catch (e: Exception) {
                                ""
                            }


                            var playableUrl = ""
                            var playableJsonArray: JSONArray? = null
                            try {
                                playableJsonArray = jsonDetailsObject.getJSONObject(j).getJSONArray("playbackStreamProfile")
                            } catch (e: Exception) {
                            }

                            if (playableJsonArray != null) {
                                if (playableJsonArray.length() > 0) {
                                    playableUrl = playableJsonArray.optJSONObject(0).optJSONArray("urltype").optJSONObject(0).optString("value")
                                }
                            }

                            val movieDerailsData = MovieDetailsData(name, imagePath, synopsis, playableUrl, released, director, castCrew, writtenBy, runTime, viewCount, genre!!, contentTypeObject, genresJson)
                            homeContentList.add(movieDerailsData)
                        }
                        homepageLayout.addView(homePageContentDisplay(nameContentType, homeContentList, contentType))


                    }

                }

                progressBar.visibility = GONE
            }
        } else if (from == AppUtility.contentTypeMovie) {
            if (response != "") {
                val jsonObject = JSONObject(response)
                val responseCode = jsonObject.optJSONObject(context.getString(R.string.responseStatus)).optString(context.getString(R.string.statusCode))
                if (responseCode == ("200")) {
                    sharedPreferences!!.edit().putString(getString(R.string.contentString), response).apply()
                }
            }
        }
    }

    private fun rectAngularAdsDisplay(adId: String, value: Boolean): View {
        val view = LayoutInflater.from(context).inflate(R.layout.ads_view, homepageLayout, AppUtility.no)
        val advTopBanner = view.findViewById<RelativeLayout>(R.id.home_carousel_big_tiles_banner)
        if (value) {
            advTopBanner.addView(AppUtility.rectAngularBannerView(context, adId))
        } else {
            advTopBanner.addView(AppUtility.largeBannerView(context, adId))
        }
        return view
    }

    private fun headerView() {

        val height = AppUtility.getHeightOfIcon(screenHeight as Int)
        val layoutParams = AbsListView.LayoutParams(screenWidth as Int, height)

        val header = LayoutInflater.from(context).inflate(R.layout.view_pager_home_crousal, carouselImagesLayout, AppUtility.no) as RelativeLayout
        header.layoutParams = layoutParams

        viewFlipper = header.findViewById(R.id.viewFlipper)
        adsIndicatorLayout = header.findViewById(R.id.viewFlipper_indicator)





        myPagerAdapter = ViewPagerAdapter(context as MainActivity, carouselList)
        viewFlipper.adapter = (myPagerAdapter)
        adsIndicatorLayout.setupWithViewPager(viewFlipper)

        carouselImagesLayout.addView(header)
        progressContent.visibility = VISIBLE


        viewFlipper.currentItem = 0
        startFlipping()

        handler!!.postDelayed(mFlipRunnable, 5000)

        commonAsynTask = CommonAsynTask(AppUtility.homeContentUrl, context as MainActivity, AppUtility.get, AppUtility.homePageContent)
        commonAsynTask.execute()

    }


    private inner class ViewPagerAdapter internal constructor(context1: Context, var arrayList: ArrayList<CarouselData>) : PagerAdapter() {

        lateinit var imageURL: String
        var context = context1

        override fun getItemPosition(`object`: Any): Int {
            return super.getItemPosition(`object`)
        }

        override fun getCount(): Int {
            return arrayList.size
        }

        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(context).inflate(R.layout.viewpager_row, container, false) as ViewGroup
            val imageView = view.findViewById<ImageView>(R.id.image_view_video_image)
            imageURL = arrayList[position].imagePath
            imageURL = imageURL.replace("c_fill,g_north,w_250,h_250/", "")

            Glide.with(context).load(imageURL).thumbnail(0.5f).into(imageView).onLoadFailed(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))

            container.addView(view)

            return view
        }


        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

    }


    private fun homePageContentDisplay(title: String, limitedList: ArrayList<MovieDetailsData>, contentType: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.home_crousal_list_video_row_recycler, homepageLayout, false)
        val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView.addItemDecoration(spacesItemDecoration)
        recyclerView.layoutManager = (CustomLinearLayoutManager(context, AppUtility.no))
        val textView1 = view.findViewById(R.id.genreHolder) as TextView
        val textView2 = view.findViewById(R.id.seeAll) as TextView
        val point: Point = if (title.equals(AppUtility.contentTypeMovie, ignoreCase = true)) {
            AppUtility.getMovieDimension(context)
        } else {
            AppUtility.getVideoDimension(context)
        }



        if (limitedList.size > 5) {
            textView2.visibility = VISIBLE
            textView2.text = getString(R.string.more)
        } else {
            textView2.visibility = GONE
        }

        val adapter: RecyclerViewVodNewlyAddedAdapter?

        textView1.setText(title, TextView.BufferType.SPANNABLE)
        adapter = RecyclerViewVodNewlyAddedAdapter(limitedList, context as MainActivity, point.x, point.y, title, object : RecyclerViewBaseAdapter.OnRecyclerViewClick {
            override fun onClickEvent(movieDetailsData: MovieDetailsData) {
                Toast.makeText(context, movieDetailsData.name, Toast.LENGTH_SHORT).show()
                val int = Intent(context, PlayerActivity::class.java)
                int.putExtra("playingUrl", movieDetailsData.playerUrl)
                startActivity(int)
            }
        })
        recyclerView.adapter = adapter

        textView2.setOnClickListener({
            val int = Intent(context, MoreVideosActivity::class.java)
            int.putExtra(getString(R.string.contentType), contentType)
            bundle= Bundle()
            bundle!!.putParcelableArrayList("data", limitedList)
            startActivity(int)
        })

        return view
    }


}

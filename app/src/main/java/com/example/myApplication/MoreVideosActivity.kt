package com.example.myApplication

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import com.example.myApplication.constants.AppUtility
import com.example.myApplication.model.MovieDetailsData
import kotlinx.android.synthetic.main.activity_more_videos.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.example.myApplication.constants.SpacesItemDecoration


class MoreVideosActivity : AppCompatActivity() {


    private var gridLayoutManager: GridLayoutManager? = null
    private var sharedPreferences: SharedPreferences? = null
    private var contentString: String? = null
    private var contentType: String? = null
    private var contentArrayList: ArrayList<MovieDetailsData> = arrayListOf()
    private var moreVideosAdapter: MoreVideosAdapter? = null
    private var spacesItemDecoration: SpacesItemDecoration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_videos)
        backNavigation.setOnClickListener {
            finish()
        }

        spacesItemDecoration = SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.u_gridLayout), true)
        recyclerView.addItemDecoration(spacesItemDecoration)
        this.gridLayoutManager = GridLayoutManager(this, 2, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, false)
        recyclerView.layoutManager = gridLayoutManager!!

        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

        contentType = intent.extras.getString(getString(R.string.contentType))
        when (contentType) {
            AppUtility.contentTypeMovie -> moreVideosTitle.setText(AppUtility.moreMovies, TextView.BufferType.NORMAL)
            AppUtility.contentTypeVideo -> moreVideosTitle.setText(AppUtility.moreVideos, TextView.BufferType.NORMAL)
            else -> moreVideosTitle.setText(contentType, TextView.BufferType.NORMAL)
        }


        this.contentString = sharedPreferences!!.getString(getString(R.string.contentString), getString(R.string.defaultValue))
        contentArrayList = AppUtility.parseJson(contentString!!, this, contentType!!)


        contentDisplay(contentArrayList)
    }

    private fun contentDisplay(arrayList: ArrayList<MovieDetailsData>) {
        moreVideosAdapter = MoreVideosAdapter(arrayList, this, contentType!!)
        recyclerView.adapter = moreVideosAdapter
    }


    class MoreVideosAdapter(arrayList: ArrayList<MovieDetailsData>, context: Context, contentType: String) : RecyclerView.Adapter<MoreVideosAdapter.MyViewHolder>() {

        private val adapterList = arrayList
        private val adapterContext = context
        private val adapterContentType = contentType
        private var layoutParams: RelativeLayout.LayoutParams? = null

        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView? = null
            var textView: TextView? = null
            var layout: RelativeLayout? = null

            init {
                imageView = view.findViewById(R.id.imageView)
                textView = view.findViewById(R.id.textView)
                layout = view.findViewById(R.id.relativeLayout)
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(adapterContext).inflate(R.layout.single_row_content, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder?, position: Int) {
            holder?.textView!!.setText(adapterList[position].name, TextView.BufferType.NORMAL)
            Glide.with(adapterContext).load(adapterList[position].imagePath.replace("c_fill,g_north,w_250,h_250/", "")).into(holder.imageView).onLoadFailed(ContextCompat.getDrawable(adapterContext, R.mipmap.ic_launcher))
            val point: Point = if (this.adapterContentType.equals(AppUtility.contentTypeMovie, ignoreCase = true)) {
                AppUtility.getGridMovieDimension(adapterContext)
            } else {
                AppUtility.getGridVideoDimension(adapterContext)
            }


            holder.imageView!!.scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = RelativeLayout.LayoutParams(point.x, point.y)
            holder.layout!!.layoutParams = layoutParams
            holder.textView!!.width = point.x
            holder.imageView!!.scaleType = ImageView.ScaleType.FIT_XY
        }

        override fun getItemCount(): Int {
            return adapterList.size
        }
    }
}

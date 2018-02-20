package com.example.myApplication.adapters

import android.app.Activity
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.myApplication.R
import com.example.myApplication.constants.AppUtility
import com.example.myApplication.model.MovieDetailsData

import java.util.ArrayList


open class RecyclerViewBaseAdapter(private val videoDTOs: ArrayList<MovieDetailsData>, private val activity: Activity, private val width: Int, private val height: Int, private val contentType: String, onRecyclerViewClick: OnRecyclerViewClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layout: Int = R.layout.recycler_view_video
    private var isClicked = false

    private var recyclerViewClick = onRecyclerViewClick

    private val handler = Handler()
    private val runnable = Runnable { isClicked = false }

    interface OnRecyclerViewClick {
        fun onClickEvent(movieDetailsData: MovieDetailsData)
    }


    override fun getItemCount(): Int {
        return if (videoDTOs.size > 5) {
            5
        } else {
            videoDTOs.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder
        viewHolder = if (viewType == VIEW_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
            ItemViewHolder(view)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.progress_item, parent, false)

            ProgressViewHolder(v)
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return if (videoDTOs[position] != null) VIEW_ITEM else VIEW_PROG
    }

    override fun onBindViewHolder(parentHolder: RecyclerView.ViewHolder, i: Int) {
        if (parentHolder is RecyclerViewBaseAdapter.ProgressViewHolder) {
            parentHolder.progressBar.isIndeterminate = true
            return
        }
        var imageURL = videoDTOs[i].imagePath

        val holder = parentHolder as ItemViewHolder

        val point: Point = if (contentType.equals("MOVIE", ignoreCase = true)) {
            AppUtility.getVideoDimension(activity)
        } else {
            AppUtility.getVideoDimension(activity)
        }


        imageURL = imageURL.replace("c_fill,g_north,w_250,h_250/", "")


        holder.textViewTitle.width = point.x

        Glide.with(activity).load(imageURL).thumbnail(0.5f).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                holder.videoImage.scaleType = ImageView.ScaleType.FIT_CENTER
                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                holder.videoImage.scaleType = ImageView.ScaleType.FIT_XY
                return false

            }
        }).into(holder.videoImage)


        val layoutParams = RelativeLayout.LayoutParams(point.x, point.y)
        holder.videoImage.layoutParams = layoutParams

        holder.videoImage.setOnClickListener {
            if (!isClicked) {
                isClicked = true
                handler.postDelayed(runnable, 2000)
                recyclerViewClick.onClickEvent(videoDTOs[i])
            }
        }

        holder.textViewTitle.setText(videoDTOs[i].name.replace("&ldquo;", "\"").replace("&rdquo;", "\"").replace("&rsquo;", "\'").replace("&lsquo;", "\'"), TextView.BufferType.EDITABLE)


    }


    class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val progressBar: ProgressBar = v.findViewById(R.id.progressBar)

    }

    inner class ItemViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val videoImage: ImageView = itemView.findViewById(R.id.image_view_video_image)
        internal val textViewTitle: TextView = itemView.findViewById(R.id.text_view_title)


        init {
            videoImage.layoutParams.width = width
            videoImage.layoutParams.height = height
            textViewTitle.layoutParams.width = width
        }
    }

    companion object {

        private const val VIEW_ITEM = 1
        private const val VIEW_PROG = 0
    }
}


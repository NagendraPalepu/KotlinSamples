package com.example.myApplication.adapters

import android.app.Activity

import com.example.myApplication.model.MovieDetailsData

import java.util.ArrayList

/**
 * Created by Nagendra.P on 6/23/2017.
 */

class RecyclerViewVodNewlyAddedAdapter(arrayList: ArrayList<MovieDetailsData>, activity: Activity, width: Int, height: Int, contentType: String,onRecyclerViewClick: OnRecyclerViewClick) : RecyclerViewBaseAdapter(arrayList, activity, width, height, contentType,onRecyclerViewClick )
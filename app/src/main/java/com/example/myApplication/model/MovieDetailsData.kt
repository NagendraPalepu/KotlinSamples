package com.example.myApplication.model

import android.os.Parcel
import android.os.Parcelable
/**
 * Created by Nagendra.P on 1/11/2018.
 */


class MovieDetailsData internal constructor(var name: String, var imagePath: String, var synopsis: String, var playerUrl: String, private var releaseDate: String, var director: String, var castCrew: String, var writtenBy: String, private var runTime: String, var viewCount: String, private var genre: String, private var contentType: String, private var genresJson: String) : Parcelable {

    private constructor(parcel: Parcel) : this(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(imagePath)
        dest.writeString(synopsis)
        dest.writeString(playerUrl)
        dest.writeString(releaseDate)
        dest.writeString(director)
        dest.writeString(castCrew)
        dest.writeString(writtenBy)
        dest.writeString(runTime)
        dest.writeString(viewCount)
        dest.writeString(genre)
        dest.writeString(genresJson)
        dest.writeString(contentType)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<MovieDetailsData> {
        override fun createFromParcel(source: Parcel): MovieDetailsData {
            return MovieDetailsData(source)
        }

        override fun newArray(size: Int): Array<MovieDetailsData?> {
            return arrayOfNulls(size)
        }
    }
}



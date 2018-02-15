package com.example.myApplication.networkinfo

import android.content.Context
import android.os.AsyncTask


/**
 * Created by Nagendra.P on 1/10/2018.
 */
class CommonAsynTask internal constructor(url: String, contextClass: Context, methodType: String, from: String) : AsyncTask<Void, Void, String>() {

    private lateinit var response: String
    private var basicUrl = url
    private var method = methodType
    private var context = contextClass
    private lateinit var networkInfoResponse: NetworkInfoResponse
    private var isFrom = from


    override fun doInBackground(vararg params: Void?): String {
        if (method == "GET") {
            response = NetworkInfo.getResponse(basicUrl)
        } else {

        }
        return response
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        networkInfoResponse = context as NetworkInfoResponse
        networkInfoResponse.networkInfoResponse(response, isFrom)
    }


}
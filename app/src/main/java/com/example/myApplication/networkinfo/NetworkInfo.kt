package com.example.myApplication.networkinfo

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Nagendra.P on 1/10/2018.
 */
class NetworkInfo {

    companion object {

        private var inString = ""
        fun getResponse(url: String): String {

            val urlConnection: HttpURLConnection?

            try {
                val url12 = URL(url)

                urlConnection = url12.openConnection() as HttpURLConnection
                urlConnection.getRequestProperty("application/json")
                urlConnection.requestMethod = "GET"
                urlConnection.connectTimeout = 9000
                urlConnection.readTimeout = 9000
                urlConnection.connect()

                this.inString = streamToString(urlConnection.inputStream)


            } catch (ex: Exception) {
                inString = ""
            }
            return this.inString
        }


        private fun streamToString(inputStream: InputStream): String {

            val bufferReader = BufferedReader(InputStreamReader(inputStream))
            val line: String
            var result = ""

            try {

                line = bufferReader.readLine()
                if (line != null) {
                    result += line
                }

                inputStream.close()
            } catch (ex: Exception) {

            }

            return result
        }
    }


}
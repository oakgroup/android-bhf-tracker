package com.active.orbit.tracker.data_upload

import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class HttpsServer {

    companion object {
        val TAG: String? = this::class.simpleName
    }

    fun sendToServer(urlx: String, data: JSONObject): JSONObject? {
        var responseObject: JSONObject? = null
        val url = URL(urlx)
        //@todo implement https !!! urgent!!!
        //  val urlConnection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        Log.i(TAG, "Sending data to $urlx")
        try {
            urlConnection.doOutput = true
            urlConnection.setChunkedStreamingMode(0)
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            urlConnection.setRequestProperty("Accept", "application/json")
            val out = BufferedOutputStream(urlConnection.outputStream)
            val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
            writer.write(data.toString())
            writer.flush()
            writer.close()
            urlConnection.connect()
            try {
                val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                val reader = BufferedReader(InputStreamReader(`in`))
                val result = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    result.append(line)
                }
                Log.d("test", "result from server: $result")
                responseObject = JSONObject(result.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
        } finally {
            urlConnection.disconnect()
        }
        return responseObject
    }
}
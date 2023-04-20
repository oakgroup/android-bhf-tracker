package com.active.orbit.tracker.data_upload.data_senders

import android.util.Log
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject


class DataSenderUtils {

    private val TAG: String = javaClass.simpleName

    fun getJSONArrayOfObjects(elementList: List<*>): JSONArray {
        val gson = Gson()
        val jsonArray = JSONArray()
        for (element in elementList) {
            try {
                val gsonObject = JSONObject(gson.toJson(element, Any::class.java))
                jsonArray.put(gsonObject)
            } catch (ex: Exception) {
                Log.e(TAG, "Exception in creating JSON array of element for sending to server $ex.message")
            }
        }
        return jsonArray
    }
}
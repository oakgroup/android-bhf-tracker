package com.active.orbit.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import com.active.orbit.tracker.R
import com.active.orbit.tracker.Repository
import com.active.orbit.tracker.data_upload.HttpsServer
import com.active.orbit.tracker.data_upload.dts_data.BatteryDataDTS
import com.active.orbit.tracker.tracker.sensors.batteries.BatteryData
import com.active.orbit.tracker.utils.Globals
import org.json.JSONObject

class BatteryDataSender(val context: Context) {

    companion object {
        private val TAG: String? = this::class.simpleName
    }

    private val repositoryInstance: Repository? = Repository.getInstance(context)
    private var batteriesToSend: List<BatteryData?>? = null

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param context
     * @param userID
     */
    fun sendBatteryData(userID: String) {
        val dataToSend: JSONObject = prepareBatteryData(userID) ?: return
        val url: String = context.getString(R.string.base_url) + Globals.SERVER_PORT + Globals.SERVER_INSERT_BATTERIES_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            Log.i(TAG, "ok batteries data sent")
            if (batteriesToSend != null) {
                val batteriesIds = batteriesToSend?.map { it?.id }
                repositoryInstance?.dBBatteryDao?.updateSentFlag(batteriesIds)
            }
        }
    }

    /**
     * it prepares the batteries to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server or null
     */
    private fun prepareBatteryData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID_FOR_BATTERIES, userID)
        batteriesToSend = collectBatteriesFromDatabase()
        if (batteriesToSend != null && batteriesToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${batteriesToSend!!.size} batteries data")
            val batteriesDTSList: MutableList<BatteryDataDTS> = mutableListOf()
            for (battery in batteriesToSend!!)
                if (battery != null)
                    batteriesDTSList.add(BatteryDataDTS(battery))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(Globals.BATTERIES_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(batteriesDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No batteries to send")
        }
        return null
    }

    private fun collectBatteriesFromDatabase(): List<BatteryData?>? {
        return repositoryInstance?.dBBatteryDao?.getUnsentBatteryData(900)!!
    }
}
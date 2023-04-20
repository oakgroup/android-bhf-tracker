package com.active.orbit.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import com.active.orbit.tracker.R
import com.active.orbit.tracker.Repository
import com.active.orbit.tracker.data_upload.HttpsServer
import com.active.orbit.tracker.data_upload.dts_data.HeartRateDataDTS
import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import com.active.orbit.tracker.utils.Globals
import org.json.JSONObject

class HeartRateDataSender(val context: Context) {
    companion object {
        private val TAG: String? = this::class.simpleName
    }

    private val repositoryInstance: Repository? = Repository.getInstance(context)
    private var heartRatesToSend: List<HeartRateData?>? = null

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param context
     * @param userID
     */
    fun sendHeartRateData(userID: String) {
        val dataToSend: JSONObject = prepareHeartRateData(userID) ?: return
        val url: String = context.getString(R.string.base_url) + Globals.SERVER_PORT + Globals.SERVER_INSERT_HEART_RATES_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            Log.i(TAG, "ok")
            if (heartRatesToSend != null) {
                val heartRatesIds = heartRatesToSend?.map { it?.id }
                repositoryInstance?.dBHeartRateDao?.updateSentFlag(heartRatesIds)
            }
        }
    }

    /**
     * it prepares the heartRates to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server or null
     */
    private fun prepareHeartRateData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID, userID)
        heartRatesToSend = collectHeartRatesFromDatabase()
        if (heartRatesToSend != null && heartRatesToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${heartRatesToSend!!.size} heartRates")
            val heartRatesDTSList: MutableList<HeartRateDataDTS> = mutableListOf()
            for (location in heartRatesToSend!!)
                if (location != null)
                    heartRatesDTSList.add(HeartRateDataDTS(location))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(Globals.HEART_RATES_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(heartRatesDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No heartRates to send")
        }
        return null
    }

    private fun collectHeartRatesFromDatabase(): List<HeartRateData?>? {
        return repositoryInstance?.dBHeartRateDao?.getUnsentHeartRateData(900)!!
    }
}
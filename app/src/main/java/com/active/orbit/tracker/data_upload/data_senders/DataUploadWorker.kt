package com.active.orbit.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.active.orbit.tracker.R
import com.active.orbit.tracker.tracker.TrackerNotification
import com.active.orbit.tracker.utils.Globals
import com.active.orbit.tracker.utils.PreferencesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataUploadWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {
        val TAG: String? = this::class.simpleName
        var sendingData: Boolean = false
        private const val NOTIFICATION_ID = 9973
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                Log.i(TAG, "Data Upload Work manager fired")
                uploadData()
                Result.success()
            }
        } catch (e: Throwable) {
            Result.failure()
        }
    }

    private fun uploadData() {
        val preference = PreferencesStore()
        val loadData = preference.getBooleanPreference(context, Globals.SEND_DATA_TO_SERVER, true)
        Log.i(TAG, "shall we upload data? $loadData")
        if (loadData == true) {
            if (sendingData) return
            sendingData = true
            val userPreferences = PreferencesStore()
            val userId = userPreferences.getStringPreference(context, Globals.USER_ID, "")
            if (userId == null || userId == "") {
                Log.i(TAG, "No user Id assigned yet")
                return
            }
            Log.i(TAG, " Sending data")
            val activityDataSender = ActivityDataSender(context)
            activityDataSender.sendActivityData(userId)
            val locationDataSender = LocationDataSender(context)
            locationDataSender.sendLocationData(userId)
            val stepsDataSender = StepsDataSender(context)
            stepsDataSender.sendStepData(userId)
            val hrDataSender = HeartRateDataSender(context)
            hrDataSender.sendHeartRateData(userId)
            val batteryDataSender = BatteryDataSender(context)
            batteryDataSender.sendBatteryData(userId)

            val useMobilityModelling = preference.getBooleanPreference(context, Globals.USE_MOBILITY_MODELLING, true)
            Log.i(TAG, "About to send trips: shall we? $useMobilityModelling")
            useMobilityModelling.let {
                if (useMobilityModelling!!) {
                    val tripsSender = TripsDataSender(context)
                    tripsSender.sendAllTripsToServer(userId)
                }
            }
            sendingData = false
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        TrackerNotification.notificationText = "do not close the app, please"
        TrackerNotification.notificationIcon = R.drawable.ic_notification
        val notification = TrackerNotification(context, NOTIFICATION_ID, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}

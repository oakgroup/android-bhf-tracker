package com.active.orbit.tracker.core.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.TrackerPreferences
import com.active.orbit.tracker.core.tracker.TrackerNotification
import com.active.orbit.tracker.core.upload.uploaders.*
import com.active.orbit.tracker.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackerUploadWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {
        var sendingData: Boolean = false
        private const val NOTIFICATION_ID = 9973
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                Logger.d("Data Upload Work manager fired")
                uploadData()
                Result.success()
            }
        } catch (e: Throwable) {
            Result.failure()
        }
    }

    private fun uploadData() {
        if (sendingData) return
        if (TrackerPreferences.backend(context).uploadData) {
            sendingData = true
            if (!TrackerPreferences.user(context).isUserRegistered()) {
                Logger.d("No user Id assigned yet")
                return
            }
            // upload data
            if (TrackerPreferences.config(context).useActivityRecognition) ActivitiesUploader.uploadData(context)
            if (TrackerPreferences.config(context).useBatteryMonitor) BatteriesUploader.uploadData(context)
            if (TrackerPreferences.config(context).useLocationTracking) LocationsUploader.uploadData(context)
            if (TrackerPreferences.config(context).useStepCounter) StepsUploader.uploadData(context)
            if (TrackerPreferences.config(context).useHeartRateMonitor) HeartRatesUploader.uploadData(context)
            if (TrackerPreferences.config(context).useMobilityModelling) TripsUploader.uploadData(context)
            sendingData = false
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        TrackerNotification.notificationText = "Do not close the app, please"
        TrackerNotification.notificationIcon = R.drawable.ic_notification
        val notification = TrackerNotification(context, NOTIFICATION_ID, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}

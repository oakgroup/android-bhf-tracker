package com.active.orbit.tracker.tracker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.active.orbit.tracker.R
import com.active.orbit.tracker.utils.Globals
import com.active.orbit.tracker.utils.PreferencesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TackerRestarterWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    companion object {
        val TAG: String? = this::class.simpleName
        private const val NOTIFICATION_ID = 9972
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    val userPreferences = PreferencesStore()
                    val userId = userPreferences.getStringPreference(context, Globals.USER_ID, "")
                    if (!TextUtils.isEmpty(userId)) {
                        // start the tracker only if the user id is set
                        Log.i(TAG, "Checking if current service is null: ${TrackerService.currentTracker}")
                        if (TrackerService.currentTracker == null) {
                            Log.i(TAG, "Launching the tracker from the job service")
                            val trackerServiceIntent = Intent(context, TrackerService::class.java)
                            TrackerNotification.notificationText = "do not close the app, please"
                            TrackerNotification.notificationIcon = R.drawable.ic_notification
                            Log.i(TAG, "Launching tracker")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(trackerServiceIntent)
                            } else {
                                context.startService(trackerServiceIntent)
                            }
                            Log.d(TAG, "started service")
                        }
                    } else {
                        // stop the tracker if the user id is not set
                        TrackerService.currentTracker?.stopSelf()
                    }
                } catch (e: Exception) {
                    // nothing that we can do
                    Log.e(TAG, "Could not start Tracker at boot or regular restart")
                }
                Result.success()
            }
        } catch (e: Throwable) {
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        TrackerNotification.notificationText = "do not close the app, please"
        TrackerNotification.notificationIcon = R.drawable.ic_notification
        val notification = TrackerNotification(context, NOTIFICATION_ID, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}

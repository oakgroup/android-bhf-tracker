package com.active.orbit.tracker.core.restarter

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.TrackerPreferences
import com.active.orbit.tracker.core.tracker.TrackerNotification
import com.active.orbit.tracker.core.tracker.TrackerService
import com.active.orbit.tracker.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrackerRestarterWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {

        private const val NOTIFICATION_ID = 9972
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    if (TrackerPreferences.user(context).isUserRegistered()) {
                        // start the tracker only if the user id is set
                        Logger.i("Checking if current service is null: ${TrackerService.currentTracker}")
                        if (TrackerService.currentTracker == null) {
                            Logger.i("Launching the tracker from the job service")
                            val trackerServiceIntent = Intent(context, TrackerService::class.java)
                            TrackerNotification.notificationText = "Do not close the app, please"
                            TrackerNotification.notificationIcon = R.drawable.ic_notification
                            Logger.i("Launching tracker")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(trackerServiceIntent)
                            } else {
                                context.startService(trackerServiceIntent)
                            }
                            Logger.d("Started service")
                        }
                    } else {
                        // stop the tracker if the user id is not set
                        TrackerService.currentTracker?.stopSelf()
                    }
                } catch (e: Exception) {
                    // nothing that we can do
                    Logger.e("Could not start Tracker at boot or regular restart")
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

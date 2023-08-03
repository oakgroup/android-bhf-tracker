/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.restarter

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.tracker.TrackerNotification
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Tracker coroutine worker that starts the foreground service
 *
 * @param context an instance of [Context]
 * @param workerParams the worker parameters
 */
class TrackerRestarterWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    companion object {

        private const val NOTIFICATION_ID = 9972
    }

    /**
     * This will start the foreground service and shows the permanent notification
     */
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

    /**
     * This declares the worker foreground info
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        TrackerNotification.notificationText = "do not close the app, please"
        TrackerNotification.notificationIcon = R.drawable.ic_notification
        val notification = TrackerNotification(context, NOTIFICATION_ID, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}

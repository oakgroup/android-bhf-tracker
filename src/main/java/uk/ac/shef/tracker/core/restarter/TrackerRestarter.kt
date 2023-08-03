/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.restarter

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.upload.TrackerUploadWorker
import uk.ac.shef.tracker.core.utils.Logger
import java.util.concurrent.TimeUnit

/**
 * Class used to start the tracker and manage the data upload worker
 */
class TrackerRestarter {

    private val dataUploadingWorkName = "upload_work_manager"

    /**
     * Starts the tracker and the data upload (only if charging)
     *
     * @param context an instance of [Context]
     */
    fun startTrackerAndDataUpload(context: Context) {
        startTrackerProper(context)
        startDataUploader(context, true)
    }

    /**
     * This sets the data upload every 45 minutes if charging
     *
     * @param context an instance of [Context]
     */
    fun startDataUploader(context: Context, requiresCharging: Boolean, resultListener: ResultListener? = null) {
        if (!TrackerPreferences.user(context).isUserRegistered()) {
            Logger.d("Data upload not started because the user is not registered")
            return
        }

        Logger.d("Setting the constraints for the work manager")
        val constraints = Constraints.Builder()
            .setRequiresCharging(requiresCharging)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        if (requiresCharging) {
            // do it every 4 hours but do it only in the last 15 mins of the 4 hours (flexInterval)
            // https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
            // why 2 hours? Because we must get the moment when the phone is connected to the mains
            // if we put 12 hours we may miss the window of connection
            // in any case 2 hours do not cost anything because if not connected, it does not do anything
            // -- note: I have removed t
            //
            // he last 15 minutes because it makes testing impossible
            // (you have to wait for 1.45 hours!)
            val work = PeriodicWorkRequestBuilder<TrackerUploadWorker>(45, TimeUnit.MINUTES, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            val workManager = WorkManager.getInstance(context)
            Logger.d("Enqueueing period work for the work manager")
            workManager.enqueueUniquePeriodicWork(
                dataUploadingWorkName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // if it is pending, do not enqueue
                work
            )
        } else {
            val request = OneTimeWorkRequestBuilder<TrackerUploadWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(constraints)
                .build()
            val resultFuture = WorkManager.getInstance(context)
                .enqueue(request)
                .result

            resultFuture.addListener({
                resultListener?.onResult(true)
            }, {
                it?.run() // run on same thread
            })
        }
    }

    /**
     * This starts the tracker one off
     *
     * @param context an instance of [Context]
     */
    private fun startTrackerProper(context: Context) {
        if (!TrackerPreferences.user(context).isUserRegistered()) {
            Logger.d("Tracker not started because the user is not registered")
            return
        }
        Logger.d("Setting constraints for the tracker work manager")
        val constraints = Constraints.Builder()
            .build()
        val request = OneTimeWorkRequestBuilder<TrackerRestarterWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context)
            .enqueue(request)
    }
}
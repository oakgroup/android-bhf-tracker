package com.active.orbit.tracker.restarter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.*
import com.active.orbit.tracker.data_upload.data_senders.DataUploadWorker
import com.active.orbit.tracker.listeners.ResultListener
import com.active.orbit.tracker.tracker.TackerRestarterWorker
import com.active.orbit.tracker.utils.Globals
import com.active.orbit.tracker.utils.PreferencesStore
import java.util.concurrent.TimeUnit


class TrackerRestarter {

    private val dataUploadingWorkName = "general work manager"
    private val sensorWorkName = "tracker work manager"
    private val TAG = this::class.simpleName
    fun startTrackerAndDataUpload(context: Context) {
        startTrackerProper(context)
        startDataUploader(context, true)
    }

    /**
     * it sets the data uplaod every 12 hours if charging
     * @param context
     */
    fun startDataUploader(context: Context, requiresCharging: Boolean, resultListener: ResultListener? = null) {
        val userPreferences = PreferencesStore()
        val userId = userPreferences.getStringPreference(context, Globals.USER_ID, "")
        if (TextUtils.isEmpty(userId)) {
            Log.i(TAG, "Do not start the data upload because the user is not registered")
            return
        }

        Log.i(TAG, "Setting the constraints for the work manager")
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
            val work = PeriodicWorkRequestBuilder<DataUploadWorker>(45, TimeUnit.MINUTES, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            val workManager = WorkManager.getInstance(context)
            Log.i(TAG, "Enqueueing period work for the work manager")
            workManager.enqueueUniquePeriodicWork(
                dataUploadingWorkName,
                // if it is pending, do not enqueue
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                work
            )
        } else {
            val request = OneTimeWorkRequestBuilder<DataUploadWorker>()
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
     * it starts the tracker one off
     * @param context
     */
    private fun startTrackerProper(context: Context) {
        val userPreferences = PreferencesStore()
        val userId = userPreferences.getStringPreference(context, Globals.USER_ID, "")
        if (TextUtils.isEmpty(userId)) {
            Log.i(TAG, "Do not start the tracker because the user is not registered")
            return
        }
        Log.i(TAG, "Setting constraints for the tracker work manager")
        val constraints = Constraints.Builder()
            .build()
        val request = OneTimeWorkRequestBuilder<TackerRestarterWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context)
            .enqueue(request)
    }
}
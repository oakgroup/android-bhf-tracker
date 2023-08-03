/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.upload

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.tracker.TrackerNotification
import uk.ac.shef.tracker.core.upload.uploaders.ActivitiesUploader
import uk.ac.shef.tracker.core.upload.uploaders.BatteriesUploader
import uk.ac.shef.tracker.core.upload.uploaders.HeartRatesUploader
import uk.ac.shef.tracker.core.upload.uploaders.LocationsUploader
import uk.ac.shef.tracker.core.upload.uploaders.StepsUploader
import uk.ac.shef.tracker.core.upload.uploaders.TripsUploader
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Tracker listenable worker that uploads the data to the server
 *
 * @param context an instance of [Context]
 * @param workerParams the worker parameters
 */
class TrackerUploadWorker(val context: Context, workerParams: WorkerParameters) : ListenableWorker(context, workerParams) {

    companion object {
        var sendingData: Boolean = false
        private const val NOTIFICATION_ID = 9973
    }

    /**
     * This starts the async data upload
     */
    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            try {
                Logger.d("Data Upload Work manager fired")
                uploadData(object : ResultListener {
                    override fun onResult(success: Boolean) {
                        sendingData = false
                        completer.set(Result.success())
                    }
                })
            } catch (e: Throwable) {
                completer.set(Result.failure())
            }
        }
    }

    /**
     * This uploads the tracker data to the server
     */
    private fun uploadData(listener: ResultListener? = null) {
        if (sendingData) {
            listener?.onResult(true)
            return
        }

        if (TrackerPreferences.backend(context).uploadData) {
            sendingData = true
            if (!TrackerPreferences.user(context).isUserRegistered()) {
                Logger.d("No user Id assigned yet")
                listener?.onResult(false)
                return
            }

            var callbacks = 0
            val callbacksExpected = 6

            // upload data
            if (TrackerPreferences.config(context).useActivityRecognition) {
                ActivitiesUploader.uploadData(context, object : ResultListener {
                    override fun onResult(success: Boolean) {
                        callbacks++
                        if (callbacks == callbacksExpected) {
                            listener?.onResult(true)
                        }
                    }
                })
            }
            if (TrackerPreferences.config(context).useBatteryMonitor) {
                BatteriesUploader.uploadData(context, object : ResultListener {
                    override fun onResult(success: Boolean) {
                        callbacks++
                        if (callbacks == callbacksExpected) {
                            listener?.onResult(true)
                        }
                    }
                })
            }
            if (TrackerPreferences.config(context).useLocationTracking) {
                LocationsUploader.uploadData(context, object : ResultListener {
                    override fun onResult(success: Boolean) {
                        callbacks++
                        if (callbacks == callbacksExpected) {
                            listener?.onResult(true)
                        }
                    }
                })
            }
            if (TrackerPreferences.config(context).useStepCounter) {
                StepsUploader.uploadData(context, object : ResultListener {
                    override fun onResult(success: Boolean) {
                        callbacks++
                        if (callbacks == callbacksExpected) {
                            listener?.onResult(true)
                        }
                    }
                })
            }
            if (TrackerPreferences.config(context).useHeartRateMonitor) {
                HeartRatesUploader.uploadData(context, object : ResultListener {
                    override fun onResult(success: Boolean) {
                        callbacks++
                        if (callbacks == callbacksExpected) {
                            listener?.onResult(true)
                        }
                    }
                })
            }
            if (TrackerPreferences.config(context).useMobilityModelling) {
                TripsUploader.uploadData(context, object : ResultListener {
                    override fun onResult(success: Boolean) {
                        callbacks++
                        if (callbacks == callbacksExpected) {
                            listener?.onResult(true)
                        }
                    }
                })
            }
        } else {
            listener?.onResult(true)
        }
    }

    /**
     * This declares the worker foreground info
     */
    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        return CallbackToFutureAdapter.getFuture { completer ->
            TrackerNotification.notificationText = "Do not close the app, please"
            TrackerNotification.notificationIcon = R.drawable.ic_notification
            val notification = TrackerNotification(context, NOTIFICATION_ID, true)
            completer.set(ForegroundInfo(NOTIFICATION_ID, notification.notification!!))
        }
    }
}

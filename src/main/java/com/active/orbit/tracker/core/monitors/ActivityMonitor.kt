package com.active.orbit.tracker.core.monitors

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.viewbinding.BuildConfig
import com.active.orbit.tracker.core.database.models.DBActivity
import com.active.orbit.tracker.core.database.tables.TableActivities
import com.active.orbit.tracker.core.tracker.TrackerService
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.TimeUtils
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class ActivityMonitor(private var callingService: TrackerService) {

    private var activityTransitionsReceiver: ActivityTransitionsReceiver? = null
    var activitiesList: MutableList<DBActivity> = mutableListOf()

    /**
     *  The intent action triggered by the identification of a transition
     */
    private val transitionsReceiverAction = BuildConfig.LIBRARY_PACKAGE_NAME + "TRANSITIONS_RECEIVER_ACTION"
    private var arPendingIntent: PendingIntent

    companion object {
        fun getTransitionTypeString(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTERING"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXITING"
                else -> "--"
            }
        }

        private var STANDARD_MAX_SIZE = 10
        private var MAX_SIZE = STANDARD_MAX_SIZE
        private const val REQUEST_CODE = 0
    }

    init {
        val intent = Intent(transitionsReceiverAction)
        arPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(callingService, REQUEST_CODE, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(callingService, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    /**
     * API called by the Tracker Service to start A/R
     */
    fun startActivityRecognition(context: Context) {
        Logger.d("Starting A/R")
        activityTransitionsReceiver = registerARReceiver(callingService, activityTransitionsReceiver)
        setupActivityTransitions(context)
    }


    /**
     * Called by the main service to stop activity recognition
     * @param context the calling context
     */
    fun stopActivityRecognition(context: Context) {
        // Unregister the transitions:
        if (ActivityCompat.checkSelfPermission(callingService, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityRecognition.getClient(context).removeActivityTransitionUpdates(arPendingIntent)
            .addOnFailureListener { e -> Logger.e("Transitions Updates could not be unregistered: ${e.localizedMessage}") }
            .addOnSuccessListener { Logger.d("Transitions Updates successfully unregistered.") }
        if (activityTransitionsReceiver != null) {
            context.unregisterReceiver(activityTransitionsReceiver)
            activityTransitionsReceiver = null
        }
        flushToDatabase()
    }

    /**
     * This writes the remaining activities to the database
     */
    private fun flushToDatabase() {
        backgroundThread {
            TableActivities.upsert(callingService, activitiesList)
            activitiesList = mutableListOf()
        }
    }

    /**
     * This flushes the activities stored in activityDataList into the database
     */
    fun flush(context: Context) {
        backgroundThread {
            TableActivities.upsert(context, activitiesList)
            activitiesList = mutableListOf()
        }
    }

    fun keepFlushingToDB(context: Context, flush: Boolean) {
        MAX_SIZE = if (flush) {
            flush(context)
            0
        } else
            STANDARD_MAX_SIZE
        Logger.d("Flushing activities? $flush")
    }


    /**
     * Sets up [ActivityTransitionRequest]'s for the sample app, and registers callbacks for them
     * with a custom [BroadcastReceiver]
     * ---  UNKNOWN and TILTING are unsupported in the Transition API
     */
    private fun setupActivityTransitions(context: Context) {
        val request = ActivityTransitionRequest(getTransitionsOfInterest())
        // registering for incoming transitions updates.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            Logger.d("Recognition permissions allowed")
        }
        ActivityRecognition.getClient(context).requestActivityTransitionUpdates(request, arPendingIntent)
        // task.addOnSuccessListener { Logger.d("Transitions  successfully registered.") }
        // task.addOnFailureListener { e: Error -> Logger.e("Error in Transition Registration: $e") }
    }

    /**
     * This lists all the transitions that are on interest. We are interested in any relevant activity
     */
    private fun getTransitionsOfInterest(): MutableList<ActivityTransition> {
        val transitionsList: MutableList<ActivityTransition> = ArrayList()
        val typesOfInterestList = listOf(
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.STILL
            // not supported:
            // DetectedActivity.UNKNOWN,
            // DetectedActivity.TILTING
        )
        for (type in typesOfInterestList) {
            transitionsList.add(
                ActivityTransition.Builder()
                    .setActivityType(type)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            )
            transitionsList.add(
                ActivityTransition.Builder()
                    .setActivityType(type)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            )
        }
        return transitionsList
    }


    /**
     * This registers the transitions receiver
     */
    private fun registerARReceiver(
        context: Context,
        activityTransitionsReceiver: ActivityTransitionsReceiver?
    ): ActivityTransitionsReceiver {
        if (activityTransitionsReceiver != null) {
            // remove it just in case
            try {
                context.unregisterReceiver(activityTransitionsReceiver)
            } catch (e: Exception) {
                Logger.e("Error in registering the receiver: " + e.message)
            }
        }
        val newActivityTransitionsReceiver = ActivityTransitionsReceiver()
        context.registerReceiver(
            newActivityTransitionsReceiver,
            IntentFilter(transitionsReceiverAction)
        )
        return newActivityTransitionsReceiver
    }

    /**
     * A  BroadcastReceiver handling intents from the Transitions API
     */
    inner class ActivityTransitionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!TextUtils.equals(transitionsReceiverAction, intent.action)) {
                Logger.d("Unsupported action in ActivityTransitionsReceiver: " + intent.action)
                return
            }
            if (ActivityTransitionResult.hasResult(intent)) {
                val transitionsResults = ActivityTransitionResult.extractResult(intent)
                val transitionsEvents = transitionsResults?.transitionEvents
                if (transitionsEvents != null) {
                    for (transitionEvent in transitionsEvents) {
                        // there is a discussion saying that callbacks are received for old activities when the receiver is registered
                        // https://stackoverflow.com/questions/50257943/the-first-transition-is-always-the-same-activity-recognition-api-activity-tr
                        // typically it is just the current open activity. Check that this is the case and you do not get wring activities
                        val transitionType = transitionEvent.transitionType
                        transitionEvent.elapsedRealTimeNanos
                        val eventTimeInMsecs = TimeUtils.fromEventTimeToEpoch(transitionEvent.getElapsedRealTimeNanos())
                        Logger.d("Transition: " + getActivityType(transitionEvent.activityType) + " (" + getTransitionTypeString(transitionType) + ")" + "   " + SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()))
                        // insert the  activity into the db
                        val dbActivity = DBActivity()
                        dbActivity.activityType = transitionEvent.activityType
                        dbActivity.transitionType = transitionType
                        dbActivity.timeInMillis = eventTimeInMsecs
                        activitiesList.add(dbActivity)
                        if (activitiesList.size > MAX_SIZE) {
                            flushToDatabase()
                        }
                        callingService.currentActivity(dbActivity)
                    }
                }
            }
        }
    }

    private fun getActivityType(intActivity: Int): String {
        return when (intActivity) {
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }
}
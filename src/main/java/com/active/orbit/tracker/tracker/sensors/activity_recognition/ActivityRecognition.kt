package com.active.orbit.tracker.tracker.sensors.activity_recognition

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.viewbinding.BuildConfig
import com.active.orbit.tracker.tracker.TrackerService
import com.active.orbit.tracker.utils.Utils
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityRecognition
import java.text.SimpleDateFormat
import java.util.*

class ActivityRecognition(private var callingService: TrackerService) {

    private val TAG = javaClass.simpleName
    private var activityTransitionsReceiver: ActivityTransitionsReceiver? = null
    var activityDataList: MutableList<ActivityData> = mutableListOf()

    /**
     *  The intent action triggered by the identification of a transition
     */
    private val TRANSITIONS_RECEIVER_ACTION = BuildConfig.LIBRARY_PACKAGE_NAME + "TRANSITIONS_RECEIVER_ACTION"
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
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        arPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(callingService, REQUEST_CODE, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                callingService,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    //////////////////////////////////////////////////////////////////////
    //                      api functions
    //////////////////////////////////////////////////////////////////////
    /**
     * API called by the Tracker Service to start A/R
     */
    fun startActivityRecognition(context: Context) {
        Log.i(TAG, "Starting A/R")
        activityTransitionsReceiver = registerARReceiver(callingService, activityTransitionsReceiver)
        setupActivityTransitions(context)
    }


    /**
     * called by the main service to stop activity recognition
     * @param context the calling context
     */
    fun stopActivityRecognition(context: Context) {
        // Unregister the transitions:
        if (ActivityCompat.checkSelfPermission(
                callingService,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityRecognition.getClient(context).removeActivityTransitionUpdates(arPendingIntent)
            .addOnFailureListener { e ->
                Log.e(
                    TAG,
                    "Transitions Updates could not be unregistered: $e"
                )
            }
            .addOnSuccessListener { Log.i(TAG, "Transitions Updates successfully unregistered.") }
        if (activityTransitionsReceiver != null) {
            context.unregisterReceiver(activityTransitionsReceiver)
            activityTransitionsReceiver = null
        }
        flushToDatabase()
    }

    /**
     * it writes the remaining activities to the database
     */
    private fun flushToDatabase() {
        Handler(Looper.getMainLooper()).postDelayed({
            InsertActivityDataAsync(callingService, activityDataList)
            activityDataList = mutableListOf()
        }, 1000)
    }

    /**
     * it flushes the activities stored in activityDataList into the database
     */
    fun flush(context: Context) {
        InsertActivityDataAsync(context, activityDataList)
        activityDataList = mutableListOf()
    }

    fun keepFlushingToDB(context: Context, flush: Boolean) {
        MAX_SIZE = if (flush) {
            flush(context)
            0
        } else
            STANDARD_MAX_SIZE
        Log.i(TAG, "flushing activities? $flush")
    }


    //////////////////////////////////////////////////////////////////////
    //                     internal methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Sets up [ActivityTransitionRequest]'s for the sample app, and registers callbacks for them
     * with a custom [BroadcastReceiver]
     * ---  UNKNOWN and TILTING are unsupported in the Transition API
     */
    private fun setupActivityTransitions(context: Context) {
        val request = ActivityTransitionRequest(getTransitionsOfInterest())
        // registering for incoming transitions updates.
        val task = if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            Log.i(TAG, "no permissions allowed")
        }
        ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(request, arPendingIntent)
//        task.addOnSuccessListener { Log.i(TAG, "Transitions  successfully registered.") }
//        task.addOnFailureListener { e: Error -> Log.e(TAG, "Error in Transition Registration: $e") }
    }

    /**
     * it lists all the transitions that are on interest. We are interested in any relevant activity
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
     * it registers the transitions receiver
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
                Log.e(TAG, "error in registering the receiver: " + e.message)
            }
        }
        val newActivityTransitionsReceiver = ActivityTransitionsReceiver()
        context.registerReceiver(
            newActivityTransitionsReceiver,
            IntentFilter(TRANSITIONS_RECEIVER_ACTION)
        )
        return newActivityTransitionsReceiver
    }

    /**
     * A  BroadcastReceiver handling intents from the Transitions API
     */
    inner class ActivityTransitionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.action)) {
                Log.i(
                    TAG, "Unsupported action in ActivityTransitionsReceiver: "
                            + intent.action
                )
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
                        val eventTimeInMsecs =
                            Utils.fromEventTimeToEpoch(transitionEvent.getElapsedRealTimeNanos())
                        Log.i(
                            TAG, "Transition: "
                                    + getActivityType(transitionEvent.activityType) + " (" + getTransitionTypeString(
                                transitionType
                            ) + ")" + "   "
                                    + SimpleDateFormat("HH:mm:ss", Locale.US)
                                .format(Date())
                        )
                        // insert the  activity into the db
                        val activityData =
                            ActivityData(
                                eventTimeInMsecs,
                                transitionEvent.activityType,
                                transitionType
                            )
                        activityDataList.add(activityData)
                        if (activityDataList.size > MAX_SIZE) {
                            flushToDatabase()
                        }
                        callingService.currentActivity(activityData)
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
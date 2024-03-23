/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.tracker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.content.UnusedAppRestrictionsConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.database.engine.TrackerDatabase
import uk.ac.shef.tracker.core.database.tables.*
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.managers.TrackerUserManager
import uk.ac.shef.tracker.core.preferences.engine.TrackerBasePreferences
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.restarter.TrackerRestarter
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.background
import kotlin.coroutines.CoroutineContext

/**
 * This class exposes useful method for the tracker management
 *
 * @param activity an instance of [AppCompatActivity]
 */
class TrackerManager private constructor(private val activity: AppCompatActivity) {

    /**
     * This is the tracker reference time
     */
    var currentDateTime: Long = System.currentTimeMillis()

    companion object : CoroutineScope {

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Default

        @Volatile
        private var instance: TrackerManager? = null

        /**
         * Get the singleton instance of the [TrackerManager]
         *
         * @param activity an instance of the [AppCompatActivity]
         * @return the singleton instance of the [TrackerManager]
         */
        @Synchronized
        fun getInstance(activity: AppCompatActivity): TrackerManager {
            if (instance == null) {
                synchronized(TrackerManager::class.java) {
                    // double check locking
                    if (instance == null)
                        instance = TrackerManager(activity)
                }
            }
            instance?.initialize()
            return instance!!
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE: Int = 1012
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE: Int = 1013
        private const val REQUEST_BODY_SENSOR_REQUEST_CODE: Int = 1014
        private const val REQUEST_UNUSED_RESTRICTIONS_REQUEST_CODE: Int = 983724

        /**
         * This is a debuggable method to print the size of the tracker database content
         */
        fun printInformationLogs(context: Context) {
            background {
                Logger.i("------------------------------------------")
                Logger.i("Activities -> ${TrackerTableActivities.getAll(context).size}")
                Logger.i("Batteries  -> ${TrackerTableBatteries.getAll(context).size}")
                Logger.i("HeartRates -> ${TrackerTableHeartRates.getAll(context).size}")
                Logger.i("Locations  -> ${TrackerTableLocations.getAll(context).size}")
                Logger.i("Steps      -> ${TrackerTableSteps.getAll(context).size}")
                Logger.i("Trips      -> ${TrackerTableTrips.getAll(context).size}")
                Logger.i("------------------------------------------")

                TrackerBasePreferences.printAll(context)
            }
        }
    }

    private fun initialize() {
        // insert initialisation code if needed..
    }

    fun onResume() {
        val userId = TrackerPreferences.user(activity).idUser ?: Constants.EMPTY
        if (userId!=Constants.EMPTY) {
            Log.i("user ID", "userId: $userId")
            val trackerRestarter = TrackerRestarter()
            Logger.i("Starting tracker")
            trackerRestarter.startTrackerAndDataUpload(activity)
            Logger.i("Started tracker")

        }
        TrackerService.currentTracker?.keepFlushingToDB(true)
    }

    fun onPause() {
        TrackerService.currentTracker?.keepFlushingToDB(false)
    }

    /**
     * This starts the tracker and it should be used when the client app manages the required permissions by itself
     *
     * API: all implementations must request this after setting their preferences on which modules
     * to use
     *
     * @see askForPermissionAndStartTracker
     */
    fun startTracker(config: TrackerConfig) {
        setupWith(config)
        startTracker()
    }

    /**
     * API: all implementations must request this after setting their preferences on which modules
     * to use
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun askForPermissionAndStartTracker(config: TrackerConfig) {
        setupWith(config)
        askForPermissionAndStartTrackerAux()
    }

    private fun setupWith(config: TrackerConfig) {
        TrackerPreferences.backend(activity).baseUrl = config.baseUrl
        TrackerPreferences.config(activity).useActivityRecognition = config.useActivityRecognition
        TrackerPreferences.config(activity).useLocationTracking = config.useLocationTracking
        TrackerPreferences.config(activity).useStepCounter = config.useStepCounter
        TrackerPreferences.config(activity).useHeartRateMonitor = config.useHeartRateMonitor
        TrackerPreferences.config(activity).useMobilityModelling = config.useMobilityModelling
        TrackerPreferences.config(activity).useBatteryMonitor = config.useBatteryMonitor
        TrackerPreferences.config(activity).useStayPoints = config.useStayPoints
        TrackerPreferences.config(activity).compactLocations = config.compactLocations
        TrackerPreferences.backend(activity).uploadData = config.uploadData
    }

    /**
     * This asks for permissions (e.g. body sensors) and then starts the tracker
     */
    private fun askForPermissionAndStartTrackerAux() {
        if (TrackerPreferences.config(activity).useLocationTracking && !hasLocationPermissionsBeenGranted()) {
            Logger.i("Requesting location permissions")
            requestPermissionLocation()
        } else if (TrackerPreferences.config(activity).useActivityRecognition && !hasActivityRecognitionPermissionsBeenGranted()) {
            Logger.i("Requesting A/R permissions")
            requestPermissionActivityRecognition()
        } else if (TrackerPreferences.config(activity).useHeartRateMonitor && !hasBodySensorPermissionsBeenGranted()) {
            Logger.i("Requesting Body Sensor permissions")
            requestPermissionBodySensor()
        } else {
            onboardedUnusedRestrictions(activity, object : ResultListener {
                override fun onResult(success: Boolean) {
                    if (success) {
                        startTracker()
                    } else {
                        requestUnusedRestrictions(activity)
                    }
                }
            })
        }
    }

    /**
     * This starts the tracker
     */
    private fun startTracker() {
        // we have to force restart because it will have started without permission and it
        // is being blocked
        val trackerRestarter = TrackerRestarter()
        trackerRestarter.startTrackerAndDataUpload(activity)
    }

    /**
     * This stops the tracker
     */
    private fun stopTracker() {
        // stop the tracker if running
        try {
            Logger.i("Stopping the tracker service")
            TrackerService.currentTracker?.stopSelf()
        } catch (e: Exception) {
            Logger.i("Error stopping the tracker")
        }
    }

    /**
     * This requests the location to track permission as fine and in the background
     */
    private fun requestPermissionLocation() {
        val permissions = getRequestLocationPermissions()
        ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_REQUEST_CODE)
    }

    /**
     * This gets the list of permissions for locations
     * the way it does it depends on Build.VERSION.SDK_INT.
     *      - For R it will require both fine and background one at a tim because background uses the settings
     *      - for Q will require also both but together
     *      - other wise just fine is requested
     */
    private fun getRequestLocationPermissions(): Array<String?> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * This requests the activity recognition permission if needed
     */
    private fun requestPermissionActivityRecognition(): Boolean {
        val permissions = getRequestActivityRecognitionPermissions()
        return if (permissions != null) {
            ActivityCompat.requestPermissions(activity, permissions, ACTIVITY_RECOGNITION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

    /**
     * This returns the activity recognition permission only if available according to the device apis level
     */
    private fun getRequestActivityRecognitionPermissions(): Array<String?>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
        } else null
    }

    /**
     * This requests the body sensors permission
     */
    private fun requestPermissionBodySensor() {
        val permissions = getRequestBodySensorPermissions()
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_BODY_SENSOR_REQUEST_CODE)
    }

    /**
     * This returns the body sensors permission
     */
    private fun getRequestBodySensorPermissions(): Array<String?> {
        return arrayOf(Manifest.permission.BODY_SENSORS)
    }

    /**
     * This checks if the background location permission has been granted
     */
    private fun backgroundLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    /**
     * This will process the result of a location permission request
     *
     * @param resultCode the [Int] result code
     */
    private fun processLocationRequest(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            onLocationPermissionAccepted()
        } else if (resultCode == Activity.RESULT_CANCELED) {

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            // TODO
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACTIVITY_RECOGNITION)) {
            // TODO
        } else {
            // TODO
        }
    }

    /**
     * This will process the result of a activity recognition request
     *
     * @param resultCode the [Int] result code
     */
    private fun processActivityRecognitionRequest(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            onActivityRecognitionPermissionAccepted()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACTIVITY_RECOGNITION)) {
                // TODO
            } else {
                // activity recognition refused forever
                // TODO
            }
        }
    }

    /**
     * This will process the result of a body sensor request
     *
     * @param resultCode the [Int] result code
     */
    private fun processBodySensorRequest(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            onBodySensorPermissionAccepted()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // TODO
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACTIVITY_RECOGNITION)) {
                // TODO
            } else {
                // activity recognition refused forever
                // TODO
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val resultCode = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) Activity.RESULT_OK else Activity.RESULT_CANCELED
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> processLocationRequest(resultCode)
            ACTIVITY_RECOGNITION_REQUEST_CODE -> processActivityRecognitionRequest(resultCode)
            REQUEST_BODY_SENSOR_REQUEST_CODE -> processBodySensorRequest(resultCode)
            REQUEST_UNUSED_RESTRICTIONS_REQUEST_CODE -> askForPermissionAndStartTrackerAux()
        }
    }

    /**
     * This is called when the activity recognition permission has been granted
     */
    private fun onActivityRecognitionPermissionAccepted() {
        askForPermissionAndStartTrackerAux()
    }

    /**
     * This is called when the body sensors permission has been granted
     */
    private fun onBodySensorPermissionAccepted() {
        askForPermissionAndStartTrackerAux()
    }

    /**
     * This is called when the location permission has been granted
     */
    private fun onLocationPermissionAccepted() {
        if (!backgroundLocationPermissionGranted())
            requestPermissionLocation()
        else askForPermissionAndStartTrackerAux()
    }

    /**
     * This checks if the location permission has been already granted
     */
    private fun hasLocationPermissionsBeenGranted(): Boolean {
        val permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return permissionGranted && backgroundLocationPermissionGranted()
        }
        return permissionGranted
    }

    /**
     * This checks if the activity recognition has been already granted
     */
    private fun hasActivityRecognitionPermissionsBeenGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    /**
     * This checks if the body sensors permission has been already granted
     */
    private fun hasBodySensorPermissionsBeenGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This checks if the unused restrictions has been disabled
     *
     * @param activity an instance of [AppCompatActivity]
     * @param listener the callback to have the async result
     */
    private fun onboardedUnusedRestrictions(activity: AppCompatActivity, listener: ResultListener) {
        val future = PackageManagerCompat.getUnusedAppRestrictionsStatus(activity)
        future.addListener({
            when (future.get()) {
                UnusedAppRestrictionsConstants.ERROR -> listener.onResult(true)
                UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE -> listener.onResult(true)
                UnusedAppRestrictionsConstants.DISABLED -> listener.onResult(true)
                UnusedAppRestrictionsConstants.API_30_BACKPORT, UnusedAppRestrictionsConstants.API_30, UnusedAppRestrictionsConstants.API_31 -> listener.onResult(false)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    /**
     * This will request to remove the unused restrictions
     *
     * @param activity an instance of [AppCompatActivity]
     */
    fun requestUnusedRestrictions(activity: AppCompatActivity) {
        if (!activity.isDestroyed) {
            AlertDialog.Builder(activity).setTitle(activity.getString(R.string.disable_restrictions))
                .setMessage(activity.getString(R.string.disable_restrictions_description))
                .setPositiveButton(activity.getString(R.string.go)) { _, _ ->
                    // If your app works primarily in the background, you can ask the user
                    // to disable these restrictions. Check if you have already asked the
                    // user to disable these restrictions. If not, you can show a message to
                    // the user explaining why permission auto-reset and Hibernation should be
                    // disabled. Tell them that they will now be redirected to a page where
                    // they can disable these features.
                    val intent = IntentCompat.createManageUnusedAppRestrictionsIntent(activity, activity.packageName)
                    // Must use startActivityForResult(), not startActivity(), even if
                    // you don't use the result code returned in onActivityResult().
                    @Suppress("DEPRECATION")
                    activity.startActivityForResult(intent, REQUEST_UNUSED_RESTRICTIONS_REQUEST_CODE)
                }.show().setCancelable(false)
        }
    }


    /**
     * This should be called when the user logout
     */
    @WorkerThread
    fun logout(context: Context) {
        stopTracker()
        val utils= TrackerUtils()
        utils.saveUserRegistrationId(activity, null)
        TrackerBasePreferences.logout(activity)
        TrackerDatabase.getInstance(context).logout()
    }
}

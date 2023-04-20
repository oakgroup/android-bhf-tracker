package com.active.orbit.tracker

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
import com.active.orbit.tracker.data_upload.UserRegistration
import com.active.orbit.tracker.database.MyRoomDatabase
import com.active.orbit.tracker.listeners.ResultListener
import com.active.orbit.tracker.restarter.RestartTrackerBroadcastReceiver
import com.active.orbit.tracker.restarter.TrackerRestarter
import com.active.orbit.tracker.tracker.TrackerService
import com.active.orbit.tracker.utils.Globals
import com.active.orbit.tracker.utils.PreferencesStore

class TrackerManager private constructor(private val activity: AppCompatActivity) {

    private var useBodySensors: Boolean? = false
    private var useLocationTracking: Boolean? = false
    private var useActivityRecognition: Boolean? = false
    private var useStepCounter: Boolean = false
    private var useMobilityModelling: Boolean = false
    private var sendData: Boolean = false
    var currentDateTime: Long = System.currentTimeMillis()

    companion object {

        @Volatile
        private var instance: TrackerManager? = null

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

        val TAG = this::class.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE: Int = 1012
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE: Int = 1013
        private const val REQUEST_BODY_SENSOR_REQUEST_CODE: Int = 1014
        private const val REQUEST_UNUSED_RESTRICTIONS_REQUEST_CODE: Int = 983724
    }

    private fun initialize() {

    }

    fun onResume(viewModel: MyViewModel) {
        Log.i(TAG, "flushing to db and keeping it open")
        viewModel.keepFlushingToDB(true)
    }

    fun onPause(viewModel: MyViewModel) {
        Log.i(TAG, "flushing all data")
        viewModel.keepFlushingToDB(false)
    }

    /**
     * API: all implementations must request this after setting their preferences on which modules
     * to use
     *
     */
    fun askForPermissionAndStartTracker(useStepCounter: Boolean, useActivityRecognition: Boolean, useLocationTracking: Boolean, useBodySensors: Boolean, useMobilityModelling: Boolean, sendData: Boolean) {
        this.useActivityRecognition = useActivityRecognition
        this.useLocationTracking = useLocationTracking
        this.useBodySensors = useBodySensors
        this.useStepCounter = useStepCounter
        this.useMobilityModelling = useMobilityModelling
        this.sendData = sendData
        savePreferences(useStepCounter, useActivityRecognition, useLocationTracking, useBodySensors, useMobilityModelling, sendData)
        askForPermissionAndStartTrackerAux()
    }

    /**
     * it sets the behaviour of the tracker and the data sending (if any)
     *
     * @param useStepCounter
     * @param useActivityRecognition
     * @param useLocationTracking
     * @param useBodySensors
     * @param sendData
     */
    private fun savePreferences(useStepCounter: Boolean, useActivityRecognition: Boolean, useLocationTracking: Boolean, useBodySensors: Boolean, useMobilityModelling: Boolean, sendData: Boolean) {
        val preference = PreferencesStore()
        preference.setBooleanPreference(activity, Globals.USE_ACTIVITY_RECOGNITION, useActivityRecognition)
        preference.setBooleanPreference(activity, Globals.USE_LOCATION_TRACKING, useLocationTracking)
        preference.setBooleanPreference(activity, Globals.USE_STEP_COUNTER, useStepCounter)
        preference.setBooleanPreference(activity, Globals.USE_HEART_RATE_MONITOR, useBodySensors)
        preference.setBooleanPreference(activity, Globals.USE_MOBILITY_MODELLING, useMobilityModelling)
        preference.setBooleanPreference(activity, Globals.SEND_DATA_TO_SERVER, sendData)
    }

    /**
     * it asks for permissions (e.g. body sensors) and then starts the tracker
     *
     */
    private fun askForPermissionAndStartTrackerAux() {
        if (useLocationTracking!! && !hasLocationPermissionsBeenGranted()) {
            Log.i(TAG, "requesting location permissions")
            requestPermissionLocation()
        } else if (useActivityRecognition!! && !hasActivityRecognitionPermissionsBeenGranted()) {
            Log.i(TAG, "requesting A/R permissions")
            requestPermissionActivityRecognition()
        } else if (useBodySensors!! && !hasBodySensorPermissionsBeenGranted()) {
            Log.i(TAG, "requesting Body Sensor permissions")
            requestPermissionBodySensor()
//        } else if (!hasPowerSettingsPermissionBeenGranted(this)) {
//            // Open the corresponding Power Saving Settings
//            AutoStartHelper().getAutoStartPermission(this)
//            val prefs= PreferencesStore()
//            prefs.setBooleanPreference(this, Globals.POWER_PREFERENCES_GRANTED, true)
//        } else if (!hasPowerSettingsPermission2BeenGranted(this)) {
//            // Open the corresponding Power Saving Settings
//            if(AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this)) {
//                val batteryPermissionsHelper = BatteryPermissionsHelper()
//                batteryPermissionsHelper.getBatteryPermission(this)
//            }
//            val prefs= PreferencesStore()
//            prefs.setBooleanPreference(this, Globals.POWER_PREFERENCES_2_GRANTED, true)
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

    private fun startTracker() {
        // we have to force restart because it will have started without permission and it
        // is being blocked
        val trackerRestarter = TrackerRestarter()
        trackerRestarter.startTrackerAndDataUpload(activity)
    }

    private fun stopTracker() {
        // stop the tracker if running
        try {
            Log.i(TAG, "Stopping the tracker service")
            TrackerService.currentTracker?.stopSelf()
        } catch (e: Exception) {
            Log.i(TAG, "Error stopping the tracker")
        }
    }

    /**
     * it requests the location to track permission as fine and in the background
     */
    private fun requestPermissionLocation() {
        val permissions = getRequestLocationPermissions()
        ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_REQUEST_CODE)
    }

    /**
     * it gets the list of permissions for locations
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

    private fun requestPermissionActivityRecognition(): Boolean {
        val permissions = getRequestActivityRecognitionPermissions()
        return if (permissions != null) {
            ActivityCompat.requestPermissions(activity, permissions, ACTIVITY_RECOGNITION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

    private fun getRequestActivityRecognitionPermissions(): Array<String?>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
        } else null
    }

    private fun requestPermissionBodySensor(): Boolean {
        val permissions = getRequestBodySensorPermissions()
        return run {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_BODY_SENSOR_REQUEST_CODE)
            false
        }
    }

    private fun getRequestBodySensorPermissions(): Array<String?> {
        return arrayOf(Manifest.permission.BODY_SENSORS)
    }

    private fun backgroundLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true
    }


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

    private fun onActivityRecognitionPermissionAccepted() {
        askForPermissionAndStartTrackerAux()
    }

    private fun onBodySensorPermissionAccepted() {
        askForPermissionAndStartTrackerAux()
    }

    private fun onLocationPermissionAccepted() {
        if (!backgroundLocationPermissionGranted())
            requestPermissionLocation()
        else askForPermissionAndStartTrackerAux()
    }

    private fun hasLocationPermissionsBeenGranted(): Boolean {
        val permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return permissionGranted && backgroundLocationPermissionGranted()
        }
        return permissionGranted
    }

    private fun hasActivityRecognitionPermissionsBeenGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun hasPowerSettingsPermission2BeenGranted(): Boolean {
        val prefs = PreferencesStore()
        return prefs.getBooleanPreference(activity, Globals.POWER_PREFERENCES_2_GRANTED, false)!!
    }

    private fun hasPowerSettingsPermissionBeenGranted(): Boolean {
        val prefs = PreferencesStore()
        return prefs.getBooleanPreference(activity, Globals.POWER_PREFERENCES_GRANTED, false)!!
    }

    private fun hasBodySensorPermissionsBeenGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
    }

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
     * if the user is not registered with the server, it registers it
     */
    fun checkUserRegistration(viewModel: MyViewModel) {
        val userPreferences = PreferencesStore()
        val userId = userPreferences.getStringPreference(activity, Globals.USER_ID, "")
//        userPreferences.setStringPreference(activity, Globals.USER_ID, "")
        if (userId == "") {
            Log.i(RestartTrackerBroadcastReceiver.TAG, "Registering user...")
            UserRegistration(activity, viewModel)
        } else Log.i(RestartTrackerBroadcastReceiver.TAG, "User already registered with id $userId")
    }

    fun saveUserRegistrationId(userId: String?) {
        val userPreferences = PreferencesStore()
        userPreferences.setStringPreference(activity, Globals.USER_ID, userId)
    }

    @WorkerThread
    fun logout(context: Context) {
        stopTracker()
        saveUserRegistrationId(null)
        val db = MyRoomDatabase.getDatabase(context)
        db?.logout()
    }
}

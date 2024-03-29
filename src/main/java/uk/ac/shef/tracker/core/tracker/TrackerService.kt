/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.tracker

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.*
import android.os.PowerManager.WakeLock
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.Runnable
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.monitors.*
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.utils.LocationUtilities
import uk.ac.shef.tracker.core.utils.Logger

/**
 * This is the tracker service that runs in background
 */
class TrackerService : Service() {

    private lateinit var updateTimerThread: Runnable
    private var wakeLockHandler: Handler? = null
    private var savedLocation: Location? = null
    private var isContDownTimerRunning: Boolean = false

    // https://developer.android.com/guide/topics/data/audit-access
    // from android 11 the sensors need an attribution tag declared in teh manifest and
    // used when accessing the sensor manager to create a sensor
    lateinit var attributionContext: Context

    var countDownTimer: CountDownTimer? = null

    var activityRecognition: ActivityMonitor? = null
    var batteryMonitor: BatteryMonitor? = null
    var heartMonitor: HeartRateMonitor? = null
    var locationTracker: LocationMonitor? = null
    var stepCounter: StepMonitor? = null

    private var significantMotionSensor: SignificantMotionMonitor? = null

    private var currentTrackerNotification: TrackerNotification? = null

    private var wakeLock: WakeLock? = null

    companion object {
        var currentTracker: TrackerService? = null
        private const val NOTIFICATION_ID = 9974
    }

    /**
     * This creates the tracker service according to the configurations
     */
    override fun onCreate() {
        super.onCreate()
        Logger.d("Creating the Tracker Service!! $this")
        currentTracker = this

        attributionContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createAttributionContext("data_collection_attribution_tag")
        } else
            this
        // This initialises the sensor trackers and the repository before starting the foreground process
        // We do it in the onCreate so to avoid calling this every time the intent is re-delivered
        val useStepCounter = TrackerPreferences.config(this).useStepCounter
        val useActivityRecognition = TrackerPreferences.config(this).useActivityRecognition
        val useLocationTracking = TrackerPreferences.config(this).useLocationTracking
        val useHeartRateMonitoring = TrackerPreferences.config(this).useHeartRateMonitor
        val useBatteryMonitoring = TrackerPreferences.config(this).useBatteryMonitor

        if (locationTracker == null && useLocationTracking)
            locationTracker = LocationMonitor(attributionContext)
        if (activityRecognition == null && useActivityRecognition)
            activityRecognition = ActivityMonitor(currentTracker!!, attributionContext)
        if (stepCounter == null && useStepCounter)
            stepCounter = StepMonitor(attributionContext)
        if (heartMonitor == null && useHeartRateMonitoring) {
            heartMonitor = HeartRateMonitor(attributionContext)
            if (heartMonitor?.monitorIsAvailable() == false)
                heartMonitor=null
        }
        if (batteryMonitor == null && useBatteryMonitoring)
            batteryMonitor = BatteryMonitor(attributionContext)

        initCountDownToStoppingSensors()
    }


    /**
     * This starts the foreground process
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Logger.d("Starting the foreground service...")
        startWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Logger.d("Starting foreground process")
                currentTrackerNotification = TrackerNotification(this, NOTIFICATION_ID, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(NOTIFICATION_ID, currentTrackerNotification!!.notification!!, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                } else {
                    startForeground(NOTIFICATION_ID, currentTrackerNotification!!.notification)
                }
                Logger.d("Starting foreground process successful!")
            } catch (e: Exception) {
                Logger.e("Error starting foreground process " + e.localizedMessage)
            }
        }
        startTrackers()
        return START_REDELIVER_INTENT
    }

    /**
     * This acquires the wakelock
     */
    @SuppressLint("WakelockTimeout")
    fun startWakeLock() {
        if (wakeLock?.isHeld == true)
                    wakeLockHandler?.removeCallbacks(updateTimerThread)

        Logger.d("Acquiring the wakelock")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.simpleName)
        wakeLock?.acquire()
    }

    /**
     *  This starts the sensors
     */
    fun startTrackers() {
        Logger.d("Starting trackers...")
        try {
            Logger.d("Starting step counting ${stepCounter != null}")
            stepCounter?.startStepCounting()
        } catch (e: Exception) {
            Logger.e("Error starting the step counter: " + e.localizedMessage)
        }
        try {
            Logger.d("Starting A/R ${activityRecognition != null}")
            activityRecognition?.startActivityRecognition(this)
        } catch (e: Exception) {
            Logger.e("Error starting  A/R: " + e.localizedMessage)
        }
        try {
            Logger.d("Starting location tracking ${locationTracker != null}")
            locationTracker?.startLocationTracking(this)
        } catch (e: Exception) {
            Logger.e("Error starting the location tracker: " + e.localizedMessage)
        }
        try {
            Logger.d("Starting heart rate monitoring ${heartMonitor != null}")
            heartMonitor?.startMonitoring()
        } catch (e: Exception) {
            Logger.e("Error starting the hr monitor: " + e.localizedMessage)
        }
        try {
            Logger.d("Starting battery monitoring ${batteryMonitor != null}")
            batteryMonitor?.insertData()
        } catch (e: Exception) {
            Logger.e("Error starting the battery monitor: " + e.localizedMessage)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        flushDataToDB()
        Logger.d("TrackerService OnDestroy")
        stopSensors()
        wakeLockHandler?.removeCallbacks(updateTimerThread)
        try {
            wakeLock?.release()
        } catch (e: Exception) {
            Logger.e("Exception releasing the wake lock from onDestroy")
        }
        currentTracker = null
    }

    /**
     * This stops all the sensors - it also flushes everything to the database
     */
    private fun stopSensors() {
        Logger.d("Stopping sensors")
        try {
            stepCounter?.stopStepCounting()
        } catch (e: Exception) {
            Logger.w("Step counter failed to stop" + e.localizedMessage)
        }
        try {
            heartMonitor?.stopMonitor()
        } catch (e: Exception) {
            Logger.w("HR monitor did not stop" + e.localizedMessage)
        }
        try {
            activityRecognition?.stopActivityRecognition(this)
        } catch (e: Exception) {
            Logger.w("A/R did not stop " + e.localizedMessage)
        }
        try {
            locationTracker?.stopLocationTracking()
        } catch (e: Exception) {
            Logger.w("Location tracker did not stop " + e.localizedMessage)
        }
    }

    private fun initCountDownToStoppingSensors() {
        if (countDownTimer == null) {
            countDownTimer = object : CountDownTimer(600000, 600000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    val locationUtils = LocationUtilities()
                    if (locationTracker != null && locationUtils.computeDistance(locationTracker!!.currentLocation, savedLocation) < 100) {
                        stopSensors()
                        flushDataToDB()
                        significantMotionSensor = SignificantMotionMonitor(currentTracker)
                        significantMotionSensor?.startListener()
                        isContDownTimerRunning = false
                        savedLocation = null
                        try {
                            Logger.d("Removing the wakelock")
                            wakeLock?.release()
                        } catch (e: Exception) {
                            Logger.e("Exception releasing the wake lock")
                        }
                        countDownTimer?.cancel()
                    } else
                        countDownTimer?.start()
                }
            }
        }
    }

    /**
     * Signal sent by the AR telling wht activity has been recognised
     * currently used when we are entering still: we start a timer that will stop  tracking
     * if we do not move for a while
     * @param activityData the current activity
     *
     */
    fun currentActivity(activityData: TrackerDBActivity) {
        if (activityData.activityType == DetectedActivity.STILL && activityData.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            if (!isContDownTimerRunning) {
                isContDownTimerRunning = true
                countDownTimer?.start()
            }
        } else if (activityData.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            if (isContDownTimerRunning) {
                countDownTimer?.cancel()
                isContDownTimerRunning = false
            }
        }
    }

    /**
     * This saves  unsaved data to the db
     */
    fun flushDataToDB() {
        activityRecognition?.flush(this)
        heartMonitor?.flush()
        locationTracker?.flush(this)
        stepCounter?.flush()
    }

    /**
     * Called by the main interface - it reduces the size of the temporary queues to 1
     * and flushes the db
     * @param flush if true it flushes
     */
    fun keepFlushingToDB(flush: Boolean) {
        activityRecognition?.keepFlushingToDB(this, flush)
        heartMonitor?.keepFlushingToDB(flush)
        locationTracker?.keepFlushingToDB(this, flush)
        stepCounter?.keepFlushingToDB(flush)
    }
}
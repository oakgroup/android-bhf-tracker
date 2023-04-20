package com.active.orbit.tracker.tracker

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.*
import android.os.PowerManager.WakeLock
import android.util.Log
import com.active.orbit.tracker.Repository
import com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityData
import com.active.orbit.tracker.tracker.sensors.activity_recognition.ActivityRecognition
import com.active.orbit.tracker.tracker.sensors.batteries.BatteryMonitor
import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateMonitor
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationTracker
import com.active.orbit.tracker.tracker.sensors.location_recognition.LocationUtilities
import com.active.orbit.tracker.tracker.sensors.significant_motion.SignificantMotionSensor
import com.active.orbit.tracker.tracker.sensors.step_counting.StepCounter
import com.active.orbit.tracker.utils.Globals
import com.active.orbit.tracker.utils.PreferencesStore
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.Runnable


class TrackerService : Service() {

    private lateinit var updateTimerThread: Runnable
    private var wakeLockHandler: Handler? = null
    private var savedLocation: Location? = null
    private var isContDownTimerRunning: Boolean = false
    var cntdwntmr: CountDownTimer? = null
    var locationTracker: LocationTracker? = null
    var stepCounter: StepCounter? = null
    var heartMonitor: HeartRateMonitor? = null
    var batteryMonitor: BatteryMonitor? = null
    var activityRecognition: ActivityRecognition? = null
    private var significantMotionSensor: SignificantMotionSensor? = null

    private var currentTrackerNotification: TrackerNotification? = null
    private var wakeLock: WakeLock? = null
    private var repository: Repository? = null

    companion object {
        private val TAG = TrackerService::class.java.simpleName
        var currentTracker: TrackerService? = null
        private const val NOTIFICATION_ID = 9974
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Creating the Tracker Service!! $this")
        currentTracker = this

        //it initialises the sensor trackers and the repository before starting the foreground process
        // We do it in the onCreate so to avoid calling this every time the intent is re-delivered
        val preference = PreferencesStore()
        val useStepCounter = preference.getBooleanPreference(this, Globals.USE_STEP_COUNTER, true)
        val useActivityRecognition = preference.getBooleanPreference(this, Globals.USE_ACTIVITY_RECOGNITION, true)
        val useLocationTracking = preference.getBooleanPreference(this, Globals.USE_LOCATION_TRACKING, true)
        val useHRmonitoring = preference.getBooleanPreference(this, Globals.USE_HEART_RATE_MONITOR, true)
        val useBatteryMonitoring = preference.getBooleanPreference(this, Globals.USE_BATTERY_MONITOR, true)

        repository = Repository.getInstance(this)

        if (locationTracker == null && useLocationTracking == true)
            locationTracker = LocationTracker(this)
        if (activityRecognition == null && useActivityRecognition == true)
            activityRecognition = ActivityRecognition(this)
        if (stepCounter == null && useStepCounter == true)
            stepCounter = StepCounter(this)
        if (heartMonitor == null && useHRmonitoring == true)
            heartMonitor = HeartRateMonitor(this, repository)
        if (batteryMonitor == null && useBatteryMonitoring == true)
            batteryMonitor = BatteryMonitor(this, repository)

        initCountDownToStoppingSensors()
    }


    /**
     * it starts the foreground process
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "starting the foreground service...")
        startWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Log.i(TAG, "starting foreground process")
                currentTrackerNotification = TrackerNotification(this, NOTIFICATION_ID, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        currentTrackerNotification!!.notification!!,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, currentTrackerNotification!!.notification)
                }
                Log.i(TAG, "Starting foreground process successful!")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground process " + e.message)
            }
        }
        startTrackers()
        return START_REDELIVER_INTENT
    }

    /**
     * it acquires the wakelock
     */
    fun startWakeLock() {
        Log.i(TAG, "Acquiring the wakelock")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        wakeLock?.acquire()
    }

    /**
     *  it starts the sensors
     */
    fun startTrackers() {
        Log.i(TAG, "Starting trackers...")
        try {
            Log.i(TAG, "starting step counting ${stepCounter != null}")
            stepCounter?.startStepCounting()
        } catch (e: Exception) {
            Log.e(TAG, "error starting the step counter: " + e.message)
        }
        try {
            Log.i(TAG, "starting A/R ${activityRecognition != null}")
            activityRecognition?.startActivityRecognition(this)
        } catch (e: Exception) {
            Log.e(TAG, "error starting  A/R: " + e.message)
        }
        try {
            Log.i(TAG, "starting location tracking ${locationTracker != null}")
            locationTracker?.startLocationTracking(this)
        } catch (e: Exception) {
            Log.e(TAG, "error starting the location tracker: " + e.message)
        }
        try {
            Log.i(TAG, "starting heart rate monitoring ${batteryMonitor != null}")
            heartMonitor?.startMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "error starting the hr monitor: " + e.message)
        }
        try {
            Log.i(TAG, "starting battery monitoring ${heartMonitor != null}")
            batteryMonitor?.insertData()
        } catch (e: Exception) {
            Log.e(TAG, "error starting the battery monitor: " + e.message)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        flushDataToDB()
        Log.i(TAG, "TrackerService OnDestroy")
        stopSensors()
        wakeLockHandler?.removeCallbacks(updateTimerThread)
        wakeLock?.release()
        currentTracker = null
    }

    /**
     * it stops all the sensors - it also flushes everything to the database
     */
    private fun stopSensors() {
        Log.i(TAG, "stopping sensors")
        try {
            stepCounter?.stopStepCounting()
        } catch (e: Exception) {
            Log.i(TAG, "stepcounter failed to stop" + e.message)
        }
        try {
            heartMonitor?.stopMonitor()
        } catch (e: Exception) {
            Log.i(TAG, "HR monitor did not stop" + e.message)
        }
        try {
            activityRecognition?.stopActivityRecognition(this)
        } catch (e: Exception) {
            Log.i(TAG, "A/R did not stop " + e.message)
        }
        try {
            locationTracker?.stopLocationTracking()
        } catch (e: Exception) {
            Log.i(TAG, "location tracker did not stop " + e.message)
        }
    }

    private fun initCountDownToStoppingSensors() {
        if (cntdwntmr == null) {
            cntdwntmr = object : CountDownTimer(600000, 600000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    val locationUtils = LocationUtilities()
                    if (locationTracker != null && locationUtils.computeDistance(
                            locationTracker!!.currentLocation,
                            savedLocation
                        ) < 100
                    ) {
                        stopSensors()
                        flushDataToDB()
                        significantMotionSensor = SignificantMotionSensor(currentTracker)
                        significantMotionSensor?.startListener()
                        isContDownTimerRunning = false
                        savedLocation = null
                        Log.i(TAG, "removing the wakelock")
                        wakeLock?.release()
                        cntdwntmr?.cancel()
                    } else
                        cntdwntmr?.start()
                }
            }
        }
    }

    /**
     * signal sent by the AR telling wht activity has been recognised
     * currently used when we are entering still: we start a timer that will stop  tracking
     * if we do not move for a while
     * @param activityData the current activity
     *
     */
    fun currentActivity(activityData: ActivityData) {
        if (activityData.type == DetectedActivity.STILL && activityData.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            if (!isContDownTimerRunning) {
                isContDownTimerRunning = true
                cntdwntmr?.start()
            }
        } else if (activityData.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            if (isContDownTimerRunning) {
                cntdwntmr?.cancel()
                isContDownTimerRunning = false
            }
        }
    }

    /**
     * it saves  unsaved data to the db
     */
    fun flushDataToDB() {
        locationTracker?.flushLocations(this)
        stepCounter?.flush()
        heartMonitor?.flush()
        activityRecognition?.flush(this)
    }

    /**
     * called by the main interface - it reduces the size of the temporary queues to 1
     * and flushes the db
     * @param flush if true it flushes
     */
    fun keepFlushingToDB(flush: Boolean) {
        activityRecognition?.keepFlushingToDB(this, flush)
        stepCounter?.keepFlushingToDB(flush)
        locationTracker?.keepFlushingToDB(this, flush)
        heartMonitor?.keepFlushingToDB(flush)
    }
}
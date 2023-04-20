package com.active.orbit.tracker.tracker.sensors.heart_rate_monitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.active.orbit.tracker.Repository
import com.active.orbit.tracker.tracker.TrackerService
import com.active.orbit.tracker.utils.Globals

class HeartRateMonitor(val context: TrackerService, val repository: Repository?) : SensorEventListener {

    private lateinit var taskTimeOutRunnable: Runnable
    private var taskHandler: Handler? = null
    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null
    private val heartRateMonitor: HeartRateMonitor = this
    private var monitoringStartTime: Long = System.currentTimeMillis()
    private var noContactTimes: Int
    var heartRateReadingStack: MutableList<HeartRateData> = mutableListOf()

    companion object {
        private val TAG = HeartRateMonitor::class.java.simpleName

        /**
         * the size of the stack where we store the HR reading before sending them to the database
         * consider that each reading comes every second, so 80 means a db operation every 80 seconds
         * which is a lot. On the other hand you do not want to have a large memory footprint,
         * so leave it more or less this size
         */
        private const val STANDARD_BUFFER_SIZE = 100
        private var MAX_BUFFER_SIZE = STANDARD_BUFFER_SIZE
        private const val SAMPLING_RATE_IN_MICROSECONDS = 10000 * 1000
        private const val coolingOffPeriod: Long = 45000
        private const val monitoringPeriod: Long = Globals.MSECS_IN_A_MINUTE * 2
        private var sensorActive = false
    }

    init {
        noContactTimes = 0
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        if (Looper.myLooper() == null)
            Looper.prepare()
        val looper = Looper.myLooper()
        looper.let {
            taskHandler = Handler(it!!)
            taskTimeOutRunnable = Runnable {
                if (sensorActive) {
                    Log.i(TAG, "periodically stopping HR - restart in ${coolingOffPeriod / 1000} seconds")
                    stopSensing()
                } else {
                    Log.i(TAG, "periodically starting HR - stopping in ${monitoringPeriod / 1000} seconds")
                    startSensing()
                }
                taskHandler?.postDelayed(
                    taskTimeOutRunnable,
                    if (sensorActive) coolingOffPeriod else monitoringPeriod
                )
                sensorActive = !sensorActive
            }
        }
    }


    //////////////////////////////////////////////////////////
    //               API METHODS
    //////////////////////////////////////////////////////////

    /**
     * API method to be called to stop the monitor and its restarter controller
     */
    fun stopMonitor() {
        try {
            taskHandler?.removeCallbacksAndMessages(null)
            flushToDatabase()
        } catch (e: Exception) {
            Log.i(TAG, "Irrelevant")
        }
    }

    /**
     * it takes everything in the temp storage and calculates the avg heart rate and accuracy. Then it stores a
     * meta heart rate sensor value into the DB, The array is then flushed
     */
    private fun flushHeartRateToDB() {
        Log.i(TAG, "Flushing hr values to database")
        if (TrackerService.currentTracker != null) {
            InsertHeartRateDataAsync(
                TrackerService.currentTracker!!,
                heartRateReadingStack
            )
            heartRateReadingStack = mutableListOf()
        }
    }

    /**
     * API method to be called to start the monitor and its restarter controller
     */

    fun startMonitoring() {
        taskHandler?.removeCallbacksAndMessages(null)
        taskHandler?.postDelayed(taskTimeOutRunnable, 1000)
    }

    /**
     * API Method
     * called when the interface is opened - it flushes all data
     * and the incoming data will be flushed to the DB immediately
     * @param flush
     */
    fun keepFlushingToDB(flush: Boolean) {
        MAX_BUFFER_SIZE = if (flush) {
            sensorManager?.flush(this)
            0
        } else
            STANDARD_BUFFER_SIZE
        Log.i(TAG, "flushing hr readings? $flush")
    }


    /**
     * API method to flush to the readings to the database
     */
    fun flush() {
        flushToDatabase()
    }

    //////////////////////////////////////////////////////////
    //               Internal METHODS
    //////////////////////////////////////////////////////////


    private fun startSensing() {
        monitoringStartTime = System.currentTimeMillis()
        heartRateReadingStack = mutableListOf()
        if (sensorManager != null) {
            sensorManager!!.registerListener(
                this, heartRateSensor, SAMPLING_RATE_IN_MICROSECONDS, SAMPLING_RATE_IN_MICROSECONDS
            )
            Log.i(TAG, "HR started")
        } else
            Log.i(TAG, "I could not start HR Monitor!!")
        noContactTimes = 0
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            if (event.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT) {
                val heartRateData = HeartRateData(System.currentTimeMillis(), event.values[0].toInt(), event.accuracy)
                heartRateReadingStack.add(heartRateData)
                if (heartRateReadingStack.size > MAX_BUFFER_SIZE) {
                    flushHeartRateToDB()
                    heartRateReadingStack = mutableListOf()
                }
                repository?.currentHeartRate?.value = event.values[0].toInt()
                Log.d(TAG, "reading found " + event.values[0].toInt() + "(" + event.accuracy + ")")
            } else {
                Log.d(TAG, "Accuracy is not very good  " + event.values[0] + "(" + event.accuracy + ")")
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}


    /**
     * it flushes the HR sensor and it writes the values to the db
     */
    private fun stopSensing() {
        Log.i(TAG, "Heart Rate Sensor: flushing")
        flushToDatabase()
        try {
            if (sensorManager != null)
                sensorManager?.unregisterListener(heartRateMonitor)
        } catch (e: Exception) {
            Log.i(
                TAG,
                "Error Stopping HR Monitor ${e.message}"
            )
        } catch (e: Error) {
            Log.i(TAG, "No idea")
        }
    }

    private fun flushToDatabase() {
        sensorManager?.flush(this)
        flushHeartRateToDB()
    }
}
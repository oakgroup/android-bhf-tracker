package uk.ac.shef.tracker.core.monitors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import uk.ac.shef.tracker.core.database.models.TrackerDBStep
import uk.ac.shef.tracker.core.database.tables.TrackerTableSteps
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.ThreadHandler.backgroundThread
import uk.ac.shef.tracker.core.utils.TimeUtils

class StepMonitor internal constructor(private var context: Context?) {

    private lateinit var taskTimeOutRunnable: Runnable
    private lateinit var taskHandler: Handler
    private var stepCounterListener: SensorEventListener? = null
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    var stepsList: MutableList<TrackerDBStep> = mutableListOf()
    var sensorValuesList: MutableList<TrackerDBStep> = mutableListOf()

    companion object {

        var WAITING_TIME_IN_MSECS = 20000
        private const val STANDARD_MAX_SIZE = 40
        private var MAX_SIZE = STANDARD_MAX_SIZE

        // get two groups of readings because apparently you may get more than
        // 2 readings a second. This does not happen to huawei when in the background as it
        // returns only every time the handler returns (i.e. every 20 secs)
        private val MAX_SENSOR_VALUE_LIST_SIZE: Int =
            if (Build.BRAND.lowercase() == "huawei") 3 else 2 * WAITING_TIME_IN_MSECS / 1000
    }

    init {
        sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            stepCounterSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepCounterSensor != null) {
                Logger.d("Step Counter found on phone")
                stepCounterListener = getSensorListener()
            } else {
                Logger.d("Step Counter ot present on phone")
            }
        }
    }

    /**
     * API call for the Tracker Sensor to start the step counter
     */
    fun startStepCounting() {
        // if the sensor is null,then mSensorManager is null and we get a crash
        if (isStepCounterAvailable()) {
            Logger.d("Starting listener")
            registerListener()
            huaweiHandler()
        }
    }


    /**
     * API call for the Tracker Sensor to stop the step counter
     */
    fun stopStepCounting() {
        Logger.i("Stopping step counter")
        flush()
        try {
            sensorManager!!.unregisterListener(stepCounterListener)
        } catch (e: java.lang.Exception) {
            //  already unregistered
        }
    }

    /**
     * This stores the steps into the temp list (or in case of overflow into the DB)
     * and it sets the variable lastStepsDetected
     */
    private fun storeSteps(stepsData: TrackerDBStep) {
        Logger.i("Found ${stepsData.steps} steps at ${TimeUtils.formatMillis(stepsData.timeInMillis, Constants.DATE_FORMAT_HOUR_MINUTE_SECONDS)}")
        stepsList.add(stepsData)
        backgroundThread {
            if (context != null && stepsList.size > MAX_SIZE) {
                TrackerTableSteps.upsert(context!!, stepsList)
                stepsList = mutableListOf()
            }
        }
    }

    /**
     * Huawei needs regularly stopping the step counter and restarting it
     * otherwise it will not return any steps when i the background
     *
     */
    private fun huaweiHandler() {
        if (Build.BRAND.lowercase() == "huawei") {
            if (Looper.myLooper() == null) Looper.prepare()
            val looper = Looper.myLooper()
            looper.let {
                taskHandler = Handler(it!!)
                taskTimeOutRunnable = Runnable {
                    stopStepCounting()
                    registerListener()
                    taskHandler.postDelayed(taskTimeOutRunnable, WAITING_TIME_IN_MSECS.toLong())
                }
                taskHandler.postDelayed(taskTimeOutRunnable, 1000)
            }
        }
    }

    private fun registerListener() {
        // the parameters are required in microseconds and we have them in milliseconds
        // so both are * 1000
        sensorManager!!.registerListener(
            stepCounterListener, stepCounterSensor,
            WAITING_TIME_IN_MSECS * 1000,
            2 * WAITING_TIME_IN_MSECS * 1000
        )
    }

    /**
     * Constructor for the sensor listener for the step counter
     * @return the sensor listener
     */
    private fun getSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    val dbStep = TrackerDBStep()
                    dbStep.timeInMillis = TimeUtils.fromEventTimeToEpoch(event.timestamp)
                    dbStep.steps = event.values[0].toInt()
                    sensorValuesList.add(dbStep)
                    if (sensorValuesList.size > MAX_SENSOR_VALUE_LIST_SIZE) {
                        selectBestSensorValue(sensorValuesList)
                        sensorValuesList = mutableListOf()
                    }
                }
            }


            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    fun flush() {
        selectBestSensorValue(sensorValuesList)
        sensorValuesList = mutableListOf()
        try {
            sensorManager!!.flush(stepCounterListener)
        } catch (e: java.lang.Exception) {
            // do nothing
        }
        backgroundThread {
            if (context != null && stepsList.size > MAX_SIZE) {
                TrackerTableSteps.upsert(context!!, stepsList)
                stepsList = mutableListOf()
            }
        }
    }

    /**
     * The sensor returns very quickly (after every step). We collect each step
     * and then every x collection, we select the best one
     * @param sensorValuesList
     */
    private fun selectBestSensorValue(sensorValuesList: MutableList<TrackerDBStep>) {
        if (sensorValuesList.size == 0) return
        if (sensorValuesList.size == 1) {
            storeSteps(sensorValuesList[0])
            return
        }
        sensorValuesList.sortBy { it.timeInMillis }
        var firstTime = sensorValuesList[0].timeInMillis
        val lastTime = sensorValuesList[sensorValuesList.size - 1].timeInMillis
        if (lastTime - firstTime < WAITING_TIME_IN_MSECS * 1.1)
            storeSteps(sensorValuesList[sensorValuesList.size - 1])
        else {
            var found = false
            for (index in 1 until sensorValuesList.size) {
                val stepData = sensorValuesList[index]
                val time = stepData.timeInMillis
                if (time - firstTime >= WAITING_TIME_IN_MSECS) {
                    storeSteps(stepData)
                    firstTime = time
                    found = true
                }
            }
            if (!found) {
                storeSteps(sensorValuesList[sensorValuesList.size - 1])
            }
        }
    }

    fun keepFlushingToDB(flush: Boolean) {
        MAX_SIZE = if (flush) {
            flush()
            0
        } else
            STANDARD_MAX_SIZE
        Logger.i("Flushing steps? $flush")

    }

    /**
     * This checks if the step counter is available
     */
    private fun isStepCounterAvailable(): Boolean {
        return stepCounterSensor != null
    }
}
/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.monitors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate
import uk.ac.shef.tracker.core.database.tables.TrackerTableHeartRates
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.ThreadHandler.backgroundThread
import uk.ac.shef.tracker.core.utils.TimeUtils

class HeartRateMonitor(val context: TrackerService) : SensorEventListener {

    private lateinit var taskTimeOutRunnable: Runnable
    private var taskHandler: Handler? = null
    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null
    private val heartRateMonitor: HeartRateMonitor = this
    private var monitoringStartTime: Long = System.currentTimeMillis()
    private var noContactTimes: Int
    var heartRateReadingStack: MutableList<TrackerDBHeartRate> = mutableListOf()

    companion object {
        /**
         * The size of the stack where we store the HR reading before sending them to the database
         * consider that each reading comes every second, so 80 means a db operation every 80 seconds
         * which is a lot. On the other hand you do not want to have a large memory footprint,
         * so leave it more or less this size
         */
        private const val STANDARD_BUFFER_SIZE = 100
        private var MAX_BUFFER_SIZE = STANDARD_BUFFER_SIZE
        private const val SAMPLING_RATE_IN_MICROSECONDS = 10000 * 1000
        private const val coolingOffPeriod: Long = 45000
        private const val monitoringPeriod: Long = TimeUtils.ONE_MINUTE_MILLIS.toLong() * 2
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
                    Logger.i("Periodically stopping HR - restart in ${coolingOffPeriod / 1000} seconds")
                    stopSensing()
                } else {
                    Logger.i("Periodically starting HR - stopping in ${monitoringPeriod / 1000} seconds")
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

    /**
     * API method to be called to stop the monitor and its restarter controller
     */
    fun stopMonitor() {
        try {
            taskHandler?.removeCallbacksAndMessages(null)
            flushToDatabase()
        } catch (e: Exception) {
            Logger.i("Irrelevant")
        }
    }

    /**
     * This takes everything in the temp storage and calculates the avg heart rate and accuracy. Then it stores a
     * meta heart rate sensor value into the DB, The array is then flushed
     */
    private fun flushHeartRateToDB() {
        Logger.i("Flushing hr values to database")
        backgroundThread {
            TrackerTableHeartRates.upsert(context, heartRateReadingStack)
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
     * Called when the interface is opened - it flushes all data
     * and the incoming data will be flushed to the DB immediately
     * @param flush
     */
    fun keepFlushingToDB(flush: Boolean) {
        MAX_BUFFER_SIZE = if (flush) {
            sensorManager?.flush(this)
            0
        } else
            STANDARD_BUFFER_SIZE
        Logger.i("Flushing hr readings? $flush")
    }


    /**
     * API method to flush to the readings to the database
     */
    fun flush() {
        flushToDatabase()
    }

    private fun startSensing() {
        monitoringStartTime = System.currentTimeMillis()
        heartRateReadingStack = mutableListOf()
        if (sensorManager != null) {
            sensorManager!!.registerListener(
                this, heartRateSensor, SAMPLING_RATE_IN_MICROSECONDS, SAMPLING_RATE_IN_MICROSECONDS
            )
            Logger.i("HR started")
        } else
            Logger.i("I could not start HR Monitor!!")
        noContactTimes = 0
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            if (event.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT) {
                val dbHeartRate = TrackerDBHeartRate()
                dbHeartRate.timeInMillis = System.currentTimeMillis()
                dbHeartRate.heartRate = event.values[0].toInt()
                dbHeartRate.accuracy = event.accuracy
                heartRateReadingStack.add(dbHeartRate)
                if (heartRateReadingStack.size > MAX_BUFFER_SIZE) {
                    flushHeartRateToDB()
                    heartRateReadingStack = mutableListOf()
                }

                // TODO needed?
                val currentHeartRate = event.values[0].toInt()

                Logger.d("Reading found " + event.values[0].toInt() + "(" + event.accuracy + ")")
            } else {
                Logger.d("Accuracy is not very good  " + event.values[0] + "(" + event.accuracy + ")")
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}


    /**
     * This flushes the HR sensor and it writes the values to the db
     */
    private fun stopSensing() {
        Logger.i("Heart Rate Sensor: flushing")
        flushToDatabase()
        try {
            if (sensorManager != null)
                sensorManager?.unregisterListener(heartRateMonitor)
        } catch (e: Exception) {
            Logger.i(
                "Error Stopping HR Monitor ${e.localizedMessage}"
            )
        } catch (e: Error) {
            Logger.i("No idea")
        }
    }

    private fun flushToDatabase() {
        sensorManager?.flush(this)
        flushHeartRateToDB()
    }
}
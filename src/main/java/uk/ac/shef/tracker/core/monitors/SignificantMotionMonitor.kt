/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.monitors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.Logger

/**
 * This class monitors the significant motions data and save them into the database
 */
class SignificantMotionMonitor(val trackerService: TrackerService?) {

    private var sensorManager: SensorManager? = trackerService?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private var motionSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
    private val triggerEventListener: TriggerEventListener

    init {
        triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent) {
                trackerService?.startTrackers()
                trackerService?.startWakeLock()
            }
        }
    }

    /**
     * This starts the significant motion sensor
     * @return T if sensor successfully started,  otherwise F
     */
    fun startListener(): Boolean {
        val started = sensorManager?.requestTriggerSensor(triggerEventListener, motionSensor) == true
        if (started) {
            Logger.i("Motion Sensor started - flushing and stopping locations")
            TrackerService.currentTracker?.flushDataToDB()
            TrackerService.currentTracker?.locationTracker?.stopLocationTracking()
        }
        return started
    }
}
package com.active.orbit.tracker.core.monitors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import com.active.orbit.tracker.core.tracker.TrackerService
import com.active.orbit.tracker.core.utils.Logger

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
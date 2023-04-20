package com.active.orbit.tracker.tracker.sensors.significant_motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.util.Log
import com.active.orbit.tracker.tracker.TrackerService

class SignificantMotionSensor(val trackerService: TrackerService?) {

    private var sensorManager: SensorManager = trackerService?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    var motionSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
    private val triggerEventListener: TriggerEventListener

    companion object {
        private val TAG = SignificantMotionSensor::class.java.simpleName
    }

    init {
        triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent) {
                trackerService?.startTrackers()
                trackerService?.startWakeLock()
            }
        }

    }

    /**
     * it starts the significant motion sensor
     * @return T if sensor successfully started,  otherwise F
     */
    fun startListener(): Boolean {
        val started = sensorManager.requestTriggerSensor(triggerEventListener, motionSensor)
        if (started) {
            Log.i(TAG, "Motion Sensor started - flushing and stopping locations")
            TrackerService.currentTracker?.flushDataToDB()
            TrackerService.currentTracker?.locationTracker?.stopLocationTracking()
        }
        return started
    }
}
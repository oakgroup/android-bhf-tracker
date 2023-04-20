package com.active.orbit.tracker.data_upload.dts_data

import com.active.orbit.tracker.tracker.sensors.heart_rate_monitor.HeartRateData

class HeartRateDataDTS(heartRateData: HeartRateData) {

    val id: Int = heartRateData.id
    var timeInMsecs: Long = heartRateData.timeInMsecs
    var accuracy = heartRateData.accuracy
    var heartRate = heartRateData.heartRate

    /**
     * * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "HeartRateDataDTS(id=$id, timeInMsecs=$timeInMsecs, accuracy=$accuracy, heartRate=$heartRate)"
    }
}
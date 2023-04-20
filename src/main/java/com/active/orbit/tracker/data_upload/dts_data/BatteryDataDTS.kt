package com.active.orbit.tracker.data_upload.dts_data

import com.active.orbit.tracker.tracker.sensors.batteries.BatteryData

class BatteryDataDTS(batteryData: BatteryData) {

    val id: Int = batteryData.id
    var timeInMsecs: Long = batteryData.timeInMsecs
    var batteryPercent = batteryData.batteryPercent
    var isCharging = batteryData.isCharging

    /**
     * * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "HeartRateDataDTS(id=$id, timeInMsecs=$timeInMsecs, batteryPercent=$batteryPercent, isCharging=$isCharging)"
    }
}
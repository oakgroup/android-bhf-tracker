package com.active.orbit.tracker.tracker.sensors.batteries

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.active.orbit.tracker.tracker.sensors.SensingData
import com.active.orbit.tracker.utils.Utils

@Entity(indices = [Index("timeInMsecs")])
class BatteryData(override var timeInMsecs: Long, var batteryPercent: Int, var isCharging: Boolean) : SensingData() {

    @PrimaryKey(autoGenerate = true)
    override var id = 0

    override fun toString(): String {
        return Utils.millisecondsToString(timeInMsecs, "HH:mm:ss").toString() + ": " + batteryPercent + " (" + isCharging + ")"
    }

}
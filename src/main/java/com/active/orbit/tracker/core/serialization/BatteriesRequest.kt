package com.active.orbit.tracker.core.serialization

import com.active.orbit.tracker.core.database.models.TrackerDBBattery
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class BatteriesRequest {

    @SerializedName("user_id")
    var userId = Constants.EMPTY

    @SerializedName("batteries")
    val batteries = ArrayList<BatteryRequest>()

    class BatteryRequest(dbBattery: TrackerDBBattery) {

        @SerializedName("id")
        val id: Int = dbBattery.idBattery

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbBattery.timeInMillis

        @SerializedName("batteryPercent")
        val batteryPercent: Int = dbBattery.batteryPercent

        @SerializedName("isCharging")
        val isCharging: Boolean = dbBattery.isCharging
    }
}
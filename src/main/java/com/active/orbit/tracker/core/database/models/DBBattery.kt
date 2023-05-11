package com.active.orbit.tracker.core.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.TimeUtils

@Entity(
    tableName = "batteries"
)
data class DBBattery(@PrimaryKey(autoGenerate = true) var idBattery: Int = Constants.INVALID) : BaseModel {

    var batteryPercent: Int = 0
    var isCharging: Boolean = false
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    override fun identifier(): String {
        return idBattery.toString()
    }

    fun description(): String {
        return "[$idBattery - $batteryPercent - $isCharging - $timeInMillis - $timeZone - $uploaded]"
    }

    override fun isValid(): Boolean {
        return idBattery != Constants.INVALID && timeInMillis > 0
    }

    override fun priority(): Long {
        return timeInMillis
    }
}
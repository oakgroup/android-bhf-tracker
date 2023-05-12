package com.active.orbit.tracker.core.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.TimeUtils

@Entity(
    tableName = "heart_rates"
)
data class DBHeartRate(@PrimaryKey(autoGenerate = true) var idHeartRate: Int = 0) : BaseModel {

    var heartRate: Int = 0
    var accuracy: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    override fun identifier(): String {
        return idHeartRate.toString()
    }

    fun description(): String {
        return "[$idHeartRate - $heartRate - $accuracy - $timeInMillis - $timeZone - $uploaded]"
    }

    override fun isValid(): Boolean {
        return idHeartRate != Constants.INVALID && timeInMillis > 0
    }

    override fun priority(): Long {
        return timeInMillis
    }
}
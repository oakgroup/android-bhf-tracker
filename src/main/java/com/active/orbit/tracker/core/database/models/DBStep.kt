package com.active.orbit.tracker.core.database.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.monitors.StepMonitor
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.TimeUtils

@Entity(
    tableName = "steps"
)
data class DBStep(@PrimaryKey(autoGenerate = true) var idStep: Int = Constants.INVALID) : BaseModel {

    var steps: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    @Ignore
    var cadence: Int = 0

    override fun identifier(): String {
        return idStep.toString()
    }

    fun description(): String {
        return "[$idStep - $steps - $timeInMillis - $timeZone - $uploaded]"
    }

    override fun isValid(): Boolean {
        return idStep != Constants.INVALID && timeInMillis > 0
    }

    override fun priority(): Long {
        return timeInMillis
    }

    /**
     * Given a start time for the current step data, it returns the cadence
     *
     * @param previousStepsData step element immediately preceding this in time
     * @return the cadence
     */
    fun computeCadence(previousStepsData: DBStep): Int {
        val stepDiff = steps - previousStepsData.steps
        var secdiff = (timeInMillis - previousStepsData.timeInMillis) / 1000.0
        // if the consecutive step counts are far away in time, the time is not representative for cadence
        // so we just take the last few seconds as time reference
        if (secdiff > StepMonitor.WAITING_TIME_IN_MSECS * 1.5) secdiff = StepMonitor.WAITING_TIME_IN_MSECS * 1.5
        cadence = (stepDiff / secdiff * 60.0).toInt()
        return cadence
    }
}
package com.active.orbit.tracker.core.database.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.active.orbit.tracker.core.generics.TrackerBaseModel
import com.active.orbit.tracker.core.monitors.ActivityMonitor
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.TimeUtils
import com.google.android.gms.location.DetectedActivity

@Entity(
    tableName = "activities",
    indices = [
        Index(value = ["timeInMillis"], unique = true)
    ]
)
data class TrackerDBActivity(@PrimaryKey(autoGenerate = true) var idActivity: Int = 0) : TrackerBaseModel {

    var activityType: Int = 0
    var transitionType: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    companion object {

        fun getActivityTypeString(activityType: Int): String {
            return when (activityType) {
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                DetectedActivity.RUNNING -> "RUNNING"
                DetectedActivity.ON_BICYCLE -> "CYCLING"
                DetectedActivity.IN_VEHICLE -> "VEHICLE"
                DetectedActivity.ON_FOOT -> "ON FOOT"
                else -> "--"
            }
        }

        fun getTransitionType(transitionType: Int): String {
            return ActivityMonitor.getTransitionTypeString(transitionType)
        }
    }

    override fun identifier(): String {
        return idActivity.toString()
    }

    fun description(): String {
        return "[$idActivity - ${getActivityTypeString(activityType)} - ${getTransitionType(transitionType)} - ${TimeUtils.formatMillis(timeInMillis, Constants.DATE_FORMAT_UTC)} - $timeZone - $uploaded]"
    }

    override fun isValid(): Boolean {
        return idActivity != Constants.INVALID && timeInMillis > 0
    }

    override fun priority(): Long {
        return timeInMillis
    }

    fun copyFields(other: TrackerDBActivity) {
        timeInMillis = other.timeInMillis
        activityType = other.activityType
        transitionType = other.transitionType
        timeZone = other.timeZone
        idActivity = other.idActivity
    }
}
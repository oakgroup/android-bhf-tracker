/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.location.DetectedActivity
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.monitors.ActivityMonitor
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Database entity that represents an activity model
 */
@Entity(
    tableName = "activities",
    indices = [
        Index(value = ["timeInMillis", "activityType"], unique = true)
    ]
)
data class TrackerDBActivity(@PrimaryKey(autoGenerate = true) var idActivity: Int = 0) : TrackerBaseModel {

    var activityType: Int = 0
    var transitionType: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    companion object {

        /**
         * Get the activity type from a string
         *
         * @param activityType the activity type [Int]
         * @return the activity type [String]
         */
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

        /**
         * Get the transition type from a string
         *
         * @param transitionType the transition type [Int]
         * @return the transition type [String]
         */
        fun getTransitionType(transitionType: Int): String {
            return ActivityMonitor.getTransitionTypeString(transitionType)
        }
    }

    /**
     * @return the model identifier
     */
    override fun identifier(): String {
        return idActivity.toString()
    }

    /**
     * @return the model description
     */
    fun description(): String {
        return "[$idActivity - ${getActivityTypeString(activityType)} - ${getTransitionType(transitionType)} - ${TimeUtils.formatMillis(timeInMillis, Constants.DATE_FORMAT_UTC)} - $timeZone - $uploaded]"
    }

    /**
     * Check the validity of this model according to the required data
     *
     * @return true if the model is valid
     */
    override fun isValid(): Boolean {
        return idActivity != Constants.INVALID && timeInMillis > 0
    }

    /**
     * Get the priority of this model
     *
     * @return the [Long] priority
     */
    override fun priority(): Long {
        return timeInMillis
    }

    /**
     * Copy all the fields of the current object from another one
     *
     * @param other the [TrackerDBActivity] to copy the fields from
     * @return the [Long] priority
     */
    fun copyFields(other: TrackerDBActivity) {
        timeInMillis = other.timeInMillis
        activityType = other.activityType
        transitionType = other.transitionType
        timeZone = other.timeZone
        idActivity = other.idActivity
    }

    override fun toString(): String {
        return "Activity(time=${TimeUtils.formatMillis(timeInMillis, Constants.DATE_FORMAT_FULL)}, activityType=$activityType, transitionType=$transitionType"
    }


}
/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.monitors.StepMonitor
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Database entity that represents a step model
 */
@Entity(
    tableName = "steps",
    indices = [
        Index(value = ["timeInMillis"], unique = true)
    ]
)
data class TrackerDBStep(@PrimaryKey(autoGenerate = true) var idStep: Int = 0) : TrackerBaseModel {

    var steps: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    @Ignore
    var cadence: Int = 0

    /**
     * @return the model identifier
     */
    override fun identifier(): String {
        return idStep.toString()
    }

    /**
     * @return the model description
     */
    fun description(): String {
        return "[$idStep - $steps - ${TimeUtils.formatMillis(timeInMillis, Constants.DATE_FORMAT_UTC)} - $timeZone - $uploaded]"
    }

    /**
     * Check the validity of this model according to the required data
     *
     * @return true if the model is valid
     */
    override fun isValid(): Boolean {
        return idStep != Constants.INVALID && timeInMillis > 0
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
     * Given a start time for the current step data, it returns the cadence
     *
     * @param previousStepsData step element immediately preceding this in time
     * @return the cadence
     */
    fun computeCadence(previousStepsData: TrackerDBStep): Int {
        val stepDiff = steps - previousStepsData.steps
        var secdiff = (timeInMillis - previousStepsData.timeInMillis) / 1000.0
        // if the consecutive step counts are far away in time, the time is not representative for cadence
        // so we just take the last few seconds as time reference
        if (secdiff > StepMonitor.WAITING_TIME_IN_MSECS * 1.5) secdiff = StepMonitor.WAITING_TIME_IN_MSECS * 1.5
        cadence = (stepDiff / secdiff * 60.0).toInt()
        return cadence
    }
}
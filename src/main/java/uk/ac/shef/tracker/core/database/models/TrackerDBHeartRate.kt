/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Database entity that represents an heart model
 */
@Entity(
    tableName = "heart_rates",
    indices = [
        Index(value = ["timeInMillis"], unique = true)
    ]
)
data class TrackerDBHeartRate(@PrimaryKey(autoGenerate = true) var idHeartRate: Int = 0) : TrackerBaseModel {

    var heartRate: Int = 0
    var accuracy: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    /**
     * @return the model identifier
     */
    override fun identifier(): String {
        return idHeartRate.toString()
    }

    /**
     * @return the model description
     */
    fun description(): String {
        return "[$idHeartRate - $heartRate - $accuracy - $timeInMillis - $timeZone - $uploaded]"
    }

    /**
     * Check the validity of this model according to the required data
     *
     * @return true if the model is valid
     */
    override fun isValid(): Boolean {
        return idHeartRate != Constants.INVALID && timeInMillis > 0
    }

    /**
     * Get the priority of this model
     *
     * @return the [Long] priority
     */
    override fun priority(): Long {
        return timeInMillis
    }
}
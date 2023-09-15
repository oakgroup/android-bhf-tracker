/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.models

import android.location.Location
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Database entity that represents a location model
 */
@Entity(
    tableName = "locations",
    indices = [
        Index(value = ["timeInMillis"], unique = true)
    ]
)
data class TrackerDBLocation(@PrimaryKey(autoGenerate = true) var idLocation: Int = 0) : TrackerBaseModel {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var altitude: Double = 0.0
    var accuracy: Double = 0.0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    @Ignore
    var distance: Double = 0.0

    @Ignore
    var sed: Double = 0.0

    @Ignore
    var speed: Double = 0.0

    @Ignore
    var locationsSupportingCentroid: MutableList<TrackerDBLocation> = mutableListOf()

    /**
     * @constructor from [Location]
     */
    constructor(location: Location) : this() {
        timeInMillis = location.time
        latitude = location.latitude
        longitude = location.longitude
        altitude = location.altitude
        accuracy = location.accuracy.toDouble()
    }

    /**
     * @constructor from location attributes
     */
    constructor(
        timeInMillis: Long,
        latitude: Double,
        longitude: Double,
        accuracy: Double,
        altitude: Double
    ) : this() {
        this.timeInMillis = timeInMillis
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.accuracy = accuracy
    }

    /**
     * @return the model identifier
     */
    override fun identifier(): String {
        return idLocation.toString()
    }

    /**
     * @return the model description
     */
    fun description(): String {
        return "[$idLocation - $latitude - $longitude - $altitude - $accuracy - ${
            TimeUtils.formatMillis(
                timeInMillis,
                Constants.DATE_FORMAT_FULL
            )
        } - $timeZone - $uploaded]"
    }

    /**
     * Check the validity of this model according to the required data
     *
     * @return true if the model is valid
     */
    override fun isValid(): Boolean {
        return idLocation != Constants.INVALID && timeInMillis > 0
    }

    /**
     * Get the priority of this model
     *
     * @return the [Long] priority
     */
    override fun priority(): Long {
        return timeInMillis
    }

    override fun toString(): String {
        return "Location(time=${
            TimeUtils.formatMillis(
                timeInMillis,
                Constants.DATE_FORMAT_FULL
            )
        }, latitude=$latitude, longitude=$longitude, accuracy=$accuracy,  distance=$distance, speed=$speed)"
    }

    fun copyDeep(): TrackerDBLocation {
        val anotherLocation= TrackerDBLocation()
        anotherLocation.latitude = latitude
        anotherLocation.longitude = longitude
        anotherLocation.altitude = altitude
        anotherLocation.accuracy = accuracy
        anotherLocation.timeInMillis = timeInMillis
        anotherLocation.timeZone = timeZone
        anotherLocation.uploaded = uploaded
        anotherLocation.distance = distance
        anotherLocation.sed = sed
        anotherLocation.speed = speed
        anotherLocation.locationsSupportingCentroid = locationsSupportingCentroid
        return anotherLocation
    }
}
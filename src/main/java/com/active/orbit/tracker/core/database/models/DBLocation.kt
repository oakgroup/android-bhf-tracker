package com.active.orbit.tracker.core.database.models

import android.location.Location
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.active.orbit.tracker.core.generics.BaseModel
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.TimeUtils

@Entity(
    tableName = "locations"
)
data class DBLocation(@PrimaryKey(autoGenerate = true) var idLocation: Int = 0) : BaseModel {

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
    var locationsSupportingCentroid: MutableList<DBLocation> = mutableListOf()

    constructor(location: Location) : this() {
        timeInMillis = location.time
        latitude = location.latitude
        longitude = location.longitude
        altitude = location.altitude
        accuracy = location.accuracy.toDouble()
    }

    constructor(timeInMillis: Long, latitude: Double, longitude: Double, accuracy: Double, altitude: Double) : this() {
        this.timeInMillis = timeInMillis
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.accuracy = accuracy
    }

    override fun identifier(): String {
        return idLocation.toString()
    }

    fun description(): String {
        return "[$idLocation - $latitude - $longitude - $altitude - $accuracy - $timeInMillis - $timeZone - $uploaded]"
    }

    override fun isValid(): Boolean {
        return idLocation != Constants.INVALID && timeInMillis > 0
    }

    override fun priority(): Long {
        return timeInMillis
    }
}
package com.active.orbit.tracker.tracker.sensors.location_recognition

import android.location.Location
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.active.orbit.tracker.tracker.sensors.SensingData
import com.active.orbit.tracker.utils.Utils
import kotlin.math.roundToInt

@Entity(indices = [Index("uploaded"), Index(value = arrayOf("timeInMsecs"), unique = true)])
class LocationData(
    override var timeInMsecs: Long, var latitude: Double, var longitude: Double, var accuracy: Double,
    var altitude: Double
) : SensingData() {
    @Ignore
    var distance: Double = 0.0

    /**
     * the goodness of a location in the simplification process
     */
    @Ignore
    var sed: Double = 0.0

    @PrimaryKey(autoGenerate = true)
    override var id: Int = 0

    @Ignore
    var speed: Double = 0.0

    @Ignore
    var locationsSupportingCentroid: MutableList<LocationData> = mutableListOf()

    constructor(location: Location) : this(
        location.time, location.latitude,
        location.longitude, location.accuracy.toDouble(), location.altitude
    )

    override fun toString(): String {
        return Utils.millisecondsToString(timeInMsecs, "HH:mm:ss") +
                "- (" + latitude +
                "" + longitude +
                ") alt:" + altitude.roundToInt() +
                ", acc:" + accuracy.roundToInt()
    }

    fun copy(): LocationData {
        return LocationData(
            timeInMsecs,
            latitude,
            longitude,
            accuracy,
            altitude
        )
    }
}

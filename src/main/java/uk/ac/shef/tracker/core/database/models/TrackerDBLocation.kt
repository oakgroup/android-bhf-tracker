package uk.ac.shef.tracker.core.database.models

import android.location.Location
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.TimeUtils

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
        return "[$idLocation - $latitude - $longitude - $altitude - $accuracy - ${TimeUtils.formatMillis(timeInMillis, Constants.DATE_FORMAT_FULL)} - $timeZone - $uploaded]"
    }

    override fun isValid(): Boolean {
        return idLocation != Constants.INVALID && timeInMillis > 0
    }

    override fun priority(): Long {
        return timeInMillis
    }
}
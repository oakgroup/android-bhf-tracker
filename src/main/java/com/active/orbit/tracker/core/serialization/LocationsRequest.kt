package com.active.orbit.tracker.core.serialization

import com.active.orbit.tracker.core.database.models.TrackerDBLocation
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class LocationsRequest {

    @SerializedName("_id")
    var userId = Constants.EMPTY

    @SerializedName("locations")
    val locations = ArrayList<LocationRequest>()

    class LocationRequest(dbLocation: TrackerDBLocation) {

        @SerializedName("id")
        val id: Int = dbLocation.idLocation

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbLocation.timeInMillis

        @SerializedName("latitude")
        val latitude: Double = dbLocation.latitude

        @SerializedName("longitude")
        val longitude: Double = dbLocation.longitude

        @SerializedName("altitude")
        val altitude: Double = dbLocation.altitude

        @SerializedName("accuracy")
        val accuracy: Double = dbLocation.accuracy
    }
}
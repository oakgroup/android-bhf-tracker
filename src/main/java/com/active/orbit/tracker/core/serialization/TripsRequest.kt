package com.active.orbit.tracker.core.serialization

import com.active.orbit.tracker.core.database.models.TrackerDBTrip
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class TripsRequest {

    @SerializedName("_id")
    var userId = Constants.EMPTY

    @SerializedName("trips")
    val trips = ArrayList<TripRequest>()

    class TripRequest(dbTrip: TrackerDBTrip) {

        @SerializedName("id")
        val id: Int = dbTrip.idTrip

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbTrip.timeInMillis

        @SerializedName("startTime")
        val startTime: Long = dbTrip.getStartTime(dbTrip.chart)

        @SerializedName("endTime")
        val endTime: Long = dbTrip.getEndTime(dbTrip.chart)

        @SerializedName("activityType")
        val activityType: Int = dbTrip.activityType

        @SerializedName("radiusInMeters")
        val radiusInMeters: Int = dbTrip.radiusInMeters

        @SerializedName("distanceInMeters")
        val distanceInMeters: Int = dbTrip.distanceInMeters

        @SerializedName("steps")
        val steps: Int = dbTrip.steps
    }
}
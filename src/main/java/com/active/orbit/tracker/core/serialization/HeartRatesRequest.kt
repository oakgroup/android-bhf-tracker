package com.active.orbit.tracker.core.serialization

import com.active.orbit.tracker.core.database.models.TrackerDBHeartRate
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class HeartRatesRequest {

    @SerializedName("_id")
    var userId = Constants.EMPTY

    @SerializedName("heart_rates")
    val heartRates = ArrayList<HeartRateRequest>()

    class HeartRateRequest(dbHeartRate: TrackerDBHeartRate) {

        @SerializedName("id")
        val id: Int = dbHeartRate.idHeartRate

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbHeartRate.timeInMillis

        @SerializedName("accuracy")
        val accuracy: Int = dbHeartRate.accuracy

        @SerializedName("heartRate")
        val heartRate: Int = dbHeartRate.heartRate
    }
}
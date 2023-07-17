/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.database.models.TrackerDBHeartRate
import uk.ac.shef.tracker.core.utils.Constants

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
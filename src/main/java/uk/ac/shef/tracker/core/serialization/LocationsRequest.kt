/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.utils.Constants

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
/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.utils.Constants

/**
 * This class is used to automatically build the payload for the server api
 */
class ActivitiesRequest {

    @SerializedName("_id")
    var userId = Constants.EMPTY

    @SerializedName("activities")
    val activities = ArrayList<ActivityRequest>()

    class ActivityRequest(dbActivity: TrackerDBActivity) {

        @SerializedName("id")
        val id: Int = dbActivity.idActivity

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbActivity.timeInMillis

        @SerializedName("type")
        val type: Int = dbActivity.activityType

        @SerializedName("transitionType")
        val transitionType: Int = dbActivity.transitionType
    }
}
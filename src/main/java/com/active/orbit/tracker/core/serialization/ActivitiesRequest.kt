package com.active.orbit.tracker.core.serialization

import com.active.orbit.tracker.core.database.models.TrackerDBActivity
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

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
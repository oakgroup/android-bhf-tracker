package com.active.orbit.tracker.core.serialization

import com.active.orbit.tracker.core.database.models.DBStep
import com.active.orbit.tracker.core.utils.Constants
import com.google.gson.annotations.SerializedName

class StepsRequest {

    @SerializedName("_id")
    var userId = Constants.EMPTY

    @SerializedName("steps")
    val steps = ArrayList<StepRequest>()

    class StepRequest(dbStep: DBStep) {

        @SerializedName("id")
        val id: Int = dbStep.idStep

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbStep.timeInMillis

        @SerializedName("steps")
        val steps: Int = dbStep.steps
    }
}
package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.database.models.TrackerDBStep
import uk.ac.shef.tracker.core.utils.Constants

class StepsRequest {

    @SerializedName("_id")
    var userId = Constants.EMPTY

    @SerializedName("steps")
    val steps = ArrayList<StepRequest>()

    class StepRequest(dbStep: TrackerDBStep) {

        @SerializedName("id")
        val id: Int = dbStep.idStep

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbStep.timeInMillis

        @SerializedName("steps")
        val steps: Int = dbStep.steps
    }
}
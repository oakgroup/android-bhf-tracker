/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import uk.ac.shef.tracker.core.database.models.TrackerDBStep
import uk.ac.shef.tracker.core.utils.Constants

/**
 * This class is used to automatically build the payload for the server api
 */
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
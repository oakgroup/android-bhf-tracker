package com.active.orbit.tracker.data_upload.dts_data

import com.active.orbit.tracker.tracker.sensors.step_counting.StepsData

class StepsDataDTS(stepData: StepsData) {

    val id: Int = stepData.id
    var timeInMsecs: Long = stepData.timeInMsecs
    val steps: Int = stepData.steps

    /**
     * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "StepsDataDTS(id=$id, timeInMsecs=$timeInMsecs, steps=$steps)"
    }
}

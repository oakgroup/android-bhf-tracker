package com.active.orbit.tracker.core.computation.data

import com.active.orbit.tracker.core.database.models.TrackerDBActivity
import com.active.orbit.tracker.core.database.models.TrackerDBLocation
import com.active.orbit.tracker.core.utils.TimeUtils
import com.google.android.gms.location.ActivityTransition

class MobilityData(var timeInMSecs: Long) {

    companion object {
        const val INVALID_VALUE = -100
    }

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var location: TrackerDBLocation? = null

    var steps: Int = INVALID_VALUE
    var cadence: Int = INVALID_VALUE

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var speed: Double = INVALID_VALUE.toDouble()
    var distance: Double = INVALID_VALUE.toDouble()
    var longitude: Double = INVALID_VALUE.toDouble()
    var latitude: Double = INVALID_VALUE.toDouble()

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var activityIn: Int = INVALID_VALUE
    var activityOut: Int = INVALID_VALUE

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var assignedActivity: Int = INVALID_VALUE

    override fun toString(): String {
        //return "MobilityElementData(timeInMSecs=${Utils.millisecondsToString(timeInMSecs, "HH:mm:ss")},  steps=$steps, cadence=$cadence, cadenceInLastNMinutes=$cadenceInLastNMinutes, speed=$speed, speedInLastNMinutes=$speedInLastNMinutes, activity=${ActivityData.getActivityTypeString(activity)}, activitiesInLastNMinutes=$activitiesInLastNMinutes, transitionType=${ActivityData.getTransitionType(transitionType)})"
        return "${
            TimeUtils.formatMillis(timeInMSecs, "HH:mm:ss")
        },  ${
            if (cadence == INVALID_VALUE) " , ," else "$steps, $cadence,"
        } ${
            if (activityOut == INVALID_VALUE) " ," else {
                " ${TrackerDBActivity.getActivityTypeString(activityOut)} " +
                        TrackerDBActivity.getTransitionType(ActivityTransition.ACTIVITY_TRANSITION_EXIT) + ", "
            }
        } ${
            if (activityIn == INVALID_VALUE) ", , " else {
                " ${TrackerDBActivity.getActivityTypeString(activityIn)} " +
                        TrackerDBActivity.getTransitionType(ActivityTransition.ACTIVITY_TRANSITION_ENTER) + ", "
            }
        } ${
            if (speed == INVALID_VALUE.toDouble()) ", , " else {
                "$speed, $distance,"
            }
        } ${
            if (assignedActivity == INVALID_VALUE) ", " else {
                "$TrackerDBActivity.getActivityTypeString(assignedActivity),"
            }
        }"
    }

    /**
     * This copies the valid fields from element if they are invalid in the current element
     * @param element
     */
    fun copyValidFields(element: MobilityData) {
        val invalidLong = INVALID_VALUE.toLong()
        val invalidDouble = INVALID_VALUE.toDouble()
        if (timeInMSecs == invalidLong && element.timeInMSecs != invalidLong) timeInMSecs = element.timeInMSecs
        if (steps == INVALID_VALUE && element.steps != INVALID_VALUE) steps = element.steps
        if (cadence == INVALID_VALUE && element.cadence != INVALID_VALUE) cadence = element.cadence
        if (speed == invalidDouble && element.speed != invalidDouble) speed = element.speed
        if (distance == invalidDouble && element.distance != invalidDouble) distance = element.distance
        if (latitude == invalidDouble && element.latitude != invalidDouble) latitude = element.latitude
        if (longitude == invalidDouble && element.longitude != invalidDouble) longitude = element.longitude
        if (activityIn == INVALID_VALUE && element.activityIn != INVALID_VALUE) activityIn = element.activityIn
        if (activityOut == INVALID_VALUE && element.activityOut != INVALID_VALUE) activityOut = element.activityOut
        if (location == null && element.location != null) location = element.location
    }
}
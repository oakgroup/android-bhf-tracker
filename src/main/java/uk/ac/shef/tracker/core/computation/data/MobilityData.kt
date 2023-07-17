package uk.ac.shef.tracker.core.computation.data

import com.google.android.gms.location.ActivityTransition
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.TimeUtils

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
        val stringBuilder = StringBuilder()
        stringBuilder.append(TimeUtils.formatMillis(timeInMSecs, Constants.DATE_FORMAT_HOUR_MINUTE_SECONDS))
        stringBuilder.append(", ")
        stringBuilder.append(if (cadence == INVALID_VALUE) " INVALID, INVALID" else "$steps, $cadence")
        stringBuilder.append(", ")
        stringBuilder.append(if (activityIn == INVALID_VALUE) " INVALID, INVALID" else "${TrackerDBActivity.getActivityTypeString(activityIn)}, ${TrackerDBActivity.getTransitionType(ActivityTransition.ACTIVITY_TRANSITION_ENTER)}")
        stringBuilder.append(", ")
        stringBuilder.append(if (activityOut == INVALID_VALUE) " INVALID, INVALID" else "${TrackerDBActivity.getActivityTypeString(activityOut)}, ${TrackerDBActivity.getTransitionType(ActivityTransition.ACTIVITY_TRANSITION_EXIT)}")
        stringBuilder.append(", ")
        stringBuilder.append(if (speed == INVALID_VALUE.toDouble()) " INVALID, INVALID" else "$speed, $distance")
        stringBuilder.append(", ")
        stringBuilder.append(if (assignedActivity == INVALID_VALUE) " INVALID" else TrackerDBActivity.getActivityTypeString(assignedActivity))
        return stringBuilder.toString()
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
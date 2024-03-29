/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.database.models

import android.content.Context
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.DetectedActivity.STILL
import uk.ac.shef.tracker.core.computation.MobilityComputation
import uk.ac.shef.tracker.core.computation.data.MobilityData
import uk.ac.shef.tracker.core.generics.TrackerBaseModel
import uk.ac.shef.tracker.core.monitors.StepMonitor
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.LocationUtilities
import uk.ac.shef.tracker.core.utils.TimeUtils
import kotlin.math.abs
import kotlin.math.max

/**
 * Database entity that represents a trip model
 */
@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["startTime"], unique = true)
    ]
)

data class TrackerDBTrip(@PrimaryKey(autoGenerate = true) var idTrip: Int = 0) : TrackerBaseModel {

    var startTime: Int = 0
    var endTime: Int = 0
    var activityType: Int = 0
    var radiusInMeters: Int = 0
    var distanceInMeters: Int = 0
    var steps: Int = 0
    var timeInMillis: Long = 0
    var timeZone: Int = TimeUtils.getTimezoneOffset(timeInMillis)
    var uploaded: Boolean = false

    @Ignore
    var chart: MutableList<MobilityData> = arrayListOf()

    /**
     * the number of milliseconds spent over the brisk limit in walking
     */
    @Ignore
    var briskMSecs : Long = 0
    @Ignore
    var heartDistanceInMeters : Long = 0
    @Ignore
    var locations: MutableList<TrackerDBLocation> = mutableListOf()

    @Ignore
    val subTrips: MutableList<TrackerDBTrip> = mutableListOf()

    @Ignore
    var reliable: Boolean = true

    companion object {
        const val BRISK_CADENCE=100
    }
    /**
     * @constructor from trip attributes
     */
    constructor(
        startTime: Int,
        endTime: Int,
        activityType: Int,
        chart: MutableList<MobilityData>
    ) : this() {
        this.startTime = startTime
        this.endTime = endTime
        this.activityType = activityType
        this.chart = chart
    }

    /**
     * @return the model identifier
     */
    override fun identifier(): String {
        return idTrip.toString()
    }

    /**
     * @return the model description
     */
    fun description(): String {
        return "[$idTrip - ${
            TimeUtils.formatMillis(
                getStartTime(chart),
                Constants.DATE_FORMAT_HOUR_MINUTE_SECONDS
            )
        } - ${
            TimeUtils.formatMillis(
                getEndTime(chart),
                Constants.DATE_FORMAT_HOUR_MINUTE_SECONDS
            )
        } - ${TrackerDBActivity.getActivityTypeString(activityType)} - $radiusInMeters - $distanceInMeters - $steps - ${
            TimeUtils.formatMillis(
                timeInMillis,
                Constants.DATE_FORMAT_UTC
            )
        } - $timeZone - $uploaded]"
    }

    /**
     * Check the validity of this model according to the required data
     *
     * @return true if the model is valid
     */
    override fun isValid(): Boolean {
        return idTrip != Constants.INVALID && timeInMillis > 0
    }

    /**
     * Get the priority of this model
     *
     * @return the [Long] priority
     */
    override fun priority(): Long {
        return timeInMillis
    }

    /**
     * Called when all the information has been set. Remember to call it after compacting as well
     */
    fun finalise(computeLocations: Boolean) {
        finaliseStepsAndCadence()
        if (computeLocations) {
            locations = this.getTripLocations()
            cleanLocations()
        }
        getDistanceFromLocations()
        getMovementRadius()
        tagIfSuspicious()
    }

    /**
     * Get the cadence of this trip
     *
     * @return the [Int] cadence
     */
    fun getCadence(): Int {
        if (getDuration(chart) > 0)
            return (steps / getDuration(chart) * 60.0).toInt()
        return 0
    }

    /**
     * Set the number of steps for this trip and counts the number of heartMSeconds
     * The steps are NOT cumulative!
     */
    fun finaliseStepsAndCadence() {
        steps = 0
        briskMSecs = 0L
        /** TODO to be computed */
        heartDistanceInMeters = 0L
        for (index in startTime..endTime) {

            var prevIndex = -1
            if (chart[index].steps != MobilityData.INVALID_VALUE) {
                steps += chart[index].steps
                if (chart[index].cadence>BRISK_CADENCE)
                    if (prevIndex==-1){
                        briskMSecs += StepMonitor.WAITING_TIME_IN_MSECS
                    } else {
                        briskMSecs += chart[index].timeInMSecs-chart[prevIndex].timeInMSecs
                    }

            }
        }
    }

    /**
     * Get the trip start time
     *
     * @return the [Long] trip start time
     */
    fun getStartTime(chart: MutableList<MobilityData>): Long {
        if (chart.size > 0) return chart[startTime].timeInMSecs
        return 0
    }

    /**
     * Get the trip end time
     *
     * @return the [Long] trip end time
     */
    fun getEndTime(chart: MutableList<MobilityData>): Long {
        if (chart.size > 1) return chart[endTime].timeInMSecs
        return 0
    }

    /**
     * Get the trip duration
     *
     * @return the [Long] trip duration
     */
    fun getDuration(chart: MutableList<MobilityData>): Long {
        val startTime = chart[startTime].timeInMSecs
        val endTime = chart[endTime].timeInMSecs
        return endTime - startTime
    }

    /**
     * This checks if it is possible to compact two consecutive activities
     *
     * @param followingTrip
     * @return true/false - it adds the following trip to the subTrips field if compacted
     */
    fun compactIfPossible(followingTrip: TrackerDBTrip): Boolean {
        val diff =
            abs(chart[endTime].timeInMSecs - followingTrip.chart[followingTrip.startTime].timeInMSecs)
        if (compatibleActivityType(followingTrip) && diff < MobilityComputation.SHORT_ACTIVITY_DURATION) {
            subTrips.add(followingTrip)
            // select the best type for the conglomerate before changing the end time
            // but after adding to the sub-trips list
            selectBestType()
            endTime = followingTrip.endTime
            locations.addAll(followingTrip.locations)
            cleanLocations()
            // recompute the stats and locations
            finalise(false)
            return true
        }
        return false
    }

    /**
     * This considers a trip that has been fused with others. It selects the best type given the duration
     * Mostly used to decide if we are running or walking in a mixed activity
     */
    private fun selectBestType() {
        if (subTrips.size > 0) {
            // decides the type based on the sub-trips duration
            val activityDurations: HashMap<Int, Long> = hashMapOf()
            activityDurations[activityType] = getDuration(chart)
            for (subTrip in subTrips) {
                // skip the sub trips that we have already incorporated and consider only the one that
                // has just been added
                if (subTrip.startTime > endTime) {
                    activityDurations[subTrip.activityType] =
                        if (activityDurations[subTrip.activityType] != null) max(
                            subTrip.getDuration(
                                chart
                            ), activityDurations[subTrip.activityType]!!
                        )
                        else subTrip.getDuration(chart)
                }
            }
            val maxByIndex = activityDurations.maxByOrNull { it.value }
            if (maxByIndex != null) activityType = maxByIndex.key
        }
    }

    /**
     * Walking, running and on foot are compatible activities as they can be easily misclassified
     * @param otherTrip the trip to check compatibility with
     * @return
     */
    private fun compatibleActivityType(otherTrip: TrackerDBTrip): Boolean {
        return (activityType == otherTrip.activityType
                // compact on foot with whatever walking or running
                || ((activityType in listOf(DetectedActivity.WALKING, DetectedActivity.ON_FOOT))
                && (otherTrip.activityType in listOf(
            DetectedActivity.WALKING,
            DetectedActivity.ON_FOOT
        )))
                || ((activityType in listOf(DetectedActivity.RUNNING, DetectedActivity.ON_FOOT))
                && (otherTrip.activityType in listOf(
            DetectedActivity.RUNNING,
            DetectedActivity.ON_FOOT
        )))
                // if type is not identical do not compact a long walk followed by a long run: they are distinct activities
                || ((getDuration(chart) < 2 * MobilityComputation.SHORT_ACTIVITY_DURATION || otherTrip.getDuration(
            chart
        ) < 2 * MobilityComputation.SHORT_ACTIVITY_DURATION)
                && (activityType in listOf(
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_FOOT
        ))
                && (otherTrip.activityType in listOf(
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_FOOT
        ))))
    }

    /**
     * This extracts the steps from the chart associated to the trip
     *
     * @return the list of the steps associated to the trip
     */
    private fun getTripSteps(): MutableList<TrackerDBStep> {
        val steps: MutableList<TrackerDBStep> = mutableListOf()
        for (index in startTime until endTime) {
            val chartElement = chart[index]
            if (chartElement.steps > 0) {
                val dbStep = TrackerDBStep()
                dbStep.timeInMillis = chartElement.timeInMSecs
                dbStep.steps = chartElement.steps
                steps.add(dbStep)
            }
        }
        return steps
    }

    /**
     * This extracts the locations from the chart associated to the trip
     *
     * @return the list of the locations associated to the trip
     */
    private fun getTripLocations(): MutableList<TrackerDBLocation> {
        var locations: MutableList<TrackerDBLocation> = mutableListOf()
        // find initial location - this is necessary because for e.g. vehicles it takes a time before it
        // kicks in, so we want to get the last location before leaving
        for (index in startTime - 1 downTo 0) {
            val chartElement = chart[index]
            if (chartElement.location != null) {
                locations.add(chartElement.location!!)
                break
            }
        }
        // find locations internal to trip
        for (index in startTime until endTime) {
            val chartElement = chart[index]
            if (chartElement.location != null)
                locations.add(chartElement.location!!)
        }
        // do not add a final location, as if close enough
        // it has been already added to the last element

        if (TrackerService.currentTracker == null || !TrackerPreferences.config(TrackerService.currentTracker!!).compactLocations) return locations
        locations = LocationUtilities().simplifyLocationsListUsingSED(locations)
        return locations
    }

    /**
     * This looks for pikes in teh location list and normalises the spikes taking the average of the
     * surrounding locations
     */
    private fun cleanLocations() {
        if (locations.size == 0) return
        for (index in 1 until locations.size - 1) {
            val loc1 = locations[index - 1]
            val loc2 = locations[index]
            val loc3 = locations[index + 1]
            val locationUtils = LocationUtilities()
            val dist13 = locationUtils.computeDistance(loc1, loc3)
            val dist12 = locationUtils.computeDistance(loc1, loc2)
            val dist23 = locationUtils.computeDistance(loc2, loc3)
            // if there is a spike take the average of the two surrounding the spike
            if (dist13 < dist12 && dist13 < dist23) {
                loc2.latitude = (loc1.latitude + loc3.latitude) / 2
                loc2.longitude = (loc1.longitude + loc3.longitude) / 2
                loc2.altitude = (loc1.altitude + loc3.altitude) / 2
            }
        }
    }

    /**
     * This computes the distance in meters covered in the trip using the (cleaned) locations
     */
    private fun getDistanceFromLocations() {
        distanceInMeters = 0
        for (index in 1 until locations.size) {
            val loc1 = locations[index - 1]
            val loc2 = locations[index]
            val locationUtils = LocationUtilities()
            distanceInMeters += locationUtils.computeDistance(loc1, loc2).toInt()
        }
    }

    private fun getMovementRadius() {
        radiusInMeters = 0
        val locationUtils = LocationUtilities()
        for ((index1, loc1) in locations.withIndex()) {
            for (index2 in index1 until locations.size) {
                val loc2 = locations[index2]
                if (loc1 != loc2) radiusInMeters =
                    max(radiusInMeters, locationUtils.computeDistance(loc1, loc2).toInt())
            }
        }
    }

    /**
     * A car trip is suspicious if it does not move in location and so is a bike trip
     * maybe it is just a matter of the shopping moved around indoor or shopping at the supermarket
     * using a trolley (which looks like cycling)
     */
    fun tagIfSuspicious() {
        if (isSuspicious(activityType, radiusInMeters))
            reliable = false
    }

    fun isSuspicious(activityType: Int, radiusInMeters: Int): Boolean {
        if (activityType == MobilityData.INVALID_VALUE) return true
        if (activityType == DetectedActivity.IN_VEHICLE && radiusInMeters < Constants.MINIMUM_RADIUS_FOR_VEHICLES) return true
        if (activityType == DetectedActivity.ON_BICYCLE && radiusInMeters < Constants.MINIMUM_RADIUS_FOR_BIKES) return true
        return false
    }

    /**
     * This returns the speed in m/seconds
     * in case you wonder:
     * 10km/h = 2.77 m/sec
     * 20km/h = 5.55 m/sec
     * 30km/h = 8.33 m/sec
     * 40km/h = 11.1 m/sec
     * 50km/h = 13.88 m/sec
     */
    fun getSpeedInMPerSecs(): Long {
        val duration = getDuration(chart)
        return if (duration > 0) (distanceInMeters / (duration / 1000.0)).toLong()
        else 0
    }

    /**
     * This checks the average cadence of a trip and a 40 steps/minute average means that there is
     * some serious cadence. No need to
     * @param chart
     * @return true or false
     */
    fun hasAverageHighCadence(chart: MutableList<MobilityData>): Boolean {
        val durationInMinutes = getDuration(chart) / TimeUtils.ONE_MINUTE_MILLIS
        val isHighCadence = steps / durationInMinutes > Constants.HIGH_AVERAGE_CADENCE
        return isHighCadence && (durationInMinutes > MobilityComputation.SHORT_ACTIVITY_DURATION * 2 / TimeUtils.ONE_MINUTE_MILLIS)
    }

    /**
     * This checks if there are considerable high cadence in the chart for more than SHORT_ACTIVITY_DURATION * 2
     * However this should remove the walking from the still rather than allocating the entire thing
     * to walking - if you have a non recognised walk followed by 6 hours of still, you assign the 6 hours to walking!!
     * @param chart
     * @return true or false
     */
    fun hasNoticeableWalkingBurst(chart: MutableList<MobilityData>): Boolean {
        var highCadenceDurationInSecs = 0L
        var base = startTime
        for (index in startTime..endTime) {
            if (chart[index].steps != MobilityData.INVALID_VALUE) {
                val differenceInMSeconds = (chart[index].timeInMSecs - chart[base].timeInMSecs)
                // avoid unreasonably short bursts
                if (differenceInMSeconds > 5000) {
                    if (chart[index].cadence > Constants.HIGH_AVERAGE_CADENCE) {
                        highCadenceDurationInSecs += differenceInMSeconds
                    }
                }
                base = index
            }
        }
        return highCadenceDurationInSecs > MobilityComputation.SHORT_ACTIVITY_DURATION * 2
    }

    /**
     * This gets the maximum distance from any location to the base point
     * @param baseLocation the first location in the day
     * @return the distance
     */
    fun getTripRadius(baseLocation: TrackerDBLocation?): Int {
        if (baseLocation == null) return 0
        val locationUtils = LocationUtilities()
        var maxDistance = 0
        // do not use low accuracy locations for the trip radius
        for (location in locationUtils.removeLowAccuracyLocations(locations)) {
            maxDistance =
                max(maxDistance, locationUtils.computeDistance(location, baseLocation).toInt())
        }
        return maxDistance / 2
    }

    /**
     * Used when we correct a trip's type: if we change vehicle to still, we need to check if
     * for example we did not have 2 hours of still and 10 minutes of vehicle. In that case we will
     * return a still and a vehicle trips
     *
     * @param newActivityType
     * @return
     */
    fun extractWalkingAndOtherActivitiesFromStill(
        context: Context,
        newActivityType: Int
    ): List<TrackerDBTrip> {
        val finalList = mutableListOf<TrackerDBTrip>()
        if (newActivityType == DetectedActivity.IN_VEHICLE && activityType == STILL) {
            val locationUtils = LocationUtilities()
            val tripsList = locationUtils.findLocationMovementInLocationsList(
                locations,
                100,
                chart[startTime].timeInMSecs,
                chart[endTime].timeInMSecs,
                newActivityType,
                chart
            )
            var prevTripData: TrackerDBTrip? = null
            if (tripsList.size > 1) {
                var stTime = startTime
                for (tripModel in tripsList) {
                    tripModel.steps = 0
                    if (tripModel.locations.size > 0) {
                        for (index in stTime..endTime) {
                            if (chart[index].timeInMSecs == tripModel.locations[0].timeInMillis) {
                                if (tripModel.activityType == STILL)
                                    tripModel.startTime = stTime
                                else
                                    tripModel.startTime = index
                                // if the last one is a still it must end at the end of the current trip
                                // as it may not have locations, this may have been lost
                                if (prevTripData?.activityType == STILL)
                                    prevTripData.endTime = index
                            }
                            if (chart[index].timeInMSecs == tripModel.locations[tripModel.locations.size - 1].timeInMillis) {
                                tripModel.endTime = index
                                break
                            }
                        }
                        stTime = tripModel.endTime + 1
                        prevTripData = tripModel
                        finalList.add(tripModel)
                        if (stTime >= chart.size)
                            break
                    }
                    // if there are no locations then we skip the subtrip
                }
            } else finalList.add(this)
        } else if (newActivityType in listOf(DetectedActivity.WALKING, DetectedActivity.RUNNING)) {
            finalList.addAll(
                findWalkingMovementInStepsList(
                    context,
                    getTripSteps(),
                    newActivityType,
                    chart
                )
            )
        } else if ((activityType == STILL || activityType == MobilityData.INVALID_VALUE) && newActivityType == DetectedActivity.IN_VEHICLE) {
            //we are proposing a vehicle for still or invalid = let's accept it - it means that we have found that there is considerable distance
            activityType = newActivityType
            finalList.add(this)
            reliable = true
        } else {
            finalList.add(this)
            tagIfSuspicious()
        }
        // if the last one is a still it must end at the end of the current trip
        // as it may not have locations, this may have been lost
        if (finalList.size > 1 && finalList[finalList.size - 1].activityType == STILL)
            finalList[finalList.size - 1].endTime = endTime
        // otherwise add a still until the end
        else if (finalList.size > 1){
            val newTrip = TrackerDBTrip()
            newTrip.startTime= finalList[finalList.size - 1].endTime
            newTrip.endTime= endTime
            newTrip.activityType= STILL
            newTrip.chart = chart
            newTrip.finalise(false)
            finalList.add(newTrip)
        }
        for (trip in finalList)
            trip.finalise(true)
        return finalList
    }

    /**
     * it looks for the activities hidden in a still
     */
    private fun findWalkingMovementInStepsList(
        context: Context,
        steps: MutableList<TrackerDBStep>,
        activityType: Int,
        chart: MutableList<MobilityData>
    ): MutableList<TrackerDBTrip> {
        val tripsList = mutableListOf<TrackerDBTrip>()
        var prevSteps: TrackerDBStep? = null
        val initialStartTime = if (steps.size > 0) getChartIndexOfSteps(steps.get(0), chart) else -1

        var newTrip = TrackerDBTrip()
        newTrip.startTime = initialStartTime
        newTrip.endTime = initialStartTime
        newTrip.activityType = STILL
        newTrip.chart = chart
        tripsList.add(newTrip)

        for (step in steps) {
            if (prevSteps != null) {
                // val distance = computeDistance(prevLocation, location)
                val differenceInTime = step.timeInMillis - prevSteps.timeInMillis
                if (differenceInTime <= MobilityComputation.SHORT_ACTIVITY_DURATION) {
                    newTrip.activityType = activityType
                } else {
                    val stepTime = getChartIndexOfSteps(step, chart)
                    newTrip = closeDownCurrentTrip(tripsList, newTrip, stepTime, true)
                }
            } else {
                val stepTime = getChartIndexOfSteps(step, chart)
                // as the add additional still param will be false, we have to update the endtag, otehrwise we will have a
                // missed period
                newTrip.endTime= stepTime
                newTrip = closeDownCurrentTrip(tripsList, newTrip, stepTime, false)
            }
            prevSteps = step
            // always update the end or you will lose it
            newTrip.endTime = getChartIndexOfSteps(step, chart)
        }
        // add the last one as not added yet
        if (tripsList.size > 0) {
            val locationTime = getChartIndexOfSteps(prevSteps!!, chart)
            newTrip.endTime = locationTime
            newTrip.finalise(true)
            if (newTrip.getDuration(chart) < 60000 || newTrip.steps < 200) newTrip.activityType =
                STILL
        }
        // add an additional final trip if we do not reach  midnight as we may have further locations
        addFinalElement(tripsList)

        val finalFinalList: MutableList<TrackerDBTrip> = mutableListOf()
        for (trip in tripsList) {
            // now check if the stills are actually vehicles
            if (trip.activityType == STILL)
                finalFinalList.addAll(trip.detectVehicleInStills(context))
            else
                finalFinalList.add(trip)
        }
        return finalFinalList
    }

    private fun closeDownCurrentTrip(
        tripsList: MutableList<TrackerDBTrip>,
        currentTrip: TrackerDBTrip,
        stepTime: Int,
        addAdditionlStill: Boolean
    ): TrackerDBTrip {
        // current trip has already end assigned so we finalise it
        currentTrip.finalise(true)
        if (currentTrip.getDuration(chart) < 60000 || currentTrip.steps < 200) currentTrip.activityType =
            STILL
        // if there was a gap in steps, then we insert a still in between the two walking
        if (addAdditionlStill) {
            val newTrip = TrackerDBTrip()
            newTrip.startTime = currentTrip.endTime
            newTrip.endTime = stepTime
            newTrip.activityType = STILL
            newTrip.chart = chart
            newTrip.finalise(true)
            tripsList.add(newTrip)
        }
        val newTrip = TrackerDBTrip()
        newTrip.startTime = stepTime
        newTrip.endTime = stepTime
        newTrip.activityType = STILL
        newTrip.chart = chart
        tripsList.add(newTrip)
        return newTrip
    }

    /**
     * Given a step, this finds the element in the chart referring to that location, i.e. the element that
     * has the time immediately > than the location time
     * @param step the step data
     * @param chart the chart to look the position in
     * @return the index of the chart element with the time immediately > than the step time
     */
    private fun getChartIndexOfSteps(step: TrackerDBStep, chart: MutableList<MobilityData>): Int {
        for (index in 0 until chart.size) {
            if (chart[index].timeInMSecs >= step.timeInMillis) return index
        }
        return chart.size - 1
    }

    override fun toString(): String {
        return "${
            TimeUtils.formatMillis(
                getStartTime(chart),
                Constants.DATE_FORMAT_HOUR_MINUTE
            )
        } -" +
                "${TimeUtils.formatMillis(getEndTime(chart), Constants.DATE_FORMAT_HOUR_MINUTE)}+" +
                "activity:$activityType radius:$radiusInMeters distance:$distanceInMeters steps: $steps "
    }


    /**
     * given a still trip, it returns the list of vehicle movements detected using locations
     */
    fun detectVehicleInStills(context: Context): Collection<TrackerDBTrip> {
        val useGlobalSED = TrackerPreferences.config(context).compactLocations
        val locationUtilities = LocationUtilities()
        val movementFound =
            locationUtilities.isLinearTrajectoryInActivity(locations, useGlobalSED, 1500)
        return if (movementFound) {
            extractWalkingAndOtherActivitiesFromStill(context, DetectedActivity.IN_VEHICLE)
        } else {
            val finalList: MutableList<TrackerDBTrip> = mutableListOf()
            finalList.add(this)
            finalList
        }
    }
}

//TimeUtils.formatMillis(dbTrip.getStartTime(chart), Constants.DATE_FORMAT_HOUR_MINUTE) + " - " + TimeUtils.formatMillis(dbTrip.getEndTime(chart), "HH:mm")
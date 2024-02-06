/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.computation

import android.content.Context
import com.google.android.gms.location.DetectedActivity
import uk.ac.shef.tracker.core.computation.data.MobilityData
import uk.ac.shef.tracker.core.computation.data.MobilityData.Companion.INVALID_VALUE
import uk.ac.shef.tracker.core.computation.data.SummaryData
import uk.ac.shef.tracker.core.database.models.TrackerDBActivity
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.database.models.TrackerDBTrip
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.LocationUtilities
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Class that performs the computation for the trips
 */
class TripsComputation(val context: Context, val chart: MutableList<MobilityData>) {

    var trips: MutableList<TrackerDBTrip>

    init {
        trips = assignTrips()
        recoverLostActivities(context)
        trips = correctTrips()
        trips = correctSuspiciousTrips(chart)
        trips = compactConsecutiveTrips()
        finaliseLocations()
        finaliseHeartMinutes()
    }

    private fun finaliseHeartMinutes() {
        for (trip in trips){

        }
    }

    /**
     * This creates the trips using the chart of activities
     */
    private fun assignTrips(): MutableList<TrackerDBTrip> {
        // now assign the final activities
        val tripsList: MutableList<TrackerDBTrip> = mutableListOf()
        var currentTrip = TrackerDBTrip(0, 0, DetectedActivity.STILL, this.chart)
        for ((index, element) in chart.withIndex()) {
            // just in case the tag was not balanced in the end we allow also element.activityIn != INVALID_VALUE
            if (element.activityOut != INVALID_VALUE || element.activityIn != INVALID_VALUE) {
                currentTrip.endTime = index

                // to be doubly sure - we started with element 0 as STILL, it may be incorrect
                if (currentTrip.startTime == 0 && element.activityOut != INVALID_VALUE)
                    currentTrip.activityType = element.activityOut

                currentTrip.finalise(true)
                tripsList.add(currentTrip)
                currentTrip = TrackerDBTrip(index, 0, element.activityIn, this.chart)

                Logger.i("Trip assigned ${currentTrip.description()}")
            }
        }
        if (tripsList.size > 0 && currentTrip.endTime == 0 && currentTrip.startTime != chart.size - 1) {
            currentTrip.endTime = chart.size - 1
            currentTrip.finaliseStepsAndCadence()
            tripsList.add(currentTrip)
            currentTrip.finalise(true)

            Logger.i("Trip added ${currentTrip.description()}")
        }
        return tripsList
    }

    /**
     * Once we have the final trips, we remove the ones that are pointless (e.g. stills of 1 minutes
     * or vehicles that are too short, etc.
     *
     */
    private fun correctTrips(): MutableList<TrackerDBTrip> {
        // if the first trip in the day is a still, assign start to midnight
        if (trips.size > 0 && trips[0].activityType == DetectedActivity.STILL) {
            // in general if the first trip is still, then we start the time from midnight of that still.
            // However we have a corner case: the start of the still is == to the end of the still.
            // It is a corner case, not clear why it happens. It happens when the start tag for the still is missing
            // still is followed by a - say - walk that starts on the same minute
            // i.e. item 0 is </STILL> <WALK>
            // if we change the start time of element zero, then we risk that the following activity starts from midnight
            // i.e. walk starts from midnight
            // so we add an element to chart
            if (chart[0].activityOut != INVALID_VALUE) {
                incrementAllTripIndexByOne(trips)
                val element = MobilityData(TimeUtils.midnightInMsecs(chart[0].timeInMSecs))
                element.activityIn = chart[0].activityOut
                chart.add(0, element)
            }
            chart[0].timeInMSecs = TimeUtils.midnightInMsecs(chart[0].timeInMSecs)
            trips[0].startTime = 0
        }
        val finalTrips: MutableList<TrackerDBTrip> = mutableListOf()
        for ((index, trip) in trips.withIndex()) {
            if (trip.activityType == DetectedActivity.STILL && (trip.getDuration(chart) < MobilityComputation.SHORT_ACTIVITY_DURATION)) {
                Logger.i("CorrectTrips: STILL activity too short ${trip.description()}")
                // assign the longest between the activity before and after
                val previousTripDuration = if (index > 0) trips[index - 1].getDuration(chart) else 0
                val nextTripDuration = if (index < trips.size - 1) trips[index + 1].getDuration(chart) else 0
                trip.activityType = if (previousTripDuration > nextTripDuration && previousTripDuration > trip.getDuration(chart)) trips[index - 1].activityType
                else if (nextTripDuration > trip.getDuration(chart)) trips[index + 1].activityType
                else trip.activityType

                trip.tagIfSuspicious()

                // check three elements, discover bike veh bike sequences and normalise veh to bikes

            } else if (index > 0 && index < trips.size - 1
                && trips[index - 1].activityType == DetectedActivity.ON_BICYCLE
                && trip.activityType == DetectedActivity.IN_VEHICLE
                && trips[index - 1].activityType == DetectedActivity.ON_BICYCLE
                && (trips[index - 1].getDuration(chart) + trips[index + 1].getDuration(chart) > trip.getDuration(chart))
            ) {
                Logger.i("Trip corrected ${trip.description()} to ${TrackerDBActivity.getActivityTypeString(DetectedActivity.ON_BICYCLE)}")
                trip.activityType = DetectedActivity.ON_BICYCLE
                Logger.i("CorrectTrips: adding  ${trip.description()}")
                finalTrips.add(trip)
                trip.tagIfSuspicious()

                // check veh - short walk at high speed - veh
                // we are probably walking on a train or a plane
            } else if (index > 0 && index < trips.size - 1
                && trips[index - 1].activityType == DetectedActivity.IN_VEHICLE
                && (trip.activityType in listOf(
                    DetectedActivity.WALKING,
                    DetectedActivity.RUNNING,
                    DetectedActivity.ON_FOOT
                ))
                && (trip.getDuration(chart) < MobilityComputation.SHORT_ACTIVITY_DURATION)
                && trips[index - 1].activityType == DetectedActivity.IN_VEHICLE
                && (trip.getSpeedInMPerSecs() > 11)
            ) {
                Logger.i("Trip corrected ${trip.description()} to ${TrackerDBActivity.getActivityTypeString(DetectedActivity.IN_VEHICLE)}")
                trip.activityType = DetectedActivity.IN_VEHICLE
                Logger.i("CorrectTrips: adding  ${trip.description()}")
                finalTrips.add(trip)
                trip.tagIfSuspicious()
            } else {
                Logger.i("Trip corrected added ${trip.description()}")
                finalTrips.add(trip)
            }
        }
        return finalTrips
    }

    /**
     * If we add an element to the chart in position 0 then we have to inrement all the start/end
     *
     * @param trips
     */
    private fun incrementAllTripIndexByOne(trips: MutableList<TrackerDBTrip>) {
        for (trip in trips) {
            trip.startTime++
            trip.endTime++
        }
    }

    /**
     * This identifies car trips not identified as such (typically identified as stills that move
     * in location substantially. In that case it assigns the elements internal to the activity
     * as of type VEHICLE as default -  it will be corrected later if there is biking around
     * @param context the calling context
     */
    private fun recoverLostActivities(context: Context) {
        val finalTrips = mutableListOf<TrackerDBTrip>()
        for (trip in trips) {
            if ((trip.activityType == DetectedActivity.STILL || trip.activityType == INVALID_VALUE) && trip.getDuration(chart) > MobilityComputation.SHORT_ACTIVITY_DURATION * 2) {
                if (trip.hasAverageHighCadence(chart) || trip.steps > 1000) {
                    trip.activityType = DetectedActivity.WALKING
                    val subTrips = trip.extractWalkingAndOtherActivitiesFromStill(context, DetectedActivity.WALKING)
                    finalTrips.addAll(subTrips)
                } else {
                    finalTrips.addAll(trip.detectVehicleInStills(context))
                }
            } else
                finalTrips.add(trip)
        }
        trips = finalTrips
    }



    /**
     * This checks the suspicious trips and in case it changes the activity type
     * for example if a car trip has a radius of gyration <200m and there is no other driving during the day
     * then it is suspicious
     */
    private fun correctSuspiciousTrips(chart: MutableList<MobilityData>): MutableList<TrackerDBTrip> {
        val summaryData = SummaryData(trips, chart)
        for (trip in trips) {
            if (trip.reliable) continue
            if (trip.activityType == DetectedActivity.IN_VEHICLE && (
                        summaryData.vehicleMsecs < trip.getDuration(chart) * 2
                                // there are cases to consider that are long (>15 mins) veh trips but no locations
                                // || (trip.getDuration(chart) > 5 * MobilityComputation.SHORT_ACTIVITY_DURATION &&
                                || trip.distanceInMeters < 100)
            ) {
                if (trip.getCadence() > 20 || trip.steps > 100) {
                    if (!trip.isSuspicious(trip.activityType, trip.radiusInMeters) && summaryData.cyclingMsecs > trip.getDuration(chart)) {
                        Logger.i("Changing suspicious type from VEHICLE to BIKE")
                        trip.activityType = DetectedActivity.ON_BICYCLE
                    } else if (trip.getCadence() > 20 || trip.steps > 100) {
                        Logger.i("Changing suspicious type from VEHICLE to WALKING")
                        trip.activityType = DetectedActivity.ON_FOOT
                    } else {
                        Logger.i("Changing suspicious type from VEHICLE to STILL")
                        trip.activityType = DetectedActivity.STILL
                    }
                } else {
                    Logger.i("Changing suspicious type from VEHICLE to STILL")
                    trip.activityType = DetectedActivity.STILL
                }
            }
            if (trip.activityType == DetectedActivity.ON_BICYCLE && summaryData.cyclingMsecs < trip.getDuration(chart) * 2) {
                if (trip.getCadence() > 20 || trip.steps > 100) {
                    Logger.i("Changing suspicious type from BIKE to WALKING")
                    trip.activityType = DetectedActivity.ON_FOOT
                } else {
                    Logger.i("Changing suspicious type from BIKE to STILL")
                    trip.activityType = DetectedActivity.STILL
                }
            }
        }
        return trips
    }


    /**
     * This takes sequences of walking walking walking and turns them into just one overarching walking
     */
    private fun compactConsecutiveTrips(): MutableList<TrackerDBTrip> {
        val finalTrips: MutableList<TrackerDBTrip> = mutableListOf()
        if (trips.size > 0) finalTrips.add(trips[0])
        var prevTrip: TrackerDBTrip? = null
        for (index in 1 until trips.size) {
            val currentTrip = trips[index]
            if (prevTrip == null)
                trips[index - 1].also { prevTrip = it }
            val compacted: Boolean = prevTrip!!.compactIfPossible(currentTrip)
            if (!compacted) {
                finalTrips.add(currentTrip)
                prevTrip = currentTrip
            }
        }
        return finalTrips
    }

    /**
     * This considers the locations of each trip and normalises some of them
     * for example a still can have a couple of stay points just because they were a bit far away
     * it is the time to create just one location for the stay point
     */
    private fun finaliseLocations() {
        Logger.i("Finalising locations")
        if (TrackerService.currentTracker != null && TrackerPreferences.config(TrackerService.currentTracker!!).useStayPoints) {
            for (dbTrip in trips) {
                if (dbTrip.activityType == DetectedActivity.STILL) {
                    val allLocations: MutableList<TrackerDBLocation> = mutableListOf()
                    for (location in dbTrip.locations) {
                        allLocations.add(location)
                        if (location.locationsSupportingCentroid.size > 0)
                            allLocations.addAll(location.locationsSupportingCentroid)
                    }
                    val locationUtilities = LocationUtilities()
                    val centroid = locationUtilities.createCentroid(allLocations)
                    dbTrip.locations.clear()
                    dbTrip.locations.add(centroid)
                }
            }
        }
    }
}
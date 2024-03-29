/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.location.Location
import com.google.android.gms.location.DetectedActivity.STILL
import uk.ac.shef.tracker.core.computation.MobilityComputation.Companion.SHORT_ACTIVITY_DURATION
import uk.ac.shef.tracker.core.computation.data.MobilityData
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.database.models.TrackerDBTrip
import kotlin.math.*

/**
 * Utility class that exposes useful methods to manage locations
 */
class LocationUtilities {

    companion object {

        private const val RADIUS_OF_THE_EARTH = 6371
        private const val NUMBER_THRESHOLD = 5
        private const val DISTANCE_THRESHOLD = 25
    }

    /**
     * This computes the distance in meters between two location points
     * This is a modified version of the Haversine distance.
     * Code taken from http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     */
    fun computeDistance(location1: TrackerDBLocation?, location2: TrackerDBLocation?): Double {
        if (location1 == null || location2 == null) return 0.0
        val latitude1: Double = location1.latitude
        val longitude1: Double = location1.longitude
        val altitude1: Double = location1.altitude
        val latitude2: Double = location2.latitude
        val longitude2: Double = location2.longitude
        val altitude2: Double = location2.altitude
        // invalid coordinates
        if (latitude1 == 0.0 && longitude1 == 0.0 || latitude2 == 0.0 && longitude2 == 0.0)
            return 0.0
        val latDistance: Double =
            degreeToRadians(latitude2 - latitude1)
        val lonDistance: Double =
            degreeToRadians(longitude2 - longitude1)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
                + (cos(degreeToRadians(latitude1)) * cos(degreeToRadians(latitude2))
                * sin(lonDistance / 2) * sin(lonDistance / 2)))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        // distance in meters
        var distance = RADIUS_OF_THE_EARTH * c * 1000
        val height = altitude1 - altitude2
        distance = distance.pow(2.0) + height.pow(2.0)
        return (sqrt(distance) * 100.0).roundToInt() / 100.0
    }

    fun computeDistance(location1: Location?, location2: Location?): Double {
        if (location1 != null && location2 != null)
            return computeDistance(TrackerDBLocation(location1), TrackerDBLocation(location2))
        return 0.0
    }

    private fun degreeToRadians(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    fun computeSpeed(prevLocation: TrackerDBLocation, location: TrackerDBLocation): Double {
        val distance = computeDistance(prevLocation, location)
        val timeInSecs = (location.timeInMillis - prevLocation.timeInMillis) / 1000
        if (distance > 0 && timeInSecs > 0) {
            return ((distance / timeInSecs) * 100.0).roundToInt() / 100.0
        }
        return 0.0
    }


    /**
     * Algorithm for the simplification of a trajectory (Synchronous Euclidean Distance). From
     * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.85.9949&rep=rep1&type=pdf
     * voted as one of the best metrics in
     * https://journals.sagepub.com/doi/full/10.1177/15501477211050729#
     *
     * @param locationsList the list of the locations for a trip or day
     * @return the simplified list of locations
     */
    fun simplifyLocationsListUsingSED(locationsList: MutableList<TrackerDBLocation>): MutableList<TrackerDBLocation> {
        if (locationsList.size < 8)
            return locationsList
        val memorySize: Int = (locationsList.size * 0.65).toInt()
        var finalLocationsList: MutableList<TrackerDBLocation> = mutableListOf()
        for (index in 2 until locationsList.size) {
            val prevLoc = locationsList[index - 2]
            val currLoc = locationsList[index - 1]
            val nextLoc = locationsList[index]
            currLoc.sed = computeSynchronousEuclideanDistance(prevLoc, currLoc, nextLoc)
            if (finalLocationsList.size < memorySize)
                finalLocationsList.add(currLoc)
            else {
                finalLocationsList = simplifyLocationsListSEDAUX(finalLocationsList, currLoc)
            }
        }
        return finalLocationsList
    }

    /**
     * Metric for the simplification of a trajectory (Synchronous Euclidean Distance). From
     * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.85.9949&rep=rep1&type=pdf
     * voted as one of the best metrics in
     * https://journals.sagepub.com/doi/full/10.1177/15501477211050729#
     * @param pointA the point before the point to simplify
     * @param pointB the point to simplify
     * @param pointC the point after the one to simplify
     * @return
     */
    private fun computeSynchronousEuclideanDistance(pointA: TrackerDBLocation, pointB: TrackerDBLocation, pointC: TrackerDBLocation): Double {
        val vLatAC = (pointC.latitude - pointA.latitude) / (pointC.timeInMillis - pointA.timeInMillis)
        val vLonAC =
            (pointC.longitude - pointA.longitude) / (pointC.timeInMillis - pointA.timeInMillis)
        val latPB = pointA.latitude + vLatAC * (pointB.timeInMillis - pointA.timeInMillis)
        val lonPB = pointA.longitude + vLonAC * (pointB.timeInMillis - pointA.timeInMillis)
        val sed = sqrt((latPB - pointB.latitude).pow(2) + (lonPB - pointB.longitude).pow(2))
        return sed
    }

    /**
     * Given a list of locations and an element to add it decides if it is the case to add the location
     * by removing another element or to ignooe the current one. The decision is based on the sed of the elements
     * of the list and the sed of the current element. Insertion happens if SED of currElement is >
     * of sed of another list element
     * @param locationsList a list of locations
     * @param currLoc the location we want to add
     * @return teh modified list of locations
     */
    private fun simplifyLocationsListSEDAUX(locationsList: MutableList<TrackerDBLocation>, currLoc: TrackerDBLocation): MutableList<TrackerDBLocation> {
        // how to find the index of the minimum element of a list
        // https://stackoverflow.com/questions/55260175/kotlin-the-most-effective-way-to-find-first-index-of-minimum-element-in-some-li
        val minIndex = locationsList.minByOrNull { it.sed }?.let { locationsList.indexOf(it) }
        val minSEDValue = locationsList.minByOrNull { it.sed }?.sed
        if (minSEDValue != null && minSEDValue < currLoc.sed) {
            locationsList.removeAt(minIndex!!)
            locationsList.add(currLoc)
            // @todo recompute the sed for the two nearby elements
            if (minIndex > 0 && minIndex < locationsList.size - 1)
                computeSynchronousEuclideanDistance(
                    locationsList[minIndex - 1],
                    locationsList[minIndex],
                    locationsList[minIndex + 1]
                )
        }
        // else do nothing - no need to add the current element
        return locationsList
    }


    /**
     * This finds the stay points given a list of locations
     *
     * @param locationsList
     * @return
     */
    fun identifyStayPoint(locationsList: MutableList<TrackerDBLocation>): MutableList<TrackerDBLocation> {
        var index = 0
        val locationUtilities = LocationUtilities()
        val finalLocationsList: MutableList<TrackerDBLocation> = mutableListOf()
        while (index < locationsList.size - 1) {
            val currentLocation = locationsList[index]
            val relatedPoints: MutableList<TrackerDBLocation> = mutableListOf()
            relatedPoints.add(currentLocation)
            var index2 = index + 1
            // the following is necessary because there is no increment on the last element
            // and hence it goes into stack overflow
            while (index2 < locationsList.size) {
                val nextLocation = locationsList[index2]
                val distance = locationUtilities.computeDistance(currentLocation, nextLocation)
                if (distance < DISTANCE_THRESHOLD) {
                    relatedPoints.add(nextLocation)
                    index2++
                    // we have reached the end and we have to conclude this (and the outer) loop
                    if (index2 >= locationsList.size) {
                        finalLocationsList.add(createCentroid(relatedPoints))
                        index = index2
                    }
                } else {
                    if (relatedPoints.size > NUMBER_THRESHOLD) {
                        finalLocationsList.add(createCentroid(relatedPoints))
                        index = index2
                    } else {
                        finalLocationsList.add(currentLocation)
                        index++
                    }
                    break
                }
            }
        }
        return finalLocationsList
    }

    /**
     * Given a list of locations that are related to a stay point, it finds the stay points,
     * i.e. the centroid of the cluster
     * @param relatedPoints
     * @return
     */
    fun createCentroid(relatedPoints: MutableList<TrackerDBLocation>): TrackerDBLocation {
        // if there are no locations, we just put 0,0 as centroid
        if (relatedPoints.size == 0)
            return TrackerDBLocation(0L, 0.0, 0.0, 0.0, 0.0)
        var latitude = 0.0
        var longitude = 0.0
        var altitude = 0.0
        var accuracy = 0.0
        val timeInMillis = relatedPoints[0].timeInMillis
        for (location in relatedPoints) {
            latitude += location.latitude
            longitude += location.longitude
            altitude += location.altitude
            accuracy += location.accuracy
        }
        latitude /= relatedPoints.size
        longitude /= relatedPoints.size
        altitude /= relatedPoints.size
        accuracy /= relatedPoints.size
        val centroid = TrackerDBLocation(timeInMillis, latitude, longitude, accuracy, altitude)
        centroid.locationsSupportingCentroid = relatedPoints
        return centroid
    }


    /**
     * Given an activity and its locations, if the activity is a STILL activity
     * This checks the max distance among its locations. If it is >  MIN_DISTANCE_FOR_MOVEMENTS_IN_METERS
     * then it returns true
     * Note: the current algo does not check if it is really linear. It just checks
     * the max distance among all locations. Some serious zigzag due to
     * interference can still fool the algo but for now I believe it is ok
     * @param activityType the trip's activity type
     * @param locationsList its locations
     * @return true if the max distance among locations >  MIN_DISTANCE_FOR_MOVEMENTS_IN_METERS
     */
    fun isLinearTrajectoryInActivity(locationsList: MutableList<TrackerDBLocation>, useGlobalSED: Boolean?, minimumDistanceInMeters: Int): Boolean {
        var locationCopies: MutableList<TrackerDBLocation> = removeLowAccuracyLocations(locationsList)
        // normalise the locations if not done already
        if (useGlobalSED != null && !useGlobalSED) {
            val locationCopies2: MutableList<TrackerDBLocation> = mutableListOf()
            for (loc in locationCopies) {
                //you cannot use the default copy as the fields are defined as var and those are not copied by the copy
                locationCopies2.add(loc.copyDeep())
            }
            simplifyLocationsListUsingSED(locationCopies2)
            locationCopies = locationCopies2
        }
        for (index in 0 until locationCopies.size - 1)
            for (index2 in index until locationCopies.size) {
                val distance = computeDistance(locationCopies[index], locationCopies[index2])
                if (distance > minimumDistanceInMeters) {
                    return true
                }
            }
        return false
    }

    /**
     * Used to recognise sub trips not recognised by activity recognition - for example a missing
     * car trip lost na still activity
     * @param locations of the activities
     * @param minimumDistanceInMeters the minimum distance to consider two locations as part of a trip
     * @return a list of location timing indicating separated activities
     * (e.g. ( (1234556 1234556 1322234) (33344455 33344433 666545566 ) ... (77766677 86677766))
     */
    fun findLocationMovementInLocationsList(locations: MutableList<TrackerDBLocation>,
                                            minimumDistanceInMeters: Int,
                                            startTime: Long,
                                            endTime: Long,
                                            activityType: Int, chart: MutableList<MobilityData>): MutableList<TrackerDBTrip> {
        val tripsList = mutableListOf<TrackerDBTrip>()
        var prevLocation: TrackerDBLocation? = null
        val initialStartTime = if (locations.size > 0) getChartIndexOfLocation(locations.get(0), chart) else -1
        var newTrip = TrackerDBTrip()
        newTrip.startTime = initialStartTime
        newTrip.endTime = Constants.INVALID
        newTrip.activityType = STILL
        newTrip.chart = chart
        // locations have by construction a first
        for (location in locations) {
            // sometimes we add some contextual locations from the preceding or following activity
            if (location.timeInMillis < startTime || location.timeInMillis > endTime)
                continue
            if (prevLocation != null) {
                // val distance = computeDistance(prevLocation, location)
                val differenceInTime = location.timeInMillis - prevLocation.timeInMillis
                if (differenceInTime <= SHORT_ACTIVITY_DURATION) {
                    newTrip.activityType = activityType
                } else {
                    val locationTime = getChartIndexOfLocation(location, chart)
                    newTrip.endTime = locationTime
                    tripsList.add(newTrip)

                    newTrip = TrackerDBTrip()
                    newTrip.startTime = locationTime
                    newTrip.endTime = Constants.INVALID
                    newTrip.activityType = STILL
                    newTrip.chart = chart
                }
            }
            newTrip.locations.add(location)
            prevLocation = location
        }
        // add the last one as not added yet
        if (newTrip.locations.size > 0) {
            val locationTime = getChartIndexOfLocation(newTrip.locations[newTrip.locations.size - 1], chart)
            newTrip.endTime = locationTime
            tripsList.add(newTrip)
        }
        return tripsList
    }

    /**
     * Given a location, it finds the element in the chart referring to that loation, i.e. the element that
     * has the time immediately > than the location time
     * @param location the location
     * @param chart the chart to look the position in
     * @return the index of the chart element with the time immediately > than the location time
     */
    private fun getChartIndexOfLocation(location: TrackerDBLocation, chart: MutableList<MobilityData>): Int {
        for (index in 0 until chart.size) {
            if (chart[index].timeInMSecs >= location.timeInMillis)
                return index
        }
        return chart.size - 1
    }

    /**
     * Decisions such as turn a still into a vehicle can only be done when the locations
     * have high accuracy
     * @param locationsList the original location list
     * @return a copy of the list containing only the lcoations with accuracy <300m
     */
    fun removeLowAccuracyLocations(locationsList: MutableList<TrackerDBLocation>): MutableList<TrackerDBLocation> {
        val newLocationList: MutableList<TrackerDBLocation> = mutableListOf()
        for (location in locationsList)
            if (location.accuracy < 300)
                newLocationList.add(location)
        return newLocationList
    }

    /**
     * Spikes are locations that are completely out of the current path. We recognise them
     * because the direct distance between the two surrounding points is less than
     * the distance required to reach the point from the surrounding points
     *        p2
     *        /\
     *       /  \
     *      /    \
     *    p1 --- p3
     * p1-2 + p2-3 > 2* p1-3
     * Spikes must be removed because they let the tracker think that a still activity
     * is actually a car activity or that a short walk is actually a walk on a train
     * (which is converted in vehicle)
     * @param originalLocations the locations list
     * @return the filtered locations list
     */
    fun removeSpikes(originalLocations: MutableList<TrackerDBLocation>): MutableList<TrackerDBLocation> {
        var locations = originalLocations
        var removed = true
        var timesLooped = 0
        while (removed) {
            removed = false
            val cleanedLocations: MutableList<TrackerDBLocation> = mutableListOf()
            if (locations.size <= 2) return locations
            cleanedLocations.add(locations[0])
            var skipNext = false
            for (index in 1 until locations.size - 1) {
                if (skipNext) {
                    skipNext = false
                    continue
                }
                val loc1 = locations[index - 1]
                val loc2 = locations[index]
                val loc3 = locations[index + 1]
                val dist1 = computeDistance(loc1, loc2)
                val dist2 = computeDistance(loc2, loc3)
                val dist3 = computeDistance(loc1, loc3)
                if (dist1 + dist2 < dist3 * 2) {
                    cleanedLocations.add(loc2)
                    skipNext = false
                } else {
                    // set skipNext to true if you prefer to remove the location rather than adding the
                    // average location
                    skipNext = false
                    removed = true
                    val correctedLocation = getAverageLocation(loc1, loc2, loc3)
                    cleanedLocations.add(correctedLocation)
                    Logger.i("Correcting location $loc2 as $correctedLocation")
                }
            }
            cleanedLocations.add(locations[locations.size - 1])
            locations = cleanedLocations
            // put a limit here
            if (timesLooped++ > 4)
                removed = false
        }
        return locations
    }

    /**
     * When in a sequence of three locations we find a spike, we produce a location that is the average
     * between the two and remove the spike
     * @param loc1
     * @param loc2 the original location providing the spike
     * @param loc3
     * @return a new DBLocation
     */
    private fun getAverageLocation(loc1: TrackerDBLocation, loc2: TrackerDBLocation, loc3: TrackerDBLocation): TrackerDBLocation {
        return TrackerDBLocation(
            loc2.timeInMillis, (loc1.latitude + loc3.latitude) / 2,
            (loc1.longitude + loc3.longitude) / 2,
            (loc1.accuracy + loc3.accuracy) / 2,
            (loc1.altitude + loc3.altitude) / 2
        )
    }
}
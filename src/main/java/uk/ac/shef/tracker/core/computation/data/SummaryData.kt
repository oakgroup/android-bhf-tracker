/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.computation.data

import com.google.android.gms.location.DetectedActivity
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.database.models.TrackerDBTrip
import uk.ac.shef.tracker.core.utils.Logger

/**
 * The [SummaryData] model
 */
class SummaryData(trips: MutableList<TrackerDBTrip>, chart: MutableList<MobilityData>) {

    var steps: Int = 0
    var vehicleMsecs: Long = 0
    var walkingMsecs: Long = 0
    var runningMsecs: Long = 0
    var cyclingMsecs: Long = 0
    var stillMsecs: Long = 0

    var vehicleDistance: Double = 0.0
    var walkingDistance: Double = 0.0
    var runningDistance: Double = 0.0
    var cyclingDistance: Double = 0.0

    var radius: Int = 0

    var heartMsecs : Long = 0
    var heartDistance :  Double = 0.0

    init {
        Logger.d("Creating day summary results")
        var baseLocation: TrackerDBLocation? = null
        var index = 0
        while (baseLocation == null) {
            if (index == trips.size) break
            if (trips[index].locations.size > 0) {
                baseLocation = trips[index].locations[0]
            }
            index += 1
        }
        for (trip in trips) {
            steps += trip.steps
            when (trip.activityType) {
                DetectedActivity.IN_VEHICLE -> {
                    vehicleMsecs += trip.getDuration(chart)
                    vehicleDistance += trip.distanceInMeters
                }

                DetectedActivity.ON_BICYCLE -> {
                    cyclingMsecs += trip.getDuration(chart)
                    cyclingDistance += trip.distanceInMeters
                }

                in listOf(
                    DetectedActivity.ON_FOOT,
                    DetectedActivity.WALKING
                ) -> {
                    walkingMsecs += trip.getDuration(chart)
                    walkingDistance += trip.distanceInMeters
                    heartMsecs +=  trip.briskMSecs
                    heartDistance += trip.heartDistanceInMeters
                }

                DetectedActivity.RUNNING -> {
                    runningMsecs += trip.getDuration(chart)
                    runningDistance += trip.distanceInMeters
                    heartMsecs +=  trip.briskMSecs
                    heartDistance += trip.heartDistanceInMeters
                }

                DetectedActivity.STILL -> {
                    stillMsecs += trip.getDuration(chart)
                }
            }
            radius = radius.coerceAtLeast(trip.getTripRadius(baseLocation))
        }

        walkingDistance /= 1000
        runningDistance /= 1000
        vehicleDistance /= 1000
        cyclingDistance /= 1000
    }
}

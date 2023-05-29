package com.active.orbit.tracker.core.upload

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.tables.*
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.TimeUtils
import kotlin.math.min

class TrackerUploadUtils(val context: Context) {

    @WorkerThread
    fun dataToSend(limitMillis: Long? = 0): Int {
        val currentMidnight = TimeUtils.midnightInMsecs(System.currentTimeMillis())
        if (limitMillis != null) {
            val unsentActivityCount = TrackerTableActivities.getNotUploadedCountBefore(context, limitMillis)
            val unsentBatteriesCount = TrackerTableBatteries.getNotUploadedCountBefore(context, limitMillis)
            val unsentHeartRatesCount = TrackerTableHeartRates.getNotUploadedCountBefore(context, limitMillis)
            val unsentLocationsCount = TrackerTableLocations.getNotUploadedCountBefore(context, limitMillis)
            val unsentStepsCount = TrackerTableSteps.getNotUploadedCountBefore(context, limitMillis)
            val unsentTripsCount = TrackerTableTrips.getNotUploadedCountBefore(context, min(currentMidnight, limitMillis))
            Logger.d("Unsent data: Activities: $unsentActivityCount Batteries: $unsentBatteriesCount HeartRate: $unsentHeartRatesCount Locations: $unsentLocationsCount Steps: $unsentStepsCount Trips: $unsentTripsCount")
            return unsentActivityCount + unsentBatteriesCount + unsentHeartRatesCount + unsentLocationsCount + unsentStepsCount + unsentTripsCount
        } else {
            val unsentActivityCount = TrackerTableActivities.getNotUploaded(context).size
            val unsentBatteriesCount = TrackerTableBatteries.getNotUploaded(context).size
            val unsentHeartRatesCount = TrackerTableHeartRates.getNotUploaded(context).size
            val unsentLocationsCount = TrackerTableLocations.getNotUploaded(context).size
            val unsentStepsCount = TrackerTableSteps.getNotUploaded(context).size
            val unsentTripsCount = TrackerTableTrips.getNotUploadedBefore(context, currentMidnight).size
            Logger.d("Unsent data: Activities: $unsentActivityCount Batteries: $unsentBatteriesCount HeartRate: $unsentHeartRatesCount Locations: $unsentLocationsCount Steps: $unsentStepsCount Trips: $unsentTripsCount")
            return unsentActivityCount + unsentBatteriesCount + unsentHeartRatesCount + unsentLocationsCount + unsentStepsCount + unsentTripsCount
        }
    }
}
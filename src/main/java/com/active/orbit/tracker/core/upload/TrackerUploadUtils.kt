package com.active.orbit.tracker.core.upload

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.tables.*
import com.active.orbit.tracker.core.utils.Logger

class TrackerUploadUtils(val context: Context) {

    @WorkerThread
    fun dataToSend(limitMillis: Long? = 0): Int {
        if (limitMillis != null) {
            val unsentActivityCount = TrackerTableActivities.getNotUploadedCountBefore(context, limitMillis)
            val unsentBatteriesCount = TrackerTableBatteries.getNotUploadedCountBefore(context, limitMillis)
            val unsentHeartRatesCount = TrackerTableHeartRates.getNotUploadedCountBefore(context, limitMillis)
            val unsentLocationsCount = TrackerTableLocations.getNotUploadedCountBefore(context, limitMillis)
            val unsentStepsCount = TrackerTableSteps.getNotUploadedCountBefore(context, limitMillis)
            val unsentTripsCount = TrackerTableTrips.getNotUploadedCountBefore(context, limitMillis)
            Logger.d("Unsent data: Activities: $unsentActivityCount Batteries: $unsentBatteriesCount HeartRate: $unsentHeartRatesCount Locations: $unsentLocationsCount Steps: $unsentStepsCount Trips: $unsentTripsCount")
            return unsentActivityCount + unsentBatteriesCount + unsentHeartRatesCount + unsentLocationsCount + unsentStepsCount + unsentTripsCount
        } else {
            val unsentActivityCount = TrackerTableActivities.getNotUploaded(context).size
            val unsentBatteriesCount = TrackerTableBatteries.getNotUploaded(context).size
            val unsentHeartRatesCount = TrackerTableHeartRates.getNotUploaded(context).size
            val unsentLocationsCount = TrackerTableLocations.getNotUploaded(context).size
            val unsentStepsCount = TrackerTableSteps.getNotUploaded(context).size
            val unsentTripsCount = TrackerTableTrips.getNotUploaded(context).size
            Logger.d("Unsent data: Activities: $unsentActivityCount Batteries: $unsentBatteriesCount HeartRate: $unsentHeartRatesCount Locations: $unsentLocationsCount Steps: $unsentStepsCount Trips: $unsentTripsCount")
            return unsentActivityCount + unsentBatteriesCount + unsentHeartRatesCount + unsentLocationsCount + unsentStepsCount + unsentTripsCount
        }
    }
}
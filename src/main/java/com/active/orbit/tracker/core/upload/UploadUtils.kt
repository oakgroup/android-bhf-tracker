package com.active.orbit.tracker.core.upload

import android.content.Context
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.core.database.tables.*
import com.active.orbit.tracker.core.utils.Logger

class UploadUtils(val context: Context) {

    @WorkerThread
    fun dataToSend(limitMillis: Long? = 0): Int {
        if (limitMillis != null) {
            val unsentActivityCount = TableActivities.getNotUploadedCountBefore(context, limitMillis)
            val unsentBatteriesCount = TableBatteries.getNotUploadedCountBefore(context, limitMillis)
            val unsentHeartRatesCount = TableHeartRates.getNotUploadedCountBefore(context, limitMillis)
            val unsentLocationsCount = TableLocations.getNotUploadedCountBefore(context, limitMillis)
            val unsentStepsCount = TableSteps.getNotUploadedCountBefore(context, limitMillis)
            val unsentTripsCount = TableTrips.getNotUploadedCountBefore(context, limitMillis)
            Logger.d("Unsent data: Activities: $unsentActivityCount Batteries: $unsentBatteriesCount HeartRate: $unsentHeartRatesCount Locations: $unsentLocationsCount Steps: $unsentStepsCount Trips: $unsentTripsCount")
            return unsentActivityCount + unsentBatteriesCount + unsentHeartRatesCount + unsentLocationsCount + unsentStepsCount + unsentTripsCount
        } else {
            val unsentActivityCount = TableActivities.getNotUploaded(context).size
            val unsentBatteriesCount = TableBatteries.getNotUploaded(context).size
            val unsentHeartRatesCount = TableHeartRates.getNotUploaded(context).size
            val unsentLocationsCount = TableLocations.getNotUploaded(context).size
            val unsentStepsCount = TableSteps.getNotUploaded(context).size
            val unsentTripsCount = TableTrips.getNotUploaded(context).size
            Logger.d("Unsent data: Activities: $unsentActivityCount Batteries: $unsentBatteriesCount HeartRate: $unsentHeartRatesCount Locations: $unsentLocationsCount Steps: $unsentStepsCount Trips: $unsentTripsCount")
            return unsentActivityCount + unsentBatteriesCount + unsentHeartRatesCount + unsentLocationsCount + unsentStepsCount + unsentTripsCount
        }
    }
}
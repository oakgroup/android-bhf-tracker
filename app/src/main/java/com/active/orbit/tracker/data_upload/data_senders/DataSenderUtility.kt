package com.active.orbit.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.active.orbit.tracker.Repository

class DataSenderUtility(val context: Context) {

    private val repositoryInstance: Repository? = Repository.getInstance(context)

    @WorkerThread
    fun dataToSend(limitMillis: Long? = 0): Int {
        if (limitMillis != null) {
            val unsentStepsCount = repositoryInstance?.dBStepsDao?.getUnsentCountBefore(limitMillis) ?: 0
            val unsentActivityCount = repositoryInstance?.dBActivityDao?.getUnsentCountBefore(limitMillis) ?: 0
            val unsentLocationCount = repositoryInstance?.dBLocationDao?.getUnsentCountBefore(limitMillis) ?: 0
            val unsentHeartRateCount = repositoryInstance?.dBHeartRateDao?.getUnsentCountBefore(limitMillis) ?: 0
            val unsentBatteryCount = repositoryInstance?.dBBatteryDao?.getUnsentCountBefore(limitMillis) ?: 0
            Log.d("DataSenderUtility", "UNSENT DATA: Steps: $unsentStepsCount Activities: $unsentActivityCount Locations: $unsentLocationCount HeartRate: $unsentHeartRateCount Battery: $unsentBatteryCount")
            return unsentStepsCount + unsentActivityCount + unsentLocationCount + unsentHeartRateCount + unsentBatteryCount
        } else {
            val unsentStepsCount = repositoryInstance?.dBStepsDao?.getUnsentCount() ?: 0
            val unsentActivityCount = repositoryInstance?.dBActivityDao?.getUnsentCount() ?: 0
            val unsentLocationCount = repositoryInstance?.dBLocationDao?.getUnsentCount() ?: 0
            val unsentHeartRateCount = repositoryInstance?.dBHeartRateDao?.getUnsentCount() ?: 0
            val unsentBatteryCount = repositoryInstance?.dBBatteryDao?.getUnsentCount() ?: 0
            Log.d("DataSenderUtility", "UNSENT DATA: Steps: $unsentStepsCount Activities: $unsentActivityCount Locations: $unsentLocationCount HeartRate: $unsentHeartRateCount Battery: $unsentBatteryCount")
            return unsentStepsCount + unsentActivityCount + unsentLocationCount + unsentHeartRateCount + unsentBatteryCount
        }
    }
}
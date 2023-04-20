package com.active.orbit.tracker.tracker.sensors.batteries

import android.util.Log
import com.active.orbit.tracker.Repository
import com.active.orbit.tracker.tracker.TrackerService
import com.active.orbit.tracker.utils.Utils

class BatteryMonitor(val context: TrackerService, val repository: Repository?) {

    //////////////////////////////////////////////////////////
    //               API METHODS
    //////////////////////////////////////////////////////////

    private var lastBatterySentMillis: Long = 0L

    /**
     * API method to be called to stop the monitor and its restarter controller
     */
    fun insertData() {
        if (lastBatterySentMillis != 0L) {
            // do not save too many batteries data, one every hour maximum
            if ((System.currentTimeMillis() - lastBatterySentMillis) > 3600000) {
                saveBatteryData()
            } else {
                Log.i("BatteryMonitor", "Too early to write battery data")
            }
        } else {
            saveBatteryData()
        }
    }

    private fun saveBatteryData() {
        val batteryData = BatteryData(System.currentTimeMillis(), Utils.getBatteryPercentage(context), Utils.isCharging(context))
        InsertBatteryDataAsync(TrackerService.currentTracker!!, listOf(batteryData))
        Log.i("BatteryMonitor", "Writing battery data $batteryData")
        lastBatterySentMillis = batteryData.timeInMsecs
    }
}
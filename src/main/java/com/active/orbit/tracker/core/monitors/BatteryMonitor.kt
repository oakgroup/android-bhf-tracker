package com.active.orbit.tracker.core.monitors

import com.active.orbit.tracker.core.database.models.DBBattery
import com.active.orbit.tracker.core.database.tables.TableBatteries
import com.active.orbit.tracker.core.tracker.TrackerService
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.Utils

class BatteryMonitor(val context: TrackerService) {

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
                Logger.i("Too early to write battery data")
            }
        } else {
            saveBatteryData()
        }
    }

    private fun saveBatteryData() {
        backgroundThread {
            val dbBattery = DBBattery()
            dbBattery.timeInMillis = System.currentTimeMillis()
            dbBattery.batteryPercent = Utils.getBatteryPercentage(context)
            dbBattery.isCharging = Utils.isCharging(context)
            TableBatteries.upsert(context, dbBattery)
            Logger.d("Writing battery to database ${dbBattery.description()}")
            lastBatterySentMillis = dbBattery.timeInMillis
        }
    }
}
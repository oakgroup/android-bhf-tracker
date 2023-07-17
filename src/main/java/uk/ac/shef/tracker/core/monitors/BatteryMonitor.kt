package uk.ac.shef.tracker.core.monitors

import uk.ac.shef.tracker.core.database.models.TrackerDBBattery
import uk.ac.shef.tracker.core.database.tables.TrackerTableBatteries
import uk.ac.shef.tracker.core.tracker.TrackerService
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.ThreadHandler.backgroundThread
import uk.ac.shef.tracker.core.utils.TrackerUtils

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
            val dbBattery = TrackerDBBattery()
            dbBattery.timeInMillis = System.currentTimeMillis()
            dbBattery.batteryPercent = TrackerUtils.getBatteryPercentage(context)
            dbBattery.isCharging = TrackerUtils.isCharging(context)
            TrackerTableBatteries.upsert(context, dbBattery)
            Logger.d("Writing battery to database ${dbBattery.description()}")
            lastBatterySentMillis = dbBattery.timeInMillis
        }
    }
}
package com.active.orbit.tracker.utils

import android.content.Context
import android.os.BatteryManager
import android.os.SystemClock
import com.active.orbit.tracker.utils.Globals.Companion.MSECS_IN_AN_HOUR
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

    fun millisecondsToString(msecs: Long, format: String): String? {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = Date()
        date.time = msecs
        return simpleDateFormat.format(date)
    }

    fun getTMZoneOffset(time: Long): Int {
        return TimeZone.getDefault().getOffset(time)
    }

    private fun calendarWithLocalTimeZone(msecs: Long): Calendar {
        val tmz = TimeZone.getDefault()
        val calendar = Calendar.getInstance(tmz)
        calendar.timeInMillis = msecs
        return calendar
    }

    /**
     * this was taken from the lecture on local time zone
     */
    fun midnightinMsecs(timeInMsecs: Long): Long {
        var calendar = calendarWithLocalTimeZone(timeInMsecs)
        calendar[Calendar.HOUR_OF_DAY] = 3 // 3am as the summer/winter times change at 3am
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        var midNightTime = calendar.timeInMillis
        // remove the three hours of the summer/winter times changes
        midNightTime -= 10800000
        calendar = calendarWithLocalTimeZone(midNightTime)
        val hour = calendar[Calendar.HOUR_OF_DAY]
        //  day of change to summer time
        if (hour > 12) midNightTime += (24 - hour) * MSECS_IN_AN_HOUR else if (hour < 12)
            midNightTime -= hour * MSECS_IN_AN_HOUR
        return midNightTime
    }

    /**
     * it gets the epoch time (time in MSecs) using the event time that is relative to the last time
     * the phone was rebooted
     * @param timestamp is time is in nanoseconds it represents the set reference times the first time we come here
     * @return the event timestamp in milliseconds from epoch
     * see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
     */
    fun fromEventTimeToEpoch(timestamp: Long): Long {
        // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
        val timePhoneWasLastRebooted =
            System.currentTimeMillis() - SystemClock.elapsedRealtime()
        return timePhoneWasLastRebooted + (timestamp / 1000000.0).toLong()
    }

    /**
     * it gets a time in msecs and returns the rounded time in seconds         *
     * @param timeInMSecs
     * @return
     */
    fun getTimeInSeconds(timeInMSecs: Long): Long {
        return (timeInMSecs / 1000).toLong()
    }

    /**
     * give a duration in msecs prints the duration in teh form of
     *    HH : mm : ss
     *    or mm : ss
     *    or ss Secs
     *
     * @param millis
     * @return
     */
    fun millisecondsToDuration(millis: Long): CharSequence {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(millis)
        )
        val minStr =
            if (hours == 0L && minutes == 0L) "" else if (minutes > 9) minutes.toString() else "0$minutes"
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(millis)
        )
        val secStr = if (seconds > 9) seconds.toString() else "0$seconds"
        var finStr = if (hours == 0L) "" else "$hours:"
        if (minStr == "") {
            finStr = "$secStr Secs"
        } else {
            finStr += "$minStr:$secStr"
        }
        return finStr
    }


    fun isToday(millis: Long): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val calendar2: Calendar = Calendar.getInstance()
        calendar2.timeInMillis = System.currentTimeMillis()
        return calendar[Calendar.DAY_OF_YEAR] == calendar2[Calendar.DAY_OF_YEAR]
    }

    fun getBatteryPercentage(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 0
    }

    fun isCharging(context: Context): Boolean {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
        return bm?.isCharging ?: false
    }
}
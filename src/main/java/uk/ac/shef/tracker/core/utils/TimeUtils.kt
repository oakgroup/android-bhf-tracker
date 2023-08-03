/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.os.SystemClock
import android.text.TextUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class that provides some useful methods to manage timestamps
 */
@Suppress("MemberVisibilityCanBePrivate")
object TimeUtils {

    const val ONE_SECOND_MILLIS = 1000
    const val ONE_MINUTE_MILLIS = 60 * ONE_SECOND_MILLIS
    const val ONE_HOUR_MILLIS = 60 * ONE_MINUTE_MILLIS
    const val ONE_DAY_MILLIS = 24 * ONE_HOUR_MILLIS

    private val utcTimezone = TimeZone.getTimeZone("UTC")
    private val defaultTimezone = TimeZone.getDefault()

    /**
     * This returns a calendar with utc timezone
     */
    fun getUTC(): Calendar {
        return toUTC(getCurrent())
    }

    /**
     * This returns a calendar with current timezone
     */
    fun getCurrent(): Calendar {
        return Calendar.getInstance()
    }

    /**
     * This returns a calendar with current timezone by setting the time in millis
     */
    fun getCurrent(timeInMillis: Long): Calendar {
        val currentCalendar = getCurrent()
        currentCalendar.timeInMillis = timeInMillis
        return currentCalendar
    }

    /**
     * This converts a current timezone calendar into the UTC timezone calendar
     */
    fun toUTC(calendar: Calendar): Calendar {
        val utcCalendar = getCurrent()
        utcCalendar.timeZone = calendar.timeZone
        utcCalendar.timeInMillis = calendar.timeInMillis
        utcCalendar.timeZone = utcTimezone
        return utcCalendar
    }

    /**
     * This converts a UTC timezone calendar into the current timezone calendar
     */
    fun toDefault(calendar: Calendar): Calendar {
        val defaultCalendar = getCurrent()
        defaultCalendar.timeZone = calendar.timeZone
        defaultCalendar.timeInMillis = calendar.timeInMillis
        defaultCalendar.timeZone = defaultTimezone
        return defaultCalendar
    }

    /**
     * This gets the current timezone offset
     */
    fun getTimezoneOffset(time: Long): Int {
        return TimeZone.getDefault().getOffset(time)
    }

    /**
     * This format the input calendar according to the input format
     */
    fun format(calendar: Calendar, toFormat: String): String {
        try {
            val simpleDateFormat = SimpleDateFormat(toFormat, Locale.getDefault())
            simpleDateFormat.calendar = calendar
            return simpleDateFormat.format(calendar.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Constants.EMPTY
    }

    /**
     * This parses the input string with the input format and returns the calendar
     */
    fun parse(date: String?, fromFormat: String): Calendar? {
        if (TextUtils.isEmpty(date)) {
            Logger.e("Trying to parse an empty date string")
            return null
        }
        try {
            val simpleDateFormat = SimpleDateFormat(fromFormat, Locale.getDefault())
            simpleDateFormat.timeZone = utcTimezone
            val dateParsed = simpleDateFormat.parse(date!!)
            if (dateParsed != null) {
                val calendar = getCurrent()
                calendar.time = dateParsed
                return calendar
            } else Logger.e("Parsed date is null $date")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * This convert a date from one format to another
     *
     * @param date date in String
     * @param fromFormat input date format
     * @param toFormat output date format
     * @return the formatted date
     */
    fun convertDate(date: String, fromFormat: String, toFormat: String): String {
        try {
            val calendar = parse(date, fromFormat)
            if (calendar != null) return format(calendar, toFormat)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Constants.EMPTY
    }

    /**
     * This returns the current day [Int]
     */
    private fun getCurrentDay(): Int {
        return getCurrent().get(Calendar.DAY_OF_YEAR)
    }

    /**
     * This returns the current month [Int]
     */
    private fun getCurrentMonth(): Int {
        return getCurrent().get(Calendar.MONTH)
    }

    /**
     * This returns the current year [Int]
     */
    private fun getCurrentYear(): Int {
        return getCurrent().get(Calendar.YEAR)
    }

    /**
     * This returns the current day of month [Int]
     */
    fun getDayOfMonth(calendar: Calendar): Int {
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * This returns true if the input calendar is today
     */
    fun isToday(calendar: Calendar?): Boolean {
        return isThisMonth(calendar) && calendar?.get(Calendar.DAY_OF_YEAR) == getCurrentDay()
    }

    /**
     * This returns true if the input calendar is in this month
     */
    fun isThisMonth(calendar: Calendar?): Boolean {
        return isThisYear(calendar) && calendar?.get(Calendar.MONTH) == getCurrentMonth()
    }

    /**
     * This returns true if the input calendar is in this year
     */
    fun isThisYear(calendar: Calendar?): Boolean {
        return calendar?.get(Calendar.YEAR) == getCurrentYear()
    }

    /**
     * This returns true if the first calendar and the second calendar are in the same day
     */
    fun isSameDay(calendarStart: Calendar?, calendarEnd: Calendar?): Boolean {
        return calendarStart?.get(Calendar.DAY_OF_YEAR) == calendarEnd?.get(Calendar.DAY_OF_YEAR) &&
                calendarStart?.get(Calendar.MONTH) == calendarEnd?.get(Calendar.MONTH) &&
                calendarStart?.get(Calendar.YEAR) == calendarEnd?.get(Calendar.YEAR)
    }

    fun formatHHMMSS(seconds: Long): String {
        val secondsCount = seconds % 60
        var secondsString = secondsCount.toString()
        val minutesCount = seconds / 60 % 60
        var minutesString = minutesCount.toString()
        val hoursCount = seconds / 60 / 60
        var hoursString = hoursCount.toString()
        secondsString = secondsString.padStart(2, '0')
        minutesString = minutesString.padStart(2, '0')
        hoursString = hoursString.padStart(2, '0')
        return "$hoursString:$minutesString:$secondsString"
    }

    fun formatMillis(millis: Long, format: String): String? {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = Date()
        date.time = millis
        return simpleDateFormat.format(date)
    }

    private fun calendarWithLocalTimeZone(msecs: Long): Calendar {
        val tmz = TimeZone.getDefault()
        val calendar = Calendar.getInstance(tmz)
        calendar.timeInMillis = msecs
        return calendar
    }

    /**
     * This was taken from the lecture on local time zone
     */
    fun midnightInMsecs(timeInMsecs: Long): Long {
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
        if (hour > 12) midNightTime += (24 - hour) * TimeUtils.ONE_HOUR_MILLIS else if (hour < 12)
            midNightTime -= hour * TimeUtils.ONE_HOUR_MILLIS
        return midNightTime
    }

    /**
     * This gets the epoch time (time in MSecs) using the event time that is relative to the last time
     * the phone was rebooted
     * @param timestamp is time is in nanoseconds it represents the set reference times the first time we come here
     * @return the event timestamp in milliseconds from epoch
     * see answer 2 at http://stackoverflow.com/questions/5500765/accelerometer-sensorevent-timestamp
     */
    fun fromEventTimeToEpoch(timestamp: Long): Long {
        // http://androidforums.com/threads/how-to-get-time-of-last-system-boot.548661/
        val timePhoneWasLastRebooted = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        return timePhoneWasLastRebooted + (timestamp / 1000000.0).toLong()
    }

    /**
     * This gets a time in msecs and returns the rounded time in seconds         *
     * @param timeInMSecs
     * @return
     */
    fun getTimeInSeconds(timeInMSecs: Long): Long {
        return (timeInMSecs / 1000).toLong()
    }

    /**
     * This returns true if the input millis is today
     */
    fun isToday(millis: Long): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val calendar2: Calendar = Calendar.getInstance()
        calendar2.timeInMillis = System.currentTimeMillis()
        return calendar[Calendar.DAY_OF_YEAR] == calendar2[Calendar.DAY_OF_YEAR]
    }
}
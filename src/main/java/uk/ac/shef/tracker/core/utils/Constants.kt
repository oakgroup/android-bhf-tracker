/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

/**
 * Utility class with constants values
 */
object Constants {

    const val EMPTY = ""
    const val INVALID = -1

    const val DATABASE_ENCRYPTION_KEY = "000Th3D4t4b4s31s3ncrYpt3d?000"

    const val DATE_FORMAT_ID = "yyyyMMddHHmmss"
    const val DATE_FORMAT_UTC = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ssZZZZZ"
    const val DATE_FORMAT_ISO_NO = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    const val DATE_FORMAT_ISO_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.000'Z'"
    const val DATE_FORMAT_FULL = "dd/MM/yyyy HH:mm:ss"
    const val DATE_FORMAT_DAY_MONTH_YEAR = "dd/MM/yyyy"
    const val DATE_FORMAT_MONTH_YEAR = "MMMM yyyy"
    const val DATE_FORMAT_DAY_MONTH = "dd\nMMM"
    const val DATE_FORMAT_HOUR_MINUTE = "HH:mm"
    const val DATE_FORMAT_HOUR_MINUTE_SECONDS = "HH:mm:ss"
    const val DATE_FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd"
    const val DATE_FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_DAY_MONTH_HOUR_MINUTE = "dd MMMM HH:mm"

    const val PRIORITY_CURRENT = -1
    const val PRIORITY_OTHER = 1
    const val PRIORITY_ZERO = 0

    const val MINIMUM_RADIUS_FOR_VEHICLES = 1500
    const val MINIMUM_RADIUS_FOR_BIKES = 300

    // on average if a trip has more than 40 you can think that is a reasonable guess for a missed walk marked as STILL
    const val HIGH_AVERAGE_CADENCE = 40
}
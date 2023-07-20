/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.monitors

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.location.Location
import android.os.Looper
import androidx.annotation.WorkerThread
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import uk.ac.shef.tracker.core.database.models.TrackerDBLocation
import uk.ac.shef.tracker.core.database.tables.TrackerTableLocations
import uk.ac.shef.tracker.core.permissions.TrackerPermissions
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.background
import kotlin.coroutines.CoroutineContext

/**
 * This class monitors the locations data and save them into the database
 */
class LocationMonitor(context: Context) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private var lastRecordedLocation: Location? = null
    private val locationRequest: LocationRequest = LocationRequest.create()
    private val fusedLocationClient: FusedLocationProviderClient?
    private val locationTracker: LocationMonitor

    var currentLocation: Location? = null
    var locationsList: MutableList<TrackerDBLocation> = mutableListOf()

    companion object {

        const val DETECTION_FREQUENCY_IN_MSECS = 5000

        /** This is the delay in returning the locations, used to avoid awakening Android
         * for just one location. Let's pack all the locations in the past minute and return
         * them all at once
         */
        const val DETECTION_DELAY_IN_MSECS = 45000

        /**
         * Do not return locations less distant than 10m
         */
        const val SMALLEST_DISPLACEMENT = 10

        /**
         * Max size of {@link locDataList}
         */
        private const val STANDARD_MAX_SIZE = 40
        private var MAX_SIZE = STANDARD_MAX_SIZE

    }

    init {
        val filter = IntentFilter()
        filter.addAction(javaClass.simpleName)
        locationRequest.interval = DETECTION_FREQUENCY_IN_MSECS.toLong()
        locationRequest.maxWaitTime = DETECTION_DELAY_IN_MSECS.toLong()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.fastestInterval = DETECTION_FREQUENCY_IN_MSECS.toLong()
        locationRequest.smallestDisplacement = SMALLEST_DISPLACEMENT.toFloat()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationTracker = this
    }

    @SuppressLint("MissingPermission")
    fun startLocationTracking(context: Context) {
        if (TrackerPermissions(TrackerPermissions.Group.ACCESS_FINE_LOCATION).check(context)) {
            if (Looper.myLooper() == null) Looper.prepare()
            Looper.myLooper()?.let {
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, it)
                Logger.d("Location tracking started")
            }
        }
    }

    fun stopLocationTracking() {
        Logger.d("Stopping location tracking")
        fusedLocationClient?.flushLocations()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    fun flush(context: Context?) {
        Logger.i("Flushing locations")
        fusedLocationClient?.flushLocations()
        if (context != null) {
            background {
                TrackerTableLocations.upsert(context, locationsList)
                locationsList = mutableListOf()
            }
        }
    }

    fun keepFlushingToDB(context: Context, flush: Boolean) {
        MAX_SIZE = if (flush) {
            flush(context)
            0
        } else STANDARD_MAX_SIZE
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            currentLocation = locationResult.lastLocation
            if (currentLocation != null) addLocation(context, currentLocation!!)
        }
    }


    /**
     * This creates the current location and it stores it in the temporary list
     * if the temp list is overflowing, it will store all the locations into the DB
     * @param context the calling context
     * @param location the Android location element
     */
    fun addLocation(context: Context?, location: Location) {
        val dbLocation = TrackerDBLocation(location)
        Logger.d("Saving location ${dbLocation.description()} ")
        insertLocationIntoDB(context, dbLocation)
        lastRecordedLocation = when {
            lastRecordedLocation == null -> location
            lastRecordedLocation!!.time < location.time -> location
            else -> lastRecordedLocation
        }
    }

    /**
     * This inserts the locations into a temporary list. When the list is overflowing, then it
     * stores the values into the database
     */
    @WorkerThread
    private fun insertLocationIntoDB(context: Context?, location: TrackerDBLocation) {
        locationsList.add(location)
        if (context != null && locationsList.size > MAX_SIZE) {
            background {
                TrackerTableLocations.upsert(context, locationsList)
                locationsList = mutableListOf()
            }
        }
    }
}
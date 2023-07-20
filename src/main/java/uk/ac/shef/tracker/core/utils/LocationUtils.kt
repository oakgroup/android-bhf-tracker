/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.location.Location
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng

/**
 * Utility class that provides some useful methods to manage locations
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object LocationUtils {

    fun toLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    fun toLatLng(latitude: Double, longitude: Double): LatLng {
        return LatLng(latitude, longitude)
    }

    fun toLocation(latLng: LatLng): Location {
        return toLocation(latLng.latitude, latLng.longitude)
    }

    fun toLocation(latitude: Double, longitude: Double): Location {
        val location = Location(LocationManager.GPS_PROVIDER)
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    fun getDistance(locationA: Location, locationB: Location): Float {
        val distance = locationA.distanceTo(locationB)
        Logger.d("Distance between locations is $distance")
        return distance
    }
}
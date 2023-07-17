/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.network

import android.content.Context
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.utils.BaseException

/**
 * Utility enum to declare api urls
 *
 * @author omar.brugna
 */
enum class TrackerApi(private var apiUrl: Int) {
    EMPTY(R.string.tracker_api_empty),
    INSERT_ACTIVITIES(R.string.tracker_api_insert_activities),
    INSERT_BATTERIES(R.string.tracker_api_insert_batteries),
    INSERT_HEART_RATES(R.string.tracker_api_insert_heart_rates),
    INSERT_LOCATIONS(R.string.tracker_api_insert_locations),
    INSERT_STEPS(R.string.tracker_api_insert_steps),
    INSERT_TRIPS(R.string.tracker_api_insert_trips),
    USER_REGISTRATION(R.string.tracker_api_user_registration);

    private var params = ArrayList<String>()

    fun clearParams() {
        params.clear()
    }

    fun addParam(param: String) {
        params.add(param)
    }

    fun getUrl(context: Context): String {
        if (params.isEmpty()) return context.getString(apiUrl)
        if (params.size == 1) return context.getString(apiUrl, params[0])
        if (params.size == 2) return context.getString(apiUrl, params[0], params[1])
        if (params.size == 3) return context.getString(apiUrl, params[0], params[1], params[2])
        if (params.size == 4) return context.getString(apiUrl, params[0], params[1], params[2], params[3])
        if (params.size == 5) return context.getString(apiUrl, params[0], params[1], params[2], params[3], params[4])
        throw BaseException("Current implementation actually supports at least 5 parameters, please add the new cases")
    }
}
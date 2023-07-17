/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences.engine

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Abstract preferences class that should be extended from all utility
 * preferences classes
 *
 * @author omar.brugna
 */
abstract class TrackerBasePreferences {

    protected lateinit var res: Resources
    protected lateinit var prefs: SharedPreferences

    companion object {

        fun logout(context: Context) {
            TrackerPreferences.backend(context).logout()
            TrackerPreferences.lifecycle(context).logout()
            TrackerPreferences.user(context).logout()
            TrackerPreferences.config(context).logout()
        }

        fun printAll(context: Context) {
            val prefs = context.getSharedPreferences(context.getString(R.string.tracker_preference_filename_key), Context.MODE_PRIVATE)
            Logger.d("Stored Preferences")
            for ((key, value) in prefs.all)
                Logger.d("$key - $value")
        }
    }

    internal fun setupPreferences(context: Context) {
        res = context.resources
        prefs = context.getSharedPreferences(context.getString(R.string.tracker_preference_filename_key), Context.MODE_PRIVATE)
    }

    abstract fun logout()
}
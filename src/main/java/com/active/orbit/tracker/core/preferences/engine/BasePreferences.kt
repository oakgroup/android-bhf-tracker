package com.active.orbit.tracker.core.preferences.engine

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.utils.Logger

/**
 * Abstract preferences class that should be extended from all utility
 * preferences classes
 *
 * @author omar.brugna
 */
abstract class BasePreferences {

    protected lateinit var res: Resources
    protected lateinit var prefs: SharedPreferences

    companion object {

        fun logout(context: Context) {
            Preferences.backend(context).logout()
            Preferences.lifecycle(context).logout()
            Preferences.user(context).logout()
            Preferences.tracker(context).logout()
        }

        fun printAll(context: Context) {
            val prefs = context.getSharedPreferences(context.getString(R.string.preference_filename_key), Context.MODE_PRIVATE)
            Logger.d("Stored Preferences")
            for ((key, value) in prefs.all)
                Logger.d("$key - $value")
        }
    }

    internal fun setupPreferences(context: Context) {
        res = context.resources
        prefs = context.getSharedPreferences(context.getString(R.string.preference_filename_key), Context.MODE_PRIVATE)
    }

    abstract fun logout()
}
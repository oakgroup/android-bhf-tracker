/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences.engine

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.utils.Logger

/**
 * Abstract preferences class that should be extended from all the other utility preferences classes
 */
abstract class TrackerBasePreferences {

    protected lateinit var res: Resources
    protected lateinit var prefs: SharedPreferences

    companion object {

        /**
         * This will run the logout method for each preference entity that will remove the data that should be removed
         *
         * @param context an instance of [Context]
         */
        fun logout(context: Context) {
            TrackerPreferences.backend(context).logout()
            TrackerPreferences.lifecycle(context).logout()
            TrackerPreferences.user(context).logout()
            TrackerPreferences.config(context).logout()
        }

        /**
         * This will print all the preferences stored in the tracker filename
         *
         * @param context an instance of [Context]
         */
        fun printAll(context: Context) {
            val prefs = context.getSharedPreferences(context.getString(R.string.tracker_preference_filename_key), Context.MODE_PRIVATE)
            Logger.d("Stored Preferences")
            for ((key, value) in prefs.all)
                Logger.d("$key - $value")
        }
    }

    /**
     * This setup the preferences and it's needed to avoid having references to an old [Context]
     *
     * @param context an instance of [Context]
     */
    internal fun setupPreferences(context: Context) {
        res = context.resources
        prefs = context.getSharedPreferences(context.getString(R.string.tracker_preference_filename_key), Context.MODE_PRIVATE)
    }

    /**
     * This will allow to the preferences entities to define their logout logic
     */
    abstract fun logout()
}
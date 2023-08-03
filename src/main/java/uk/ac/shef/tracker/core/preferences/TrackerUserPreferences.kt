/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences

import android.text.TextUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.preferences.engine.TrackerBasePreferences
import uk.ac.shef.tracker.core.utils.Constants

/**
 * Class that stores all the preferences for the user details
 */
class TrackerUserPreferences : TrackerBasePreferences() {

    /**
     * Check if the current user is registered or not, essentially if we have an user id
     */
    internal fun isUserRegistered(): Boolean {
        return !TextUtils.isEmpty(idUser)
    }

    /**
     * This stores the id of the registered user
     */
    var idUser: String?
        get() = prefs.getString(res.getString(R.string.tracker_preference_user_id_user_key), Constants.EMPTY)
        set(value) {
            val editor = prefs.edit()
            editor.putString(res.getString(R.string.tracker_preference_user_id_user_key), value)
            editor.apply()

            if (!TextUtils.isEmpty(value)) FirebaseCrashlytics.getInstance().setCustomKey("id_user", value!!)
        }

    /**
     * This will be called when the user logout
     */
    override fun logout() {
        idUser = null
    }
}
/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences

import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.preferences.engine.TrackerBasePreferences
import uk.ac.shef.tracker.core.utils.Constants

class TrackerLifecyclePreferences : TrackerBasePreferences() {

    var firstInstall: Long?
        get() = prefs.getLong(res.getString(R.string.tracker_preference_lifecycle_first_install_key), Constants.INVALID.toLong())
        set(value) {
            val editor = prefs.edit()
            if (value != null) editor.putLong(res.getString(R.string.tracker_preference_lifecycle_first_install_key), value)
            else editor.remove(res.getString(R.string.tracker_preference_lifecycle_first_install_key))
            editor.apply()
        }

    var lastTripsUpload: Long?
        get() = prefs.getLong(res.getString(R.string.tracker_preference_lifecycle_last_trips_upload_key), Constants.INVALID.toLong())
        set(value) {
            val editor = prefs.edit()
            if (value != null) editor.putLong(res.getString(R.string.tracker_preference_lifecycle_last_trips_upload_key), value)
            else editor.remove(res.getString(R.string.tracker_preference_lifecycle_last_trips_upload_key))
            editor.apply()
        }

    override fun logout() {
        firstInstall = null
        lastTripsUpload = null
    }
}
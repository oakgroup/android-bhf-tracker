package com.active.orbit.tracker.core.preferences

import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.TrackerBasePreferences
import com.active.orbit.tracker.core.utils.Constants

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
/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences

import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.preferences.engine.TrackerBasePreferences
import uk.ac.shef.tracker.core.utils.Constants

/**
 * Class that stores all the preferences for the backend configurations
 */
class TrackerBackendPreferences : TrackerBasePreferences() {

    /**
     * The tracker base url used for the network requests
     */
    var baseUrl: String
        get() = prefs.getString(res.getString(R.string.tracker_preference_backend_base_url_key), Constants.EMPTY) ?: Constants.EMPTY
        set(value) {
            val editor = prefs.edit()
            editor.putString(res.getString(R.string.tracker_preference_backend_base_url_key), value)
            editor.apply()
        }

    /**
     * This flag indicates if the tracker should upload the data to the server or not
     */
    var uploadData: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_backend_upload_data_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_backend_upload_data_key), value)
            editor.apply()
        }

    /**
     * This will be called when the user logout
     */
    override fun logout() {
        // baseUrl = Constants.EMPTY
        // uploadData = false
    }
}
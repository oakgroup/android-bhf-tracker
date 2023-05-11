package com.active.orbit.tracker.core.preferences

import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.BasePreferences
import com.active.orbit.tracker.core.utils.Constants

class BackendPreferences : BasePreferences() {

    var baseUrl: String
        get() = prefs.getString(res.getString(R.string.preference_backend_base_url_key), Constants.EMPTY) ?: Constants.EMPTY
        set(value) {
            val editor = prefs.edit()
            editor.putString(res.getString(R.string.preference_backend_base_url_key), value)
            editor.apply()
        }

    var uploadData: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_backend_upload_data_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_backend_upload_data_key), value)
            editor.apply()
        }

    override fun logout() {
        // baseUrl = Constants.EMPTY
        // uploadData = false
    }
}
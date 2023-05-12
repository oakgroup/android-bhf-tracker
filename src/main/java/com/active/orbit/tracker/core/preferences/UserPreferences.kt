package com.active.orbit.tracker.core.preferences

import android.text.TextUtils
import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.BasePreferences
import com.active.orbit.tracker.core.utils.Constants
import com.google.firebase.crashlytics.FirebaseCrashlytics

class UserPreferences : BasePreferences() {

    internal fun register(idUser: String, idProgram: String) {
        this.idUser = idUser
    }

    internal fun isUserRegistered(): Boolean {
        return !TextUtils.isEmpty(idUser)
    }

    var idUser: String?
        get() = prefs.getString(res.getString(R.string.tracker_preference_user_id_user_key), Constants.EMPTY)
        set(value) {
            val editor = prefs.edit()
            editor.putString(res.getString(R.string.tracker_preference_user_id_user_key), value)
            editor.apply()

            if (!TextUtils.isEmpty(value)) FirebaseCrashlytics.getInstance().setCustomKey("id_user", value!!)
        }


    override fun logout() {
        idUser = null
    }
}
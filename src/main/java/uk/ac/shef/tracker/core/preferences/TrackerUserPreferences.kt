/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences

import android.text.TextUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.preferences.engine.TrackerBasePreferences
import uk.ac.shef.tracker.core.utils.Constants

class TrackerUserPreferences : TrackerBasePreferences() {

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
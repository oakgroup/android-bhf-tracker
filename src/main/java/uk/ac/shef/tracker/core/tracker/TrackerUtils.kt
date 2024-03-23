package uk.ac.shef.tracker.core.tracker

import android.content.Context
import uk.ac.shef.tracker.core.managers.TrackerUserManager
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger

class TrackerUtils {
    /**
     * If the user is not registered with the server, it registers it
     * TODO this method should be used if the tracker wants to register the patient automatically
     */
    fun checkUserRegistration(context: Context) {
        if (!TrackerPreferences.user(context).isUserRegistered()) {
            Logger.i("Registering user")
            TrackerUserManager.registerUser(context)
        } else Logger.i("User already registered with id ${TrackerPreferences.user(context).idUser}")
    }

    /**
     * This will allow the client app to store the user id when it's require that the app manages the user registration
     */
    fun saveUserRegistrationId(context: Context, userId: String?) {
        TrackerPreferences.user(context).idUser = userId

        val firstInstall = TrackerPreferences.lifecycle(context).firstInstall
        if (firstInstall == null || firstInstall == Constants.INVALID.toLong()) {
            TrackerPreferences.lifecycle(context).firstInstall = System.currentTimeMillis()
        }
    }

}
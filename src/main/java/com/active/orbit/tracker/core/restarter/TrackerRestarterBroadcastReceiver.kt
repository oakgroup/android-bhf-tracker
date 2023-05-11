package com.active.orbit.tracker.core.restarter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.active.orbit.tracker.core.permissions.Permissions
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.TimeUtils
import com.google.firebase.FirebaseApp

class TrackerRestarterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        FirebaseApp.initializeApp(context)
        Logger.d("Restarter broadcast received!")
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Logger.d("Package reinstalled")
            Preferences.lifecycle(context).firstInstall = TimeUtils.getCurrent().timeInMillis
        }

        val permissionGranted = Permissions(Permissions.Group.ACCESS_FINE_LOCATION).check(context)
        if (permissionGranted) {
            val trackerRestarter = TrackerRestarter()
            Logger.i("Starting tracker")
            trackerRestarter.startTrackerAndDataUpload(context)
        } else {
            Logger.i("No permissions set yet, waiting to start the tracker")
        }
    }
}
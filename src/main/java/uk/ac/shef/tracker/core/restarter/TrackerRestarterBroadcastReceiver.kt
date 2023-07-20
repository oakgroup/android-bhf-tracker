/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.restarter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.FirebaseApp
import uk.ac.shef.tracker.core.permissions.TrackerPermissions
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.TimeUtils

/**
 * Broadcast receive to start the tracker when needed
 */
class TrackerRestarterBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        FirebaseApp.initializeApp(context)
        Logger.d("Restarter broadcast received!")
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Logger.d("Package reinstalled")
            TrackerPreferences.lifecycle(context).firstInstall = TimeUtils.getCurrent().timeInMillis
        }

        val permissionGranted = TrackerPermissions(TrackerPermissions.Group.ACCESS_FINE_LOCATION).check(context)
        if (permissionGranted) {
            val trackerRestarter = TrackerRestarter()
            Logger.i("Starting tracker")
            trackerRestarter.startTrackerAndDataUpload(context)
        } else {
            Logger.i("No permissions set yet, waiting to start the tracker")
        }
    }
}
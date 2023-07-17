/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences

import uk.ac.shef.tracker.R
import uk.ac.shef.tracker.core.preferences.engine.TrackerBasePreferences

class TrackerConfigurationPreferences : TrackerBasePreferences() {

    var useActivityRecognition: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_activity_recognition_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_activity_recognition_key), value)
            editor.apply()
        }

    var useLocationTracking: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_location_tracking_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_location_tracking_key), value)
            editor.apply()
        }

    var useStepCounter: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_step_counter_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_step_counter_key), value)
            editor.apply()
        }

    var useHeartRateMonitor: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_heart_rate_monitor_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_heart_rate_monitor_key), value)
            editor.apply()
        }

    var useMobilityModelling: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_mobility_modelling_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_mobility_modelling_key), value)
            editor.apply()
        }

    var useBatteryMonitor: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_battery_monitor_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_battery_monitor_key), value)
            editor.apply()
        }

    var useStayPoints: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_stay_points_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_use_stay_points_key), value)
            editor.apply()
        }

    var compactLocations: Boolean
        get() = prefs.getBoolean(res.getString(R.string.configuration_preference_tracker_tracker_compact_locations_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.configuration_preference_tracker_tracker_compact_locations_key), value)
            editor.apply()
        }

    override fun logout() {
        // useActivityRecognition = false
        // useLocationTracking = false
        // useStepCounter = false
        // useHeartRateMonitor = false
        // useMobilityModelling = false
        // useBatteryMonitor = false
        // useStayPoints = false
        // compactLocations = false
    }
}
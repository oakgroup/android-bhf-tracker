package com.active.orbit.tracker.core.preferences

import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.BasePreferences

class TrackerPreferences : BasePreferences() {

    var useActivityRecognition: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_activity_recognition_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_activity_recognition_key), value)
            editor.apply()
        }

    var useLocationTracking: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_location_tracking_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_location_tracking_key), value)
            editor.apply()
        }

    var useStepCounter: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_step_counter_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_step_counter_key), value)
            editor.apply()
        }

    var useHeartRateMonitor: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_heart_rate_monitor_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_heart_rate_monitor_key), value)
            editor.apply()
        }

    var useMobilityModelling: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_mobility_modelling_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_mobility_modelling_key), value)
            editor.apply()
        }

    var useBatteryMonitor: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_battery_monitor_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_battery_monitor_key), value)
            editor.apply()
        }

    var useStayPoints: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_stay_points_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_use_stay_points_key), value)
            editor.apply()
        }

    var compactLocations: Boolean
        get() = prefs.getBoolean(res.getString(R.string.tracker_preference_tracker_tracker_compact_locations_key), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.tracker_preference_tracker_tracker_compact_locations_key), value)
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
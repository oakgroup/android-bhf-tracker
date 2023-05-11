package com.active.orbit.tracker.core.preferences

import com.active.orbit.tracker.R
import com.active.orbit.tracker.core.preferences.engine.BasePreferences

class TrackerPreferences : BasePreferences() {

    var useActivityRecognition: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_activity_recognition), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_activity_recognition), value)
            editor.apply()
        }

    var useLocationTracking: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_location_tracking), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_location_tracking), value)
            editor.apply()
        }

    var useStepCounter: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_step_counter), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_step_counter), value)
            editor.apply()
        }

    var useHeartRateMonitor: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_heart_rate_monitor), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_heart_rate_monitor), value)
            editor.apply()
        }

    var useMobilityModelling: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_mobility_modelling), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_mobility_modelling), value)
            editor.apply()
        }

    var useBatteryMonitor: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_battery_monitor), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_battery_monitor), value)
            editor.apply()
        }

    var useStayPoints: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_use_stay_points), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_use_stay_points), value)
            editor.apply()
        }

    var compactLocations: Boolean
        get() = prefs.getBoolean(res.getString(R.string.preference_tracker_tracker_compact_locations), false)
        set(value) {
            val editor = prefs.edit()
            editor.putBoolean(res.getString(R.string.preference_tracker_tracker_compact_locations), value)
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
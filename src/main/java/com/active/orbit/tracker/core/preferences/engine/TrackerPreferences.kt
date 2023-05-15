package com.active.orbit.tracker.core.preferences.engine

import android.content.Context
import com.active.orbit.tracker.core.preferences.TrackerBackendPreferences
import com.active.orbit.tracker.core.preferences.TrackerConfigurationPreferences
import com.active.orbit.tracker.core.preferences.TrackerLifecyclePreferences
import com.active.orbit.tracker.core.preferences.TrackerUserPreferences

object TrackerPreferences {

    @Volatile
    private var backendPreferences: TrackerBackendPreferences? = null

    @Volatile
    private var configurationPreferences: TrackerConfigurationPreferences? = null

    @Volatile
    private var lifecyclePreferences: TrackerLifecyclePreferences? = null

    @Volatile
    private var userPreferences: TrackerUserPreferences? = null

    @Synchronized
    fun backend(context: Context): TrackerBackendPreferences {
        if (backendPreferences == null) backendPreferences = TrackerBackendPreferences()
        backendPreferences!!.setupPreferences(context)
        return backendPreferences!!
    }

    @Synchronized
    fun config(context: Context): TrackerConfigurationPreferences {
        if (configurationPreferences == null) configurationPreferences = TrackerConfigurationPreferences()
        configurationPreferences!!.setupPreferences(context)
        return configurationPreferences!!
    }

    @Synchronized
    fun lifecycle(context: Context): TrackerLifecyclePreferences {
        if (lifecyclePreferences == null) lifecyclePreferences = TrackerLifecyclePreferences()
        lifecyclePreferences!!.setupPreferences(context)
        return lifecyclePreferences!!
    }

    @Synchronized
    fun user(context: Context): TrackerUserPreferences {
        if (userPreferences == null) userPreferences = TrackerUserPreferences()
        userPreferences!!.setupPreferences(context)
        return userPreferences!!
    }
}
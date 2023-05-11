package com.active.orbit.tracker.core.preferences.engine

import android.content.Context
import com.active.orbit.tracker.core.preferences.BackendPreferences
import com.active.orbit.tracker.core.preferences.LifecyclePreferences
import com.active.orbit.tracker.core.preferences.TrackerPreferences
import com.active.orbit.tracker.core.preferences.UserPreferences

object Preferences {

    @Volatile
    private var backendPreferences: BackendPreferences? = null

    @Volatile
    private var lifecyclePreferences: LifecyclePreferences? = null

    @Volatile
    private var trackerPreferences: TrackerPreferences? = null

    @Volatile
    private var userPreferences: UserPreferences? = null

    @Synchronized
    fun backend(context: Context): BackendPreferences {
        if (backendPreferences == null) backendPreferences = BackendPreferences()
        backendPreferences!!.setupPreferences(context)
        return backendPreferences!!
    }

    @Synchronized
    fun lifecycle(context: Context): LifecyclePreferences {
        if (lifecyclePreferences == null) lifecyclePreferences = LifecyclePreferences()
        lifecyclePreferences!!.setupPreferences(context)
        return lifecyclePreferences!!
    }

    @Synchronized
    fun user(context: Context): UserPreferences {
        if (userPreferences == null) userPreferences = UserPreferences()
        userPreferences!!.setupPreferences(context)
        return userPreferences!!
    }

    @Synchronized
    fun tracker(context: Context): TrackerPreferences {
        if (trackerPreferences == null) trackerPreferences = TrackerPreferences()
        trackerPreferences!!.setupPreferences(context)
        return trackerPreferences!!
    }
}
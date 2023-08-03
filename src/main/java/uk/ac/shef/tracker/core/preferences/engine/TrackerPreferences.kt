/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences.engine

import android.content.Context
import uk.ac.shef.tracker.core.preferences.TrackerBackendPreferences
import uk.ac.shef.tracker.core.preferences.TrackerConfigurationPreferences
import uk.ac.shef.tracker.core.preferences.TrackerLifecyclePreferences
import uk.ac.shef.tracker.core.preferences.TrackerUserPreferences

/**
 * Utility class that declares a singleton instance of all the preferences entities
 */
object TrackerPreferences {

    @Volatile
    private var backendPreferences: TrackerBackendPreferences? = null

    @Volatile
    private var configurationPreferences: TrackerConfigurationPreferences? = null

    @Volatile
    private var lifecyclePreferences: TrackerLifecyclePreferences? = null

    @Volatile
    private var userPreferences: TrackerUserPreferences? = null

    /**
     * @param context an instance of [Context]
     * @return the [TrackerBackendPreferences] entity
     */
    @Synchronized
    fun backend(context: Context): TrackerBackendPreferences {
        if (backendPreferences == null) backendPreferences = TrackerBackendPreferences()
        backendPreferences!!.setupPreferences(context)
        return backendPreferences!!
    }

    /**
     * @param context an instance of [Context]
     * @return the [TrackerConfigurationPreferences] entity
     */
    @Synchronized
    fun config(context: Context): TrackerConfigurationPreferences {
        if (configurationPreferences == null) configurationPreferences = TrackerConfigurationPreferences()
        configurationPreferences!!.setupPreferences(context)
        return configurationPreferences!!
    }

    /**
     * @param context an instance of [Context]
     * @return the [TrackerLifecyclePreferences] entity
     */
    @Synchronized
    fun lifecycle(context: Context): TrackerLifecyclePreferences {
        if (lifecyclePreferences == null) lifecyclePreferences = TrackerLifecyclePreferences()
        lifecyclePreferences!!.setupPreferences(context)
        return lifecyclePreferences!!
    }

    /**
     * @param context an instance of [Context]
     * @return the [TrackerUserPreferences] entity
     */
    @Synchronized
    fun user(context: Context): TrackerUserPreferences {
        if (userPreferences == null) userPreferences = TrackerUserPreferences()
        userPreferences!!.setupPreferences(context)
        return userPreferences!!
    }
}
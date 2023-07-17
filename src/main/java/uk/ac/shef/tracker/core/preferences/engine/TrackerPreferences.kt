/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.preferences.engine

import android.content.Context
import uk.ac.shef.tracker.core.preferences.TrackerBackendPreferences
import uk.ac.shef.tracker.core.preferences.TrackerConfigurationPreferences
import uk.ac.shef.tracker.core.preferences.TrackerLifecyclePreferences
import uk.ac.shef.tracker.core.preferences.TrackerUserPreferences

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
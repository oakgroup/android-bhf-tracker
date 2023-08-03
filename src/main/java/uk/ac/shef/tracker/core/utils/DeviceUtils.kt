/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.os.Build
import android.text.TextUtils
import java.util.Locale

/**
 * Utility class that provides some useful methods to get device information
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object DeviceUtils {

    fun getDeviceName(): String? {
        val model = Build.MODEL.lowercase(Locale.getDefault())
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        if (!TextUtils.isEmpty(model)) {
            return if (model.startsWith(manufacturer)) {
                model
            } else {
                "$manufacturer $model"
            }
        }
        return null
    }

    fun getDeviceOSVersion(): String {
        return Build.VERSION.RELEASE
    }
}
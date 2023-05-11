package com.active.orbit.tracker.core.utils

import android.os.Build
import android.text.TextUtils
import java.util.*

/**
 * Utility class that provides some useful methods to get device information
 *
 * @author omar.brugna
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
/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.utils

import android.content.pm.PackageInfo
import android.os.Build

/**
 * Utility class that provides some useful methods to manage different os versions
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object VersionUtils {

    val isMarshMallow: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.M

    val isNougat: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.N

    val isOreo: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.O

    val isPie: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.P

    val isAndroid10: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.Q

    fun hasMarshMallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun hasNougat(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    fun hasOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun hasPie(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun hasAndroid10(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    @Suppress("DEPRECATION")
    fun versionCode(packageInfo: PackageInfo): Long {
        return if (hasPie()) packageInfo.longVersionCode
        else packageInfo.versionCode.toLong()
    }
}
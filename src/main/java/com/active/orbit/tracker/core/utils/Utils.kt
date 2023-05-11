package com.active.orbit.tracker.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.active.orbit.tracker.R
import java.util.*

/**
 * Utility class that provides some general useful methods
 *
 * @author omar.brugna
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object Utils {

    fun getAppName(context: Context): String {
        return context.resources.getString(R.string.app_name)
    }

    fun getPackageName(context: Context): String {
        val packageManager = context.packageManager
        if (packageManager != null) {
            try {
                val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
                if (packageInfo != null) return packageInfo.packageName
                else Logger.e("Package info is null")
            } catch (e: Exception) {
                Logger.e("Exception getting package name")
            }
        }
        return Constants.EMPTY
    }

    fun getVersionName(context: Context): String {
        val packageManager = context.packageManager
        if (packageManager != null) {
            try {
                val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
                if (packageInfo != null) return packageInfo.versionName
                else Logger.e("Package info is null")
            } catch (e: Exception) {
                Logger.e("Exception getting version name")
            }
        }
        return Constants.EMPTY
    }

    fun getVersionCode(context: Context): Long {
        val packageManager = context.packageManager
        if (packageManager != null) {
            try {
                val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
                if (packageInfo != null) return VersionUtils.versionCode(packageInfo)
                else Logger.e("Package info is null")
            } catch (e: Exception) {
                Logger.e("Exception getting version code")
            }
        }
        return 0
    }

    fun getPhoneModel(): String {
        return Build.MODEL + " " + Build.PRODUCT + "" + Build.BOARD
    }

    fun getAndroidVersion(): String {
        return ("Android:" + Build.VERSION.RELEASE + " " + Build.VERSION.INCREMENTAL)
    }

    fun getAppVersion(context: Context): String {
        var versionName = Constants.EMPTY
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = "Android " + packageInfo.versionName
        } catch (e: java.lang.Exception) {
            Logger.e("Error retrieving app version" + (e.localizedMessage ?: Constants.EMPTY))
        }
        return versionName
    }

    fun delay(milliseconds: Long, runnable: Runnable) {
        Handler(Looper.getMainLooper()).postDelayed(runnable, milliseconds)
    }

    /**
     * Generate a random number between min and max (included)
     *
     * @param min minimum number
     * @param max maximum number
     * @return the generated random number in range
     */
    fun randomNumber(min: Int, max: Int): Int {
        return (min..max).random()
    }

    /**
     * Generate a random string
     * @return the generated random string
     */
    fun randomString(): String {
        return UUID.randomUUID().toString()
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.copy), text)
        clipboard.setPrimaryClip(clip)
    }

    fun showKeyboard(view: View?) {
        if (view != null && !isKeyboardOpen(view)) {
            val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager?.showSoftInput(view, InputMethodManager.SHOW_FORCED)
        }
    }

    fun hideKeyboard(view: View?) {
        if (view != null && isKeyboardOpen(view)) {
            val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputManager?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun isKeyboardOpen(view: View?): Boolean {
        if (view != null) {
            val inputManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            return inputManager?.isAcceptingText ?: false
        }
        return false
    }

    fun getBatteryPercentage(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: Constants.INVALID
    }

    fun isCharging(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager?
            bm?.isCharging ?: false
        } else {
            false
        }
    }
}
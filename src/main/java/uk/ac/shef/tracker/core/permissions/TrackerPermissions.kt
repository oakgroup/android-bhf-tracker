package uk.ac.shef.tracker.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
 * Utility class to manage runtime permissions
 *
 * @author omar.brugna
 */
class TrackerPermissions(val group: Group) {

    companion object {
        private const val REQUEST_ACCESS_FINE_LOCATION = 0
        private const val REQUEST_ACCESS_BACKGROUND_LOCATION = 1
        private const val REQUEST_ACCESS_EXTERNAL_STORAGE = 2
        private const val REQUEST_ACCESS_ACTIVITY_RECOGNITION = 3
        private const val REQUEST_ACCESS_CAMERA_FOR_SCAN = 4
        private const val REQUEST_ACCESS_CAMERA_FOR_CAPTURE = 5
    }

    fun check(context: Context): Boolean {
        for (permission in group.permissions) {
            val status = ActivityCompat.checkSelfPermission(context, permission)
            if (status != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    fun shouldShowExplanation(activity: AppCompatActivity): Boolean {
        for (permission in group.permissions) {
            val status = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            if (status) return true
        }
        return false
    }

    fun request(activity: AppCompatActivity) {
        ActivityCompat.requestPermissions(activity, group.permissions, group.requestCode)
    }

    enum class Group(val permissions: Array<String>, val requestCode: Int) {
        ACCESS_FINE_LOCATION(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_ACCESS_FINE_LOCATION
        ),

        @RequiresApi(Build.VERSION_CODES.Q)
        ACCESS_BACKGROUND_LOCATION(
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_ACCESS_BACKGROUND_LOCATION
        ),
        ACCESS_EXTERNAL_STORAGE(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_ACCESS_EXTERNAL_STORAGE
        ),

        @RequiresApi(Build.VERSION_CODES.Q)
        ACCESS_ACTIVITY_RECOGNITION(
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            REQUEST_ACCESS_ACTIVITY_RECOGNITION
        ),
        ACCESS_CAMERA_FOR_SCAN(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_ACCESS_CAMERA_FOR_SCAN
        ),
        ACCESS_CAMERA_FOR_CAPTURE(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_ACCESS_CAMERA_FOR_CAPTURE
        )
    }
}
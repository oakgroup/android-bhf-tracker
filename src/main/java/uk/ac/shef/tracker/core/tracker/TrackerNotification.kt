/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.tracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN

/**
 * Created by Iman on 08/09/2016.
 */
class TrackerNotification @JvmOverloads constructor(context: Context, private val mId: Int, runningInBackground: Boolean = false) {

    private var notificationBuilder: Builder? = null
    var notification: Notification? = null
    private var notificationPendingIntent: PendingIntent? = null
    private var notificationManager: NotificationManager? = null

    companion object {

        private val CHANNEL_ID = Notification::javaClass.name
        var notificationIcon = 0
        var notificationTitle: String? = null
        var thoughtOfTheDay: String? = null
        var notificationText: String? = null
    }

    init {
        notification = if (runningInBackground) {
            val message = "Running in the background"
            val title = "Insert notification title"
            setNotification(context, message)
        } else {
            val thoughOfTheDay =
                thoughtOfTheDay
            val notificationTitle =
                notificationTitle
            val notificationIcon =
                notificationIcon
            setNotification(context, notificationTitle, thoughOfTheDay, notificationIcon)
        }
    }

    /**
     * This is the method that can be called to update the TrackerNotification
     */
    private fun setNotification(context: Context, title: String?, text: String?, icon: Int): Notification {
//        @TODO to open the app when the notification is pressed
//        if (notificationPendingIntent == null) {
//            if (CoreApplication.interfaceInstance != null) {
//                // get activity from the app that uses this core module
//                val activity: Class<*> = CoreApplication.interfaceInstance.getMainActivity()
//                val notificationIntent = Intent(context, activity)
//                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                // notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                notificationPendingIntent =
//                    PendingIntent.getActivity(
//                        context,
//                        0,
//                        notificationIntent,
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//            }
//        }
        val notification: Notification
        notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // OREO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val name: CharSequence = "Permanent TrackerNotification"
            //mContext.getString(R.string.channel_name);
            val importance: Int = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            //String description = mContext.getString(R.string.notifications_description);
            val description = "I would like to receive travel alerts and notifications for:"
            channel.description = description
            notificationBuilder = Builder(context, CHANNEL_ID)
            if (notificationManager != null) {
                notificationManager!!.createNotificationChannel(channel)
            }
            notification =
                notificationBuilder!!
                    .setSmallIcon(icon)
                    // .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(notificationPendingIntent)
                    .setVisibility(VISIBILITY_PRIVATE)
                    .build()
        } else {
            notification = Builder(
                context,
                "channelID"
            )
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_MIN
                    else IMPORTANCE_MIN
                )
                .setContentIntent(notificationPendingIntent)
                .setVisibility(VISIBILITY_PRIVATE)
                .build()
        }
        return notification
    }


    private fun setNotification(context: Context, text: String?): Notification {
        return setNotification(context, notificationTitle, text, notificationIcon)
    }

}
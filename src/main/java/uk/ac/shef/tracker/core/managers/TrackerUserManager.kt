/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.managers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import uk.ac.shef.tracker.core.deserialization.UserRegistrationMap
import uk.ac.shef.tracker.core.listeners.UserRegistrationListener
import uk.ac.shef.tracker.core.network.TrackerApi
import uk.ac.shef.tracker.core.network.TrackerConnection
import uk.ac.shef.tracker.core.network.TrackerConnectionListener
import uk.ac.shef.tracker.core.network.TrackerWebService
import uk.ac.shef.tracker.core.serialization.UserRegistrationRequest
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.TimeUtils
import uk.ac.shef.tracker.core.utils.TrackerUtils

/**
 * Utility class to manage the apis requests relative to the current user
 */
object TrackerUserManager {

    /**
     * Try to register an user with the server registration api
     *
     * @param context an instance of [Context]
     * @param listener an optional listener to receive the registration callbacks
     */
    fun registerUser(context: Context, listener: UserRegistrationListener? = null) {

        val request = UserRegistrationRequest()
        request.phoneModel = TrackerUtils.getPhoneModel()
        request.appVersion = TrackerUtils.getAppVersion(context)
        request.androidVersion = TrackerUtils.getAndroidVersion()
        request.batteryLevel = TrackerUtils.getBatteryPercentage(context)
        request.isCharging = TrackerUtils.isCharging(context)
        request.registrationTimestamp = TimeUtils.getCurrent().timeInMillis

        if (!request.isValid()) {
            Logger.e("Invalid user registration request")
            listener?.onError()
            return
        }

        val webService = TrackerWebService(context, TrackerApi.USER_REGISTRATION)
        webService.params = Gson().toJson(request)

        TrackerConnection(webService, object : TrackerConnectionListener {
            override fun onConnectionSuccess(tag: Int, response: String) {
                try {
                    val map = Gson().fromJson<UserRegistrationMap>(response, object : TypeToken<UserRegistrationMap>() {}.type)
                    if (map.isValid()) {
                        listener?.onSuccess(map)
                    } else {
                        Logger.d("Error registering user ${map.id}")
                        listener?.onError()
                    }
                } catch (e: JsonSyntaxException) {
                    Logger.e("Error registering user json response")
                    listener?.onError()
                } catch (e: IllegalStateException) {
                    Logger.e("Error registering user json response")
                    listener?.onError()
                }
            }

            override fun onConnectionError(tag: Int) {
                Logger.e("Registering user to server error")
                listener?.onError()
            }

        }).connect()
    }
}
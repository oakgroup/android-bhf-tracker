package com.active.orbit.tracker.core.managers

import android.content.Context
import com.active.orbit.tracker.core.deserialization.UserRegistrationMap
import com.active.orbit.tracker.core.listeners.UserRegistrationListener
import com.active.orbit.tracker.core.network.TrackerApi
import com.active.orbit.tracker.core.network.TrackerConnection
import com.active.orbit.tracker.core.network.TrackerConnectionListener
import com.active.orbit.tracker.core.network.TrackerWebService
import com.active.orbit.tracker.core.serialization.UserRegistrationRequest
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.TimeUtils
import com.active.orbit.tracker.core.utils.TrackerUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object TrackerUserManager {

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
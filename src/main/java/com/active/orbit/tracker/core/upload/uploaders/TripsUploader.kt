package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.computation.DailyComputation
import com.active.orbit.tracker.core.database.tables.TrackerTableTrips
import com.active.orbit.tracker.core.deserialization.UploadTripsMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.TrackerApi
import com.active.orbit.tracker.core.network.TrackerConnection
import com.active.orbit.tracker.core.network.TrackerConnectionListener
import com.active.orbit.tracker.core.network.TrackerWebService
import com.active.orbit.tracker.core.preferences.engine.TrackerPreferences
import com.active.orbit.tracker.core.serialization.TripsRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
import com.active.orbit.tracker.core.utils.TimeUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object TripsUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Trips upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            // we can only send the trips up to yesterday night
            val currentMidnight = TimeUtils.midnightInMsecs(System.currentTimeMillis())

            var lastTripsUpload = TrackerPreferences.lifecycle(context).lastTripsUpload
            if (lastTripsUpload == null || lastTripsUpload == Constants.INVALID.toLong()) {
                val firstInstall = TrackerPreferences.lifecycle(context).firstInstall
                lastTripsUpload = if (firstInstall == null || firstInstall == Constants.INVALID.toLong()) {
                    TimeUtils.midnightInMsecs(System.currentTimeMillis())
                } else {
                    TimeUtils.midnightInMsecs(firstInstall)
                }
            }

            if (lastTripsUpload >= (currentMidnight - TimeUtils.ONE_DAY_MILLIS)) {
                Logger.d("Trying to upload trips on server too soon")
                listener?.onResult(false)
                return@backgroundThread
            }

            while (lastTripsUpload < currentMidnight) {
                val dataComputer = DailyComputation(context, lastTripsUpload, currentMidnight)
                dataComputer.computeResults(false)
                lastTripsUpload += TimeUtils.ONE_DAY_MILLIS
            }

            val models = TrackerTableTrips.getNotUploadedBefore(context, currentMidnight)
            if (models.isEmpty()) {
                Logger.d("No trips to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = TripsRequest()
            request.userId = TrackerPreferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = TripsRequest.TripRequest(model)
                request.trips.add(modelRequest)
            }

            val webService = TrackerWebService(context, TrackerApi.INSERT_TRIPS)
            webService.params = Gson().toJson(request)

            TrackerConnection(webService, object : TrackerConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadTripsMap? = null
                    try {
                        map = Gson().fromJson<UploadTripsMap>(response, object : TypeToken<UploadTripsMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing trips json response")
                    } catch (e: IllegalStateException) {
                        Logger.e("Error trips json response")
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Trips uploaded to server ${map.inserted} success")
                            TrackerPreferences.lifecycle(context).lastTripsUpload = lastTripsUpload
                            backgroundThread {
                                // mark trips as uploaded
                                models.forEach { it.uploaded = true }
                                TrackerTableTrips.upsert(context, models)
                                mainThread {
                                    isUploading = false
                                    listener?.onResult(true)
                                }
                            }

                        } else {
                            Logger.d("Trips uploaded to server error ${map.inserted} success")
                            isUploading = false
                            listener?.onResult(false)
                        }
                    } else {
                        Logger.e("Trips uploaded to server invalid")
                        isUploading = false
                        listener?.onResult(false)
                    }
                }

                override fun onConnectionError(tag: Int) {
                    Logger.e("Error uploading trips to server")
                    isUploading = false
                    listener?.onResult(false)
                }

            }).connect()
        }
    }
}
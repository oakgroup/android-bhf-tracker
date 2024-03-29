/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.upload.uploaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import uk.ac.shef.tracker.core.computation.DailyComputation
import uk.ac.shef.tracker.core.database.tables.TrackerTableTrips
import uk.ac.shef.tracker.core.deserialization.UploadTripsMap
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.network.TrackerApi
import uk.ac.shef.tracker.core.network.TrackerConnection
import uk.ac.shef.tracker.core.network.TrackerConnectionListener
import uk.ac.shef.tracker.core.network.TrackerWebService
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.serialization.TripsRequest
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.TimeUtils
import uk.ac.shef.tracker.core.utils.background
import uk.ac.shef.tracker.core.utils.main
import kotlin.coroutines.CoroutineContext

/**
 * This class manages the upload of all the trips data
 */
object TripsUploader : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private var isUploading = false

    /**
     * This starts the data upload
     *
     * @param context an instance of [Context]
     * @param listener an optional listener to receive the result callback
     */
    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Trips upload already in progress")
            listener?.onResult(false)
            return
        }

        background {

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
                return@background
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
                return@background
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
                        isUploading = false
                        listener?.onResult(false)
                        return
                    } catch (e: IllegalStateException) {
                        Logger.e("Error trips json response")
                        isUploading = false
                        listener?.onResult(false)
                        return
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Trips uploaded to server ${map.inserted} success")
                            TrackerPreferences.lifecycle(context).lastTripsUpload = lastTripsUpload
                            background {
                                // mark trips as uploaded
                                models.forEach { it.uploaded = true }
                                TrackerTableTrips.update(context, models)
                                main {
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
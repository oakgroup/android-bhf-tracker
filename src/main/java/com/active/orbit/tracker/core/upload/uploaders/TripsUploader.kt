package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.database.tables.TableTrips
import com.active.orbit.tracker.core.deserialization.UploadTripsMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.Api
import com.active.orbit.tracker.core.network.Connection
import com.active.orbit.tracker.core.network.ConnectionListener
import com.active.orbit.tracker.core.network.WebService
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.serialization.TripsRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
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

            val models = TableTrips.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No trips to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = TripsRequest()
            request.userId = Preferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = TripsRequest.TripRequest(model)
                request.trips.add(modelRequest)
            }

            val webService = WebService(context, Api.INSERT_TRIPS)
            webService.params = Gson().toJson(request)

            Connection(webService, object : ConnectionListener {
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
                            backgroundThread {
                                // mark trips as uploaded
                                models.forEach { it.uploaded = true }
                                TableTrips.upsert(context, models)
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
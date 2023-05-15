package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.database.tables.TrackerTableLocations
import com.active.orbit.tracker.core.deserialization.UploadLocationsMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.TrackerApi
import com.active.orbit.tracker.core.network.TrackerConnection
import com.active.orbit.tracker.core.network.TrackerConnectionListener
import com.active.orbit.tracker.core.network.TrackerWebService
import com.active.orbit.tracker.core.preferences.engine.TrackerPreferences
import com.active.orbit.tracker.core.serialization.LocationsRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object LocationsUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Locations upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            val models = TrackerTableLocations.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No locations to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = LocationsRequest()
            request.userId = TrackerPreferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = LocationsRequest.LocationRequest(model)
                request.locations.add(modelRequest)
            }

            val webService = TrackerWebService(context, TrackerApi.INSERT_LOCATIONS)
            webService.params = Gson().toJson(request)

            TrackerConnection(webService, object : TrackerConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadLocationsMap? = null
                    try {
                        map = Gson().fromJson<UploadLocationsMap>(response, object : TypeToken<UploadLocationsMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing locations json response")
                    } catch (e: IllegalStateException) {
                        Logger.e("Error locations json response")
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Locations uploaded to server ${map.inserted} success")
                            backgroundThread {
                                // mark locations as uploaded
                                models.forEach { it.uploaded = true }
                                TrackerTableLocations.upsert(context, models)
                                mainThread {
                                    isUploading = false
                                    listener?.onResult(true)
                                }
                            }

                        } else {
                            Logger.d("Locations uploaded to server error ${map.inserted} success")
                            isUploading = false
                            listener?.onResult(false)
                        }
                    } else {
                        Logger.e("Locations uploaded to server invalid")
                        isUploading = false
                        listener?.onResult(false)
                    }
                }

                override fun onConnectionError(tag: Int) {
                    Logger.e("Error uploading locations to server")
                    isUploading = false
                    listener?.onResult(false)
                }

            }).connect()
        }
    }
}
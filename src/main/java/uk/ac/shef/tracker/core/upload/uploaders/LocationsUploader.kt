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
import uk.ac.shef.tracker.core.database.tables.TrackerTableLocations
import uk.ac.shef.tracker.core.deserialization.UploadLocationsMap
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.network.TrackerApi
import uk.ac.shef.tracker.core.network.TrackerConnection
import uk.ac.shef.tracker.core.network.TrackerConnectionListener
import uk.ac.shef.tracker.core.network.TrackerWebService
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.serialization.LocationsRequest
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.background
import uk.ac.shef.tracker.core.utils.main
import kotlin.coroutines.CoroutineContext

/**
 * This class manages the upload of all the locations data
 */
object LocationsUploader : CoroutineScope {

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
            Logger.d("Locations upload already in progress")
            listener?.onResult(false)
            return
        }

        background {

            val models = TrackerTableLocations.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No locations to upload on server")
                listener?.onResult(false)
                return@background
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
                        isUploading = false
                        listener?.onResult(false)
                        return
                    } catch (e: IllegalStateException) {
                        Logger.e("Error locations json response")
                        isUploading = false
                        listener?.onResult(false)
                        return
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Locations uploaded to server ${map.inserted} success")
                            background {
                                // mark locations as uploaded
                                models.forEach { it.uploaded = true }
                                TrackerTableLocations.upsert(context, models)
                                main {
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
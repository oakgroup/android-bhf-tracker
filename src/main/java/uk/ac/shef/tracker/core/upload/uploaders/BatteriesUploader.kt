/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.upload.uploaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import uk.ac.shef.tracker.core.database.tables.TrackerTableBatteries
import uk.ac.shef.tracker.core.deserialization.UploadBatteriesMap
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.network.TrackerApi
import uk.ac.shef.tracker.core.network.TrackerConnection
import uk.ac.shef.tracker.core.network.TrackerConnectionListener
import uk.ac.shef.tracker.core.network.TrackerWebService
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.serialization.BatteriesRequest
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.ThreadHandler.backgroundThread
import uk.ac.shef.tracker.core.utils.ThreadHandler.mainThread

object BatteriesUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Batteries upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            val models = TrackerTableBatteries.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No batteries to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = BatteriesRequest()
            request.userId = TrackerPreferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = BatteriesRequest.BatteryRequest(model)
                request.batteries.add(modelRequest)
            }

            val webService = TrackerWebService(context, TrackerApi.INSERT_BATTERIES)
            webService.params = Gson().toJson(request)

            TrackerConnection(webService, object : TrackerConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadBatteriesMap? = null
                    try {
                        map = Gson().fromJson<UploadBatteriesMap>(response, object : TypeToken<UploadBatteriesMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing batteries json response")
                        isUploading = false
                        listener?.onResult(false)
                        return
                    } catch (e: IllegalStateException) {
                        Logger.e("Error batteries json response")
                        isUploading = false
                        listener?.onResult(false)
                        return
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Batteries uploaded to server ${map.inserted} success")
                            backgroundThread {
                                // mark batteries as uploaded
                                models.forEach { it.uploaded = true }
                                TrackerTableBatteries.upsert(context, models)
                                mainThread {
                                    isUploading = false
                                    listener?.onResult(true)
                                }
                            }

                        } else {
                            Logger.d("Batteries uploaded to server error ${map.inserted} success")
                            isUploading = false
                            listener?.onResult(false)
                        }
                    } else {
                        Logger.e("Batteries uploaded to server invalid")
                        isUploading = false
                        listener?.onResult(false)
                    }
                }

                override fun onConnectionError(tag: Int) {
                    Logger.e("Error uploading batteries to server")
                    isUploading = false
                    listener?.onResult(false)
                }

            }).connect()
        }
    }
}
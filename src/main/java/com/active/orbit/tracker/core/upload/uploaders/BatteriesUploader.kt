package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.database.tables.TableBatteries
import com.active.orbit.tracker.core.deserialization.UploadBatteriesMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.Api
import com.active.orbit.tracker.core.network.Connection
import com.active.orbit.tracker.core.network.ConnectionListener
import com.active.orbit.tracker.core.network.WebService
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.serialization.BatteriesRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object BatteriesUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Batteries upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            val models = TableBatteries.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No batteries to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = BatteriesRequest()
            request.userId = Preferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = BatteriesRequest.BatteryRequest(model)
                request.batteries.add(modelRequest)
            }

            val webService = WebService(context, Api.INSERT_BATTERIES)
            webService.params = Gson().toJson(request)

            Connection(webService, object : ConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadBatteriesMap? = null
                    try {
                        map = Gson().fromJson<UploadBatteriesMap>(response, object : TypeToken<UploadBatteriesMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing batteries json response")
                    } catch (e: IllegalStateException) {
                        Logger.e("Error batteries json response")
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Batteries uploaded to server ${map.inserted} success")
                            backgroundThread {
                                // mark batteries as uploaded
                                models.forEach { it.uploaded = true }
                                TableBatteries.upsert(context, models)
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
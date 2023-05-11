package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.database.tables.TableHeartRates
import com.active.orbit.tracker.core.deserialization.UploadHeartRatesMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.Api
import com.active.orbit.tracker.core.network.Connection
import com.active.orbit.tracker.core.network.ConnectionListener
import com.active.orbit.tracker.core.network.WebService
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.serialization.HeartRatesRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object HeartRatesUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("HeartRates upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            val models = TableHeartRates.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No heart rates to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = HeartRatesRequest()
            request.userId = Preferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = HeartRatesRequest.HeartRateRequest(model)
                request.heartRates.add(modelRequest)
            }

            val webService = WebService(context, Api.INSERT_HEART_RATES)
            webService.params = Gson().toJson(request)

            Connection(webService, object : ConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadHeartRatesMap? = null
                    try {
                        map = Gson().fromJson<UploadHeartRatesMap>(response, object : TypeToken<UploadHeartRatesMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing heart rates json response")
                    } catch (e: IllegalStateException) {
                        Logger.e("Error heart rates json response")
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("HeartRates uploaded to server ${map.inserted} success")
                            backgroundThread {
                                // mark heart rates as uploaded
                                models.forEach { it.uploaded = true }
                                TableHeartRates.upsert(context, models)
                                mainThread {
                                    isUploading = false
                                    listener?.onResult(true)
                                }
                            }

                        } else {
                            Logger.d("HeartRates uploaded to server error ${map.inserted} success")
                            isUploading = false
                            listener?.onResult(false)
                        }
                    } else {
                        Logger.e("HeartRates uploaded to server invalid")
                        isUploading = false
                        listener?.onResult(false)
                    }
                }

                override fun onConnectionError(tag: Int) {
                    Logger.e("Error uploading heart rates to server")
                    isUploading = false
                    listener?.onResult(false)
                }

            }).connect()
        }
    }
}
package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.database.tables.TableSteps
import com.active.orbit.tracker.core.deserialization.UploadStepsMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.Api
import com.active.orbit.tracker.core.network.Connection
import com.active.orbit.tracker.core.network.ConnectionListener
import com.active.orbit.tracker.core.network.WebService
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.serialization.StepsRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object StepsUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Steps upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            val models = TableSteps.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No steps to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = StepsRequest()
            request.userId = Preferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = StepsRequest.StepRequest(model)
                request.steps.add(modelRequest)
            }

            val webService = WebService(context, Api.INSERT_STEPS)
            webService.params = Gson().toJson(request)

            Connection(webService, object : ConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadStepsMap? = null
                    try {
                        map = Gson().fromJson<UploadStepsMap>(response, object : TypeToken<UploadStepsMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing steps json response")
                    } catch (e: IllegalStateException) {
                        Logger.e("Error steps json response")
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Steps uploaded to server ${map.inserted} success")
                            backgroundThread {
                                // mark steps as uploaded
                                models.forEach { it.uploaded = true }
                                TableSteps.upsert(context, models)
                                mainThread {
                                    isUploading = false
                                    listener?.onResult(true)
                                }
                            }

                        } else {
                            Logger.d("Steps uploaded to server error ${map.inserted} success")
                            isUploading = false
                            listener?.onResult(false)
                        }
                    } else {
                        Logger.e("Steps uploaded to server invalid")
                        isUploading = false
                        listener?.onResult(false)
                    }
                }

                override fun onConnectionError(tag: Int) {
                    Logger.e("Error uploading steps to server")
                    isUploading = false
                    listener?.onResult(false)
                }

            }).connect()
        }
    }
}
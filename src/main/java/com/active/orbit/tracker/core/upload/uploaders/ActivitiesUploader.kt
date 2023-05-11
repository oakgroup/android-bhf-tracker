package com.active.orbit.tracker.core.upload.uploaders

import android.content.Context
import com.active.orbit.tracker.core.database.tables.TableActivities
import com.active.orbit.tracker.core.deserialization.UploadActivitiesMap
import com.active.orbit.tracker.core.listeners.ResultListener
import com.active.orbit.tracker.core.network.Api
import com.active.orbit.tracker.core.network.Connection
import com.active.orbit.tracker.core.network.ConnectionListener
import com.active.orbit.tracker.core.network.WebService
import com.active.orbit.tracker.core.preferences.engine.Preferences
import com.active.orbit.tracker.core.serialization.ActivitiesRequest
import com.active.orbit.tracker.core.utils.Constants
import com.active.orbit.tracker.core.utils.Logger
import com.active.orbit.tracker.core.utils.ThreadHandler.backgroundThread
import com.active.orbit.tracker.core.utils.ThreadHandler.mainThread
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

object ActivitiesUploader {

    private var isUploading = false

    fun uploadData(context: Context, listener: ResultListener? = null) {
        if (isUploading) {
            Logger.d("Activities upload already in progress")
            listener?.onResult(false)
            return
        }

        backgroundThread {

            val models = TableActivities.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No activities to upload on server")
                listener?.onResult(false)
                return@backgroundThread
            }

            isUploading = true

            val request = ActivitiesRequest()
            request.userId = Preferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = ActivitiesRequest.ActivityRequest(model)
                request.activities.add(modelRequest)
            }

            val webService = WebService(context, Api.INSERT_ACTIVITIES)
            webService.params = Gson().toJson(request)

            Connection(webService, object : ConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadActivitiesMap? = null
                    try {
                        map = Gson().fromJson<UploadActivitiesMap>(response, object : TypeToken<UploadActivitiesMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing activities json response")
                    } catch (e: IllegalStateException) {
                        Logger.e("Error activities json response")
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Activities uploaded to server ${map.inserted} success")
                            backgroundThread {
                                // mark activities as uploaded
                                models.forEach { it.uploaded = true }
                                TableActivities.upsert(context, models)
                                mainThread {
                                    isUploading = false
                                    listener?.onResult(true)
                                }
                            }

                        } else {
                            Logger.d("Activities uploaded to server error ${map.inserted} success")
                            isUploading = false
                            listener?.onResult(false)
                        }
                    } else {
                        Logger.e("Activities uploaded to server invalid")
                        isUploading = false
                        listener?.onResult(false)
                    }
                }

                override fun onConnectionError(tag: Int) {
                    Logger.e("Error uploading activities to server")
                    isUploading = false
                    listener?.onResult(false)
                }

            }).connect()
        }
    }
}
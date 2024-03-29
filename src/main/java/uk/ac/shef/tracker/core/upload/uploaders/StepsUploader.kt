/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.upload.uploaders

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import uk.ac.shef.tracker.core.database.tables.TrackerTableSteps
import uk.ac.shef.tracker.core.deserialization.UploadStepsMap
import uk.ac.shef.tracker.core.listeners.ResultListener
import uk.ac.shef.tracker.core.network.TrackerApi
import uk.ac.shef.tracker.core.network.TrackerConnection
import uk.ac.shef.tracker.core.network.TrackerConnectionListener
import uk.ac.shef.tracker.core.network.TrackerWebService
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.serialization.StepsRequest
import uk.ac.shef.tracker.core.utils.Constants
import uk.ac.shef.tracker.core.utils.Logger
import uk.ac.shef.tracker.core.utils.background
import uk.ac.shef.tracker.core.utils.main
import kotlin.coroutines.CoroutineContext

/**
 * This class manages the upload of all the steps data
 */
object StepsUploader : CoroutineScope {

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
            Logger.d("Steps upload already in progress")
            listener?.onResult(false)
            return
        }

        background {

            val models = TrackerTableSteps.getNotUploaded(context)
            if (models.isEmpty()) {
                Logger.d("No steps to upload on server")
                listener?.onResult(false)
                return@background
            }

            isUploading = true

            val request = StepsRequest()
            request.userId = TrackerPreferences.user(context).idUser ?: Constants.EMPTY

            for (model in models) {
                val modelRequest = StepsRequest.StepRequest(model)
                request.steps.add(modelRequest)
            }

            val webService = TrackerWebService(context, TrackerApi.INSERT_STEPS)
            webService.params = Gson().toJson(request)

            TrackerConnection(webService, object : TrackerConnectionListener {
                override fun onConnectionSuccess(tag: Int, response: String) {
                    var map: UploadStepsMap? = null
                    try {
                        map = Gson().fromJson<UploadStepsMap>(response, object : TypeToken<UploadStepsMap>() {}.type)
                    } catch (e: JsonSyntaxException) {
                        Logger.e("Error parsing steps json response")
                        isUploading = false
                        listener?.onResult(false)
                        return
                    } catch (e: IllegalStateException) {
                        Logger.e("Error steps json response")
                        isUploading = false
                        listener?.onResult(false)
                        return
                    }

                    if (map?.isValid() == true) {
                        if (map.inserted!! >= models.size) {
                            Logger.d("Steps uploaded to server ${map.inserted} success")
                            background {
                                // mark steps as uploaded
                                models.forEach { it.uploaded = true }
                                TrackerTableSteps.update(context, models)
                                main {
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
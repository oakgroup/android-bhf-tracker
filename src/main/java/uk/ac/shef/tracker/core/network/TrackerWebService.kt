/*
 *  Copyright (c) 2023. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.network

import android.content.Context
import uk.ac.shef.tracker.core.preferences.engine.TrackerPreferences
import uk.ac.shef.tracker.core.utils.Constants
import java.net.MalformedURLException
import java.net.URL

/**
 * Class used to build a web service with url and params
 *
 * @author omar.brugna
 */
class TrackerWebService(context: Context, api: TrackerApi, var params: String? = null) {

    private var baseUrl = TrackerPreferences.backend(context).baseUrl

    var method = TrackerNetwork.POST
    var encoding = TrackerNetwork.ENCODING_UTF8
    var urlString = Constants.EMPTY

    var contentType = TrackerNetwork.CONTENT_TYPE_APPLICATION_JSON
    var connection = TrackerNetwork.KEEP_ALIVE
    var cacheControl = TrackerNetwork.NO_CACHE

    var headers = HashMap<String, String>()

    /**
     * Return the URL object that represent the WebService
     *
     * @return an [URL] object
     * @throws MalformedURLException the raised exception
     */
    val url: URL
        @Throws(MalformedURLException::class)
        get() = URL(urlString)

    init {
        urlString = "$baseUrl/${api.getUrl(context)}"
    }
}
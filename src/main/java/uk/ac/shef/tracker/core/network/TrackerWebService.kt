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
 * Class used to build a web service with url and params and start a [TrackerConnection]
 */
class TrackerWebService(context: Context, api: TrackerApi, var params: String? = null) {

    /**
     * This is the domain, for example https://www.test.com
     */
    private var baseUrl = TrackerPreferences.backend(context).baseUrl

    /**
     * The request method
     */
    var method = TrackerNetwork.POST

    /**
     * The request encoding
     */
    var encoding = TrackerNetwork.ENCODING_UTF8

    /**
     * The full url string, for example https://www.test.com/retrieve_books?foo=bar
     */
    var urlString = Constants.EMPTY

    /**
     * The request content type
     */
    var contentType = TrackerNetwork.CONTENT_TYPE_APPLICATION_JSON

    /**
     * The request connection type
     */
    var connection = TrackerNetwork.KEEP_ALIVE

    /**
     * The request cache control type
     */
    var cacheControl = TrackerNetwork.NO_CACHE

    /**
     * The request headers
     */
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

    /**
     * The default initializer will build the url using the baseUrl + the apiUrl
     */
    init {
        urlString = "$baseUrl/${api.getUrl(context)}"
    }
}
/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.network

/**
 * Utility class that defines some constants values used for network requests
 */
object TrackerNetwork {

    const val ENCODING_UTF8 = "UTF-8"
    const val USER_AGENT = "User-Agent"
    const val AUTHORIZATION = "Authorization"
    const val BEARER = "Bearer"
    const val CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_APPLICATION_JSON = "application/json"
    const val CONNECTION = "Connection"
    const val CACHE_CONTROL = "Cache-Control"
    const val KEEP_ALIVE = "Keep-Alive"
    const val NO_CACHE = "no-cache"
    const val POST = "POST"
    const val GET = "GET"
    const val DELETE = "DELETE"
    const val PUT = "PUT"
    const val PATCH = "PATCH"
    const val CONNECTION_TIMEOUT = 10000
    const val CONNECTION_TIMEOUT_EXTENDED = 30000
    const val SOCKET_TIMEOUT = 40000
    const val SOCKET_TIMEOUT_EXTENDED = 60000
}